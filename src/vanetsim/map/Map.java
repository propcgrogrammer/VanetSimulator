package vanetsim.map;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.localization.Messages;

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
        Debug.whereru(this.getClass().getName(), true);
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
     * ///////////// getter & setter （start) /////////////
     */
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
