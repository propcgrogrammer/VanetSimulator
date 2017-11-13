package vanetsim.map;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.localization.Messages;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CyclicBarrier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * The map. The coordinate system is 2-dimensional with each axis allowing values from
 * 0 to Integer.MAXVALUE (2147483647). Negative values are not allowed. The scale is 1:1cm which means
 * that about 21474km x 21474km is the maximum size of a map (should be more than enough for all cases).
 * The map is divided into several rectangular {@link Region}s in order to improve performance. All
 * vehicles, nodes and streets are stored in these regions.
 * Because of the regions, for example rendering and distance calculations only need to be done
 * on a limited amount of vehicles/streets/nodes which helps handling large maps a lot.
 */
public class Map {

    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /** The only instance of this class (singleton). */
    private static final Map INSTANCE = new Map();

    /** The width of a single lane (3m). Used in various other places in this program! */
    public static final int LANE_WIDTH = 300;

    /** The width of the map in cm. */
    private int width_ = 0;

    /** The height of the map in cm. */
    private int height_ = 0;

    /** The width of a region in cm. */
    private int regionWidth_ = 0;

    /** The height of a region in cm. */
    private int regionHeight_ = 0;

    /** The amount of regions in x direction. */
    private int regionCountX_ = 0;

    /** The amount of regions in y direction. */
    private int regionCountY_ = 0;

    /** The region in which this node is. */
    private Region region_;

    /** An array holding all {@link Region}s. */
    private Region[][] regions_ = null;

    /** A flag to signal if loading is ready. While loading is in progress, simulation and rendering is not possible. */
    private boolean ready_ = true;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */
    /**
     * Empty, private constructor in order to disable instancing.
     */
    private Map() {
        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "Map()", Debug.ISLOGGED);
    }

    /**
     * Gets the single instance of this map.
     *
     * @return single instance of this map
     */
    public static Map getInstance(){
        return INSTANCE;
    }


    /**
     * Initializes a new map.
     * 於2017/10/23_2331新增
     * @param width			the width
     * @param height		the height
     * @param regionWidth	the width of a region
     * @param regionHeight	the height of a region
     */
    public void initNewMap(int width, int height, int regionWidth, int regionHeight){
        int i, j;
        if(ready_ == true) {
            ready_ = false;
            //cleanup!
            if (!Renderer.getInstance().isConsoleStart()) {

                /** 暫時封閉此行，該initNewScenario（）於 2017/10/24_0448 更新完成 */
                Scenario.getInstance().initNewScenario();	//stops the simulation thread so we don't need to do it here
                /** 初始化完成後更新其狀態為準備等待執行 */
                Scenario.getInstance().setReadyState(true);
            }

            /**
             * 分別設定原始地圖的寬（width) 和 高（height)
             * 分別設定每一個區域的寬（regionWidth) 和 高（regionHeight)
             * */
            width_ = width;
            height_ = height;
            regionWidth_ = regionWidth;
            regionHeight_ = regionHeight;

            Renderer.getInstance().setMarkedStreet(null);
            Renderer.getInstance().setMarkedVehicle(null);
            Renderer.getInstance().setAttackerVehicle(null);
            Renderer.getInstance().setAttackedVehicle(null);

            if(!Renderer.getInstance().isConsoleStart()) MouseClickManager.getInstance().cleanMarkings();

            // create the regions on the map
            /** 計算地圖x向座標分成幾個區域 */
            regionCountX_ = width_/regionWidth_;
            if(width_%regionWidth_ > 0) ++regionCountX_;
            /** 計算地圖y向座標分成幾個區域 */
            regionCountY_ = height_/regionHeight_;
            if(height_%regionHeight_ > 0) ++regionCountY_;

            /** 宣告地圖陣列 */
            regions_ = new Region[regionCountX_][regionCountY_];

            int upperboundary = 0, leftboundary = 0;
            for(i = 0; i < regionCountX_; ++i){
                for(j = 0; j < regionCountY_; ++j){
                    regions_[i][j] = new Region(i,j, leftboundary, leftboundary + regionWidth_-1, upperboundary, upperboundary + regionHeight_-1);
                    upperboundary += regionHeight_;
                }
                leftboundary += regionWidth_;
                upperboundary = 0;
            }
            Vehicle.setRegions(regions_);
            RSU.setRegions(regions_);
        }else{
            ErrorLog.log(Messages.getString("Map.mapLocked"), 7, getClass().getName(), "initNewMap", null);
        }
    }

    /**
     * This function needs to be called to signal that the loading process of the map has finished.
     */
    public void signalMapLoaded(){
        // optimize the ArrayLists in the regions in order to free wasted memory

        for(int i = 0; i < regionCountX_; ++i) {
            for (int j = 0; j < regionCountY_; ++j) {

                /** 注意calculateJunctions（）未實作 */
                regions_[i][j].calculateJunctions();
            }
        }
        ready_ = true;
        if(!Renderer.getInstance().isConsoleStart()){
            Renderer.getInstance().setMiddle(width_/2, height_/2);
            Renderer.getInstance().setMapZoom(Math.exp(5/100.0)/1000);
            Renderer.getInstance().ReRender(true, false);

        }

        //start a thread which calculates bridges in background so that loading is faster (it's just eyecandy and not necessary otherwise ;))
        Runnable job = new Runnable() {
            public void run(){
                for(int i = 0; i < regionCountX_; ++i){
                    for(int j = 0; j < regionCountY_; ++j){
                        /** 待實作checkStreetsForBridges（）方法 */
                        //regions_[i][j].checkStreetsForBridges();
                    }
                }
            }
        };
        Thread t = new Thread(job);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    /**
     * Add a new node to the correct region. A node can only be in one region.
     *
     * @param node	the node to add
     *
     * @return the added node (might be different if already existing!)
     */
    public Node addNode(Node node){
        int regionX = node.getX()/regionWidth_;	//implicit rounding (=floor)because of integer values!
        int regionY = node.getY()/regionHeight_;

        // to prevent "array out of bounds" when node is outside of map
        if (regionX >= regionCountX_) regionX = regionCountX_ - 1;
        else if(regionX < 0) regionX = 0;
        if (regionY >= regionCountY_) regionY = regionCountY_ - 1;
        else if (regionY < 0) regionY = 0;

        node.setRegion(regions_[regionX][regionY]);
        return regions_[regionX][regionY].addNode(node, true);
    }

    /**
     * Add a new street to the correct region(s). Note that a street can be in multiple regions and so we must determine
     * all here! This makes rendering and calculations a lot easier later!
     *
     * @param street	the street to add
     */
    public void addStreet(Street street){
        int startRegionX = street.getStartNode().getRegion().getX();
        int startRegionY = street.getStartNode().getRegion().getY();
        int endRegionX = street.getEndNode().getRegion().getX();
        int endRegionY = street.getEndNode().getRegion().getY();
        int i;

        // find the regions in which this street belongs!
        if(startRegionX == endRegionX){
            if(startRegionY == endRegionY) regions_[startRegionX][startRegionY].addStreet(street, true);		// just in one region
            else{	// above or beneath
                if(startRegionY < endRegionY){
                    for (i = startRegionY; i <= endRegionY; ++i) regions_[startRegionX][i].addStreet(street, true);
                } else {
                    for (i = endRegionY; i <= startRegionY; ++i) regions_[startRegionX][i].addStreet(street, true);
                }
            }
        } else if(startRegionY == endRegionY){
            if(startRegionX < endRegionX){	// left or right
                for (i = startRegionX; i <= endRegionX; ++i) regions_[i][startRegionY].addStreet(street, true);
            } else {
                for (i = endRegionX; i <= startRegionX; ++i) regions_[i][startRegionY].addStreet(street, true);
            }
        } else{		// seems to be non-trivial crossing regions, try some kind of bruteforce now!
            // we now need the real coordinates and not just the regions!
            int start_x = street.getStartNode().getX();
            int start_y = street.getStartNode().getY();
            int end_x = street.getEndNode().getX();
            int end_y = street.getEndNode().getY();

            regions_[startRegionX][startRegionY].addStreet(street, true);
            regions_[endRegionX][endRegionY].addStreet(street, true);

            // calculate line parameters: y = ax + b
            double a = ((double)start_y - end_y) / ((double)start_x - end_x);	// (start_x - end_x) can't be zero because then (start_region_x == end_region_x) above would have been true!
            double b = start_y - a * start_x;

            double x, y;
            long tmp;

            int max_x = Math.max(endRegionX, startRegionX);		//cache so that the math-function isn't called too often
            int max_y = Math.max(startRegionY, endRegionY);
            for(i = Math.min(startRegionX, endRegionX); i < max_x; ++i){	// check all vertical grid lines of the regions to be considered
                y = a * (i * regionWidth_) + b;	// left side of this grid
                tmp = Math.round(y) / regionHeight_;
                if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].addStreet(street, true);
                y = a * ((i * regionWidth_) + regionWidth_ - 1) + b;	// right side of this grid
                tmp = Math.round(y) / regionHeight_;
                if(tmp > -1 && tmp < regionCountY_) regions_[i][(int)tmp].addStreet(street, true);
            }
            for(i = Math.min(startRegionY, endRegionY); i < max_y; ++i){	// check all horizontal grid lines of the regions to be considered
                x = ((i * regionHeight_) - b)/ a;		// upper side of this grid
                tmp = Math.round(x) / regionWidth_;
                if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].addStreet(street, true);
                x = (((i * regionHeight_) + regionHeight_ - 1) - b)/ a;	// lower side of this grid
                tmp = Math.round(x) / regionWidth_;
                if(tmp > -1 && tmp < regionCountX_) regions_[(int)tmp][i].addStreet(street, true);
            }
        }
    }

    /**
     * ///////////// getter & setter （start) /////////////
     */
    /**
     * Sets the region in which this node is found.
     *
     * @param region the region
     */
    public void setRegion(Region region) {
        region_ = region;
    }
    /**
     * Gets the map width.
     *
     * @return the map width
     */
    public int getMapWidth(){
        return width_;
    }

    /**
     * Gets the map height.
     *
     * @return the map height
     */
    public int getMapHeight(){
        return height_;
    }

    /**
     * Calculates the {@link Region} of a point.
     *
     * @param x	the x coordinate of the point
     * @param y	the y coordinate of the point
     *
     * @return the region in which this point is located or <code>null</code> if there was a problem
     */
    public Region getRegionOfPoint(int x, int y){
        if(regionWidth_ > 0 && regionHeight_ > 0){
            int region_x = x/regionWidth_;
            int region_y = y/regionHeight_;
            if (region_x < 0) region_x = 0;
            else if (region_x >= regionCountX_) region_x = regionCountX_ - 1;
            if (region_y < 0) region_y = 0;
            else if (region_y >= regionCountY_) region_y = regionCountY_ - 1;
            return regions_[region_x][region_y];
        } else return null;
    }

    /**
     * write silent period header to log file
     */
    public void writeSilentPeriodHeader(){

    }


    /**
     * Gets all regions.
     *
     * @return all regions
     */
    public Region[][] getRegions(){
        return regions_;
    }

    /**
     * Gets the amount of regions in x direction.
     *
     * @return the amount of regions in x direction
     */
    public int getRegionCountX(){
        return regionCountX_;
    }

    /**
     * Gets the amount of regions in y direction.
     *
     * @return the amount of regions in y direction
     */
    public int getRegionCountY(){
        return regionCountY_;
    }

    /**
     * Returns if a map is currently in the process of being loaded. While loading, simulation
     * and rendering should not be done because not all map elements are already existing!
     *
     * @return <code>true</code> if loading has finished, else <code>false</code>
     */
    public boolean getReadyState(){
        return ready_;
    }
    /**
     * ///////////// getter & setter （end) /////////////
     */


    /**
     * ///////////// 地圖載入與儲存 （start) /////////////
     */

    /**
     * Load a map.
     *
     * @param file	the file to load
     * @param zip	<code>true</code> if the file given is zipped, else <code>false</code>
     */
    public void load(File file, boolean zip){

    }


    /**
     * Save the map.
     *
     * @param file	the file in which to save
     * @param zip	if <code>true</code>, file is saved in a compressed zip file (extension .zip is added to <code>file</code>!). If <code>false</code>, no compression is made.
     */
    public void save(File file, boolean zip){

    }

    /**
     * ///////////// 地圖載入與儲存 （end) /////////////
     */

}
