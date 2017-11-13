package vanetsim.map;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.scenario.Vehicle;

import java.util.ArrayList;

/**
 * A region stores all objects in a specific part of the map. It stores streets, nodes and vehicles.
 */
public final class Region {

    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */
    /**
     * 地區屬性變數
     */
    /** An empty vehicle array to prevent unnecessary object creation on <code>toArray()</code> operation. */
    private static final Vehicle[] EMPTY_VEHICLE = new Vehicle[0];

    /** An array storing all mix nodes. Within a defined distance, no communication is allowed (and beacon-IDs are changed). */
    private Node[] mixZoneNodes_ = new Node[0];

    /** An array storing all nodes in this region. */
    private Node[] nodes_ = new Node[0];	// This has a little bit overhead while loading compared to an ArrayList but requires less memory and is faster when iterating


    /**
     * 位子座標變數
     */
    /** The position on the x axis (in relation to all other regions => does not correspond to map coordinates!). */
    private int x_;

    /** The position on the y axis (in relation to all other regions => does not correspond to map coordinates!). */
    private int y_;

    /** The coordinate representing the left boundary of this region */
    private int leftBoundary_;

    /** The coordinate representing the right boundary of this region */
    private int rightBoundary_;

    /** The coordinate representing the upper boundary of this region */
    private int upperBoundary_;

    /** The coordinate representing the lower boundary of this region */
    private int lowerBoundary_;


    /**
     *  vehicle變數
     */
    /** <code>true</code> to indicate that the vehicles have changed since the last call to getVehicleArray() */
    private boolean vehiclesDirty_ = true;

    /** An <code>ArrayList</code> storing all vehicles in this region. */
    private ArrayList<Vehicle> vehicles_;	// changes relatively often so use ArrayList here

    /** The simulation requests an array for the vehicles which is cached here. */
    private Vehicle[] vehiclesArray_;


    /** An array storing all streets in this region. */
    private Street[] streets_ = new Street[0];		// This has a little bit overhead while loading compared to an ArrayList but requires less memory and is faster when iterating


    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */

    /**
     * Constructor for a region.
     *
     * @param x				the position on the x axis of the new region (該區域的X索引）
     * @param y				the position on the y axis of the new region (該區域的Y索引）
     * @param leftBoundary	the coordinate of the left boundary  (該區域的Ｘ起始座標）
     * @param rightBoundary	the coordinate of the right boundary (該區域的Ｘ終點座標）
     * @param upperBoundary	the coordinate of the upper boundary (該區域的Ｙ起始座標）
     * @param lowerBoundary	the coordinate of the lower boundary (該區域的Ｙ終點座標）
     */
    public Region(int x, int y, int leftBoundary, int rightBoundary, int upperBoundary, int lowerBoundary){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "Region(int x, int y, int leftBoundary, int rightBoundary, int upperBoundary, int lowerBoundary)", Debug.ISLOGGED);

        vehicles_ = new ArrayList<Vehicle>(1);
        x_ = x;
        y_ = y;
        leftBoundary_ = leftBoundary;
        rightBoundary_ = rightBoundary;
        upperBoundary_ = upperBoundary;
        lowerBoundary_ = lowerBoundary;
    }

    /**
     * Function to add a node to this region.
     *
     * @param node 		the node to add
     * @param doCheck 	<code>true</code> if a check should be made if this node already exists; else <code>false</code> to skip the test
     *
     * @return the node added (might be different from <code>node</code> if it already existed and <code>check</code> was true)
     */
    public Node addNode(Node node, boolean doCheck){
        if(doCheck){
            Node curNode, foundNode = null;
            int x = node.getX();   //cache to save function calls
            int y = node.getY();
            for(int i = 0; i < nodes_.length; ++i){
                curNode = nodes_[i];
                if(curNode.getX() == x && curNode.getY() == y){
                    foundNode = curNode;
                    break;
                }
            }
            if(foundNode != null) return foundNode;
        }
        Node[] newArray = new Node[nodes_.length+1];
        System.arraycopy (nodes_,0,newArray,0,nodes_.length);
        newArray[nodes_.length] = node;
        nodes_ = newArray;
        return node;
    }

    /**
     * Function to add a street to this region. This also checks if it is intersecting with other streets in this region
     * and sets the appropriate flag on the streets!
     *
     * @param street the street to add
     * @param doCheck 	<code>true</code> if a check should be made if this street already exists; else <code>false</code> to skip the test
     */
    public void addStreet(Street street, boolean doCheck){
        boolean foundstreet = false;
        boolean createBridges = false;
        if(Map.getInstance().getReadyState() == true) createBridges = true;
        if(streets_.length > 0 && (doCheck || createBridges)){
            Street otherStreet;
            int color1, color2;
            for(int i = 0; i < streets_.length; ++i){
                otherStreet = streets_[i];
                if((street.getStartNode() == otherStreet.getStartNode() || street.getStartNode() == otherStreet.getEndNode()) && (street.getEndNode() == otherStreet.getEndNode() ||  street.getEndNode() == otherStreet.getStartNode())) foundstreet = true;
                if(createBridges){
                    color1 = street.getDisplayColor().getRGB();
                    color2 = otherStreet.getDisplayColor().getRGB();
                    //check to which street we should add the bridge
                    /** 注意calculateBridges（）未實作 */
                    if(color1 != color2){
                        if(color1 < color2) MapHelper.calculateBridges(otherStreet, street);
                        else MapHelper.calculateBridges(street, otherStreet);
                    } else {
                        if(street.getBridgePaintLines() != null || street.getBridgePaintPolygons() != null) MapHelper.calculateBridges(street, otherStreet);	//add bridge to street which already has a bridge
                        else if(otherStreet.getBridgePaintLines() != null || otherStreet.getBridgePaintPolygons() != null) MapHelper.calculateBridges(otherStreet, street);
                        else if(street.getSpeed() > otherStreet.getSpeed()) MapHelper.calculateBridges(otherStreet, street);		//decide on speed
                        else MapHelper.calculateBridges(street, otherStreet);
                    }
                }
            }
        }
        if(!doCheck || !foundstreet){
            Street[] newArray = new Street[streets_.length+1];
            System.arraycopy (streets_,0,newArray,0,streets_.length);
            newArray[streets_.length] = street;
            streets_ = newArray;
        }
    }

    /**
     * This function should be called before starting simulation. All nodes calculate if they are junctions and
     * and what their priority streets are. Furthermore, mixing zones are generated.
     */
    public void calculateJunctions(){
        if(Renderer.getInstance().isAutoAddMixZones()) mixZoneNodes_ = new Node[0];

        for(int i = 0; i < nodes_.length; ++i){

            /** 注意calculateJunction（）未實作 */
            nodes_[i].calculateJunction();

            /** 待新增 */

        }
        /** 待實作prepareLogs（）方法 */
        //prepareLogs(nodes_);
    }



    /**
     * This function should be called before initializing a new scenario to delete all vehicles.
     */
    public void cleanVehicles(){
        vehicles_ = new ArrayList<Vehicle>(1);
        for(int i = 0; i < streets_.length; ++i){
            streets_[i].clearLanes();
        }
        vehiclesDirty_ = true;
    }

    /**
     * /////////// setter & getter (start) ///////////
     */

    /**
     * Function to get the x axis position of this region.
     *
     * @return x axis position
     */
    public int getX(){
        return x_;
    }

    /**
     * Function to get the y axis position of this region.
     *
     * @return y axis position
     */
    public int getY(){
        return y_;
    }

    /**
     * Creates an array as a copy of the vehicle <code>ArrayList</code> to prevent problems during simulation caused by
     * changing the <code>ArrayList</code> while reading it in another thread. The array is cached so that new ones are only
     * created when needed.
     *
     * @return the array copy of all vehicles in this region or an empty array if there are no elements
     */
    public Vehicle[] getVehicleArray(){
        if(vehiclesDirty_){
            if(vehicles_.size() == 0) vehiclesArray_ = EMPTY_VEHICLE;
            else vehiclesArray_ = vehicles_.toArray(EMPTY_VEHICLE);
            vehiclesDirty_ = false;
        }
        return vehiclesArray_;
    }

    /**
     * /////////// setter & getter (end) ///////////
     */


}
