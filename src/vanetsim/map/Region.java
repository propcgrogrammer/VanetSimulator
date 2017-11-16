package vanetsim.map;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;

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

    /** An array storing all the Road-Side-Units in this region. */
    private RSU[] rsus_ = new RSU[0];	// This has a little bit overhead while loading compared to an ArrayList but requires less memory and is faster when iterating

    public ArrayList<String> xxx = new ArrayList<String>();
    public ArrayList<String> yyy = new ArrayList<String>();
    public ArrayList<String> nnn = new ArrayList<String>();


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

        HashMap map = new HashMap<String, String>();
        map.put("x",String.valueOf(x_));
        map.put("y",String.valueOf(y_));
        map.put("leftBoundary",String.valueOf(leftBoundary_));
        map.put("rightBoundary",String.valueOf(rightBoundary_));
        map.put("upperBoundary",String.valueOf(upperBoundary_));
        map.put("lowerBoundary",String.valueOf(lowerBoundary_));

        Debug.debugInfo(map, Debug.ISLOGGED);

    }

    /**
     * Function to add a node to this region.
     *
     * @param node 		the node to add
     * @param doCheck 	<code>true</code> if a check should be made if this node already exists; else <code>false</code> to skip the test
     *
     * @return the node added (might be different from <code>node</code> if it already existed and <code>check</code> was true)
     * 回傳已添加新節點(Node)
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
        /** System.arraycopy(來源陣列，起始索引值，目的陣列，起始索引值，複製長度); */
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

        Debug.callFunctionInfo(this.getClass().getName(),"calculateJunctions()",Debug.ISLOGGED);

        if(Renderer.getInstance().isAutoAddMixZones()) mixZoneNodes_ = new Node[0];

        for(int i = 0; i < nodes_.length; ++i){

            /** 注意calculateJunction（）未實作 */
            nodes_[i].calculateJunction();

            /** 待新增
             *  於2017/11/14_2344新增
             * */
            //Mix zones are only added if autoAddMixZones is activated
            if(Renderer.getInstance().isAutoAddMixZones()){
                if(nodes_[i].getJunction() != null){
                    Node[] newArray = new Node[mixZoneNodes_.length+1];
                    System.arraycopy (mixZoneNodes_,0,newArray,0,mixZoneNodes_.length);
                    newArray[mixZoneNodes_.length] = nodes_[i];
                    nodes_[i].setMixZoneRadius(Vehicle.getMixZoneRadius());
                    mixZoneNodes_ = newArray;
                    if(Vehicle.isEncryptedBeaconsInMix_()){
                        RSU tmpRSU = new RSU(nodes_[i].getX(),nodes_[i].getY(), Vehicle.getMixZoneRadius(), true);
                        Map.getInstance().addRSU(tmpRSU);
                        nodes_[i].setEncryptedRSU_(tmpRSU);
                    }
                }
            }
            /** 待實作 getJunction（）方法 */
            if(nodes_[i].getJunction() != null && nodes_[i].getJunction().getNode().getTrafficLight_() == null && nodes_[i].isHasTrafficSignal_())
                new TrafficLight(nodes_[i].getJunction());

        }
        /** 待實作prepareLogs（）方法
         *  於 2017/11/15_0014 實作完成
         * */
        prepareLogs(nodes_);
    }

    /**
     * Method to prepare the log files. Calculates all intersections beetween streets and mix-zones
     */
    public void prepareLogs(Node[] nodes){
        String coordinates[] = null;

        Node node = null;
        for(int k = 0; k < nodes.length; ++k){
            node = nodes[k];

            if(node.getMixZoneRadius()>0){
                coordinates = getIntersectionPoints(node, this);


                if(coordinates != null){
                    String[] xxx2 = coordinates[0].split(":");
                    String[] yyy2 = coordinates[1].split(":");

                    for(int i = 5; i < xxx2.length;i++){
                        xxx.add(xxx2[i]);
                        yyy.add(yyy2[i]);
                        nnn.add("" + (i-4));
                    }
                }


                for(String s:coordinates) PrivacyLogWriter.log(s);
            }
        }
    }

    public String[] getIntersectionPoints(Node mixNode, Region region12) {
        Region[][] regions = Map.getInstance().getRegions();

        String[] returnArray = new String[2];
        returnArray[0] = "Mix-Zone(x):Node ID:" + mixNode.getNodeID() + ":Radius:" + mixNode.getMixZoneRadius();
        returnArray[1] = "Mix-Zone(y):Node ID:" + mixNode.getNodeID() + ":Radius:" + mixNode.getMixZoneRadius();

        //we need to check all streets:
        Street[] streets;
        Street street;

        double y1 = -1;
        double x1 = -1;
        double y2 = -1;
        double x2 = -1;
        double m = -1;
        double t = -1;

        double xNode = -1;
        double yNode = -1;
        double r = -1;

        double result = -1;
        double result1 = -1;
        double result2 = -1;

        //blacklist to avoid double values on two lane Motorways
        ArrayList<Street> blackList = new ArrayList<Street>();
        ArrayList<Street> blackList2 = new ArrayList<Street>();
        boolean blackListed = false;
        boolean blackListed2 = false;

        for(int i = 0; i < regions.length; i++){
            for(int j = 0; j < regions[i].length;j++){


                streets = regions[i][j].getStreets();
                for(int k = 0; k < streets.length; k++){
                    street = streets[k];
                    blackListed = false;
                    blackListed2 = false;
                    if(street.getLanesCount() > 1){
                        if(blackList.contains(street)){
                            blackListed = true;
                        }
                        if(!blackListed) blackList.add(street);
                    }

                    if(blackList2.contains(street)){
                        blackListed2 = true;
                    }
                    if(!blackListed2) blackList2.add(street);

                    if(!blackListed && !blackListed2){
                        //now let's do some magic
                        y1 = street.getEndNode().getY();
                        x1 = street.getEndNode().getX();
                        y2 = street.getStartNode().getY();
                        x2 = street.getStartNode().getX();
                        xNode = mixNode.getX();
                        yNode = mixNode.getY();

                        m = ((y1-y2)/(x1-x2));

                        t = y1 - (m*x1);

                        r = mixNode.getMixZoneRadius();

                        //no solution
                        if(street.getName().equals("aaaa")) System.out.println("wuhuhahahah" + (-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t));
                        if(street.getName().equals("aaaa")) System.out.println("wuhuhahahah2" + yNode + " " + xNode + " " + m + " " + r + " " + t);


                        if((-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t) < 0){

                        }
                        //two solution
                        else if((-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t) > 0){

                            result1 = (xNode + yNode*m - m*t - Math.sqrt(-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t))/(1 + m*m);
                            result2 = (xNode + yNode*m - m*t + Math.sqrt(-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t))/(1 + m*m);

                            double dx1= xNode - x1;
                            double dy1 = yNode - y1;
                            double distanceSquared1 = dx1 * dx1 + dy1 * dy1;

                            double dx2= xNode - x2;
                            double dy2 = yNode - y2;
                            double distanceSquared2 = dx2 * dx2 + dy2 * dy2;
                            if(street.getName().equals("aaaa")) System.out.println("distancesqu1alex" + distanceSquared1);
                            if(street.getName().equals("aaaa")) System.out.println("distancesqu1alex" + distanceSquared2);


                            if(street.getName().equals("aaaa")) System.out.println("da bin ich noch");

                            if((result1 >= x1 && result1 <= x2) || (result1 <= x1 && result1 >= x2)){
                                returnArray[0] += ":" + String.valueOf((int)result1);
                                returnArray[1] += ":" + String.valueOf((int)((m*result1 + t)));
                            }


                            if((result2 >= x1 && result2 <= x2) || (result2 <= x1 && result2 >= x2)){
                                returnArray[0] += ":" + String.valueOf((int)result2);
                                returnArray[1] += ":" + String.valueOf((int)((m*result2 + t)));
                            }
                        }

                        //one solutions

                        else if((-yNode*yNode + 2*xNode*yNode*m - xNode*xNode*m*m + r*r + m*m*r*r + 2*yNode*t - 2*xNode*m*t - t*t) == 0){

                            result = (xNode + yNode*m - m*t)/(1 + m*m);

                            double dx1= xNode - x1;
                            double dy1 = yNode - y1;
                            double distanceSquared1 = dx1 * dx1 + dy1 * dy1;

                            double dx2= xNode - x2;
                            double dy2 = yNode - y2;
                            double distanceSquared2 = dx2 * dx2 + dy2 * dy2;

                            if(((distanceSquared1 <= r*r) || (distanceSquared2 <= r*r)) && ((distanceSquared1 > r*r) || (distanceSquared2 > r*r)) ){

                                returnArray[0] += ":" + String.valueOf((int)result);
                                returnArray[1] += ":" + String.valueOf((int)(((m*result + t))));

                            }

                        }

                    }
                }
            }
        }


        return returnArray;

    }

    /**
     * Checks all streets in this region for possible bridges.
     */
    public void checkStreetsForBridges(){
        if(streets_.length > 0){
            Street firstStreet, secondStreet;
            int color1, color2, size = streets_.length;
            for(int i = 0; i < size; ++i){
                firstStreet = streets_[i];
                for(int j = i+1; j < size; ++j){
                    secondStreet = streets_[j];
                    color1 = firstStreet.getDisplayColor().getRGB();
                    color2 = secondStreet.getDisplayColor().getRGB();
                    //check to which street we should add the bridge
                    if(color1 != color2){
                        if(color1 < color2) MapHelper.calculateBridges(secondStreet, firstStreet);
                        else MapHelper.calculateBridges(firstStreet, secondStreet);
                    } else {
                        if(firstStreet.getBridgePaintLines() != null || firstStreet.getBridgePaintPolygons() != null) MapHelper.calculateBridges(firstStreet, secondStreet);	//add bridge to street which already has a bridge
                        else if(secondStreet.getBridgePaintLines() != null || secondStreet.getBridgePaintPolygons() != null) MapHelper.calculateBridges(secondStreet, firstStreet);
                        else if(firstStreet.getSpeed() > secondStreet.getSpeed()) MapHelper.calculateBridges(secondStreet, firstStreet);		//decide on speed
                        else MapHelper.calculateBridges(firstStreet, secondStreet);
                    }
                }
            }
        }
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
     * Returns all mix zone nodes in this region.
     *
     * @return an array containing all nodes
     */
    public Node[] getMixZoneNodes(){
        return mixZoneNodes_;
    }

    /**
     * Returns all nodes in this region.
     *
     * @return an array containing all nodes
     */
    public Node[] getNodes(){
        return nodes_;
    }

    /**
     * Returns all streets in this region.
     *
     * @return an array containing all streets
     */
    public Street[] getStreets(){
        return streets_;
    }

    /**
     * Returns all Road-Side-Units in this region.
     *
     * @return an array containing all RSUs
     */
    public RSU[] getRSUs() {
        return rsus_;
    }

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
     * Function to add a Road-Side-Units to this region.
     *
     * @param rsu the RSU to add
     *
     */
    public void addRSU(RSU rsu){
        RSU[] newArray = new RSU[rsus_.length+1];
        System.arraycopy (rsus_,0,newArray,0,rsus_.length);
        newArray[rsus_.length] = rsu;
        rsus_ = newArray;
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
