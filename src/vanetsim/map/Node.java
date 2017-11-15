package vanetsim.map;


import vanetsim.scenario.RSU;

/**
 * A node on the map.
 */
public class Node {

    /** A common counter to generate unique IDs */
    private static int counter_ = 0;

    /** A unique ID for this node */
    private final int nodeID_;

    /** The x coordinate. */
    private int x_;   public int getNodeX(){return x_;}

    /** The y coordinate. */
    private int y_;	  public int getNodeY(){return y_;}

    /** Saves the mix zone radius if this node includes a mix zone */
    private int mixZoneRadius_ = 0;

    /** Saves the RSU if encrypted beacons are activated */
    private RSU encryptedRSU_ = null;

    /** Holds the junction associated with this node or <code>null</code> if this is not a junction. */
    private Junction junction_ = null;

    /** flag if the node has a traffic signal */
    private boolean hasTrafficSignal_;   public boolean isHasTrafficSignal(){return hasTrafficSignal_;}

    /** An array containing all streets going out from this node. */
    private Street[] outgoingStreets_ = new Street[0];		// only needed for storing/iterating of a small amount of streets so an array is by far the fastest solution!

    /** An array containing all streets coming into or going out from this node. */
    private Street[] crossingStreets_ = new Street[0];

    /** The region in which this node is. */
    private Region region_;

    /** Traffic Light*/
    private TrafficLight trafficLight_ = null;

    /** Traffic Light Collections */
    private int[] streetHasException_ = null;

    /**
     * Instantiates a new node.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param hasTrafficSignal signals if node has a traffic signal
     */
    public Node(int x, int y, boolean hasTrafficSignal) {
        x_ = x;
        y_ = y;
        hasTrafficSignal_ = hasTrafficSignal;
        nodeID_ = counter_;
        ++counter_;
    }

    /**
     * ------------- Junction 相關 ---------------
     * */

    /**
     * Calculates if this is a junction and the priorities of all possible ways which go over this junction.
     */
    public void calculateJunction(){
        /** 待新增 */
        /** 在該Node區塊內，若小於3個進出該Node的Street，代表沒有相交叉 */
        if(crossingStreets_.length < 3) junction_ = null;	//if only 2 incomings, it's a street which is just continuing (or the end of a street)!

    }

    /**
     * Returns the junction object associated with this node or <code>null</code> if this is not a junction.
     *
     * @return the junction or <code>null</code> if this is not a junction
     */
    public Junction getJunction(){
        return junction_;
    }

    /**
     * ------------- Junction 相關 ---------------
     * */





    /**
     * ------------- Street 相關 ---------------
     * */

    /**
     * Check if a Traffic Signal has non-default settings
     *
     * @return true if settings are non-default
     */
    public boolean hasNonDefaultSettings() {
        if(streetHasException_ == null) return false;

        boolean tmpReturn = false;

        for(int i : streetHasException_) if(i != 1) tmpReturn = true;

        return tmpReturn;
    }

    /**
     * Gets the number of streets crossing in this node.
     *
     * @return the amount of streets
     */
    public int getCrossingStreetsCount() {
        return crossingStreets_.length;
    }

    /**
     * Gets an array of the outgoing streets of this node. You will always get an array (never <code>null</code>)
     * but it might have zero size.
     *
     * @return the array
     */
    public Street[] getOutgoingStreets() {
        return outgoingStreets_;
    }


    /**
     * Adds an outgoing street. If the array already contains the street, nothing is done.
     * Note that this operation is not thread-safe.
     *
     * @param street The outgoing street to add.
     */
    public void addOutgoingStreet(Street street) {
        boolean found = false;
        for(int i = 0; i < outgoingStreets_.length; ++i){
            if(outgoingStreets_[i] == street){
                found = true;
                break;
            }
        }
        if(!found){
            Street[] newArray = new Street[outgoingStreets_.length+1];
            System.arraycopy (outgoingStreets_,0,newArray,0,outgoingStreets_.length);
            newArray[outgoingStreets_.length] = street;
            outgoingStreets_ = newArray;
        }
    }

    /**
     * Adds a crossing street. If the array already contains the street, nothing is done.
     * Note that this operation is not thread-safe.
     *
     * @param street	the crossing street to add.
     */
    public void addCrossingStreet(Street street) {
        boolean found = false;
        for(int i = 0; i < crossingStreets_.length; ++i){
            if(crossingStreets_[i] == street){
                found = true;
                break;
            }
        }
        if(!found){
            Street[] newArray = new Street[crossingStreets_.length+1];
            System.arraycopy (crossingStreets_,0,newArray,0,crossingStreets_.length);
            newArray[crossingStreets_.length] = street;
            crossingStreets_ = newArray;
        }
    }

    /**
     * ------------- Street 相關 ---------------
     * */



    /**
     * ------------- TrafficLight 相關 ---------------
     * */


    public boolean isHasTrafficSignal_() {
        return hasTrafficSignal_;
    }

    /**
     * @return the trafficLight_
     */
    public TrafficLight getTrafficLight_() {
        return trafficLight_;
    }

    /**
     * Fill Exception Array of a String.
     */
    public void addSignalExceptionsOfString(String arrayString) {
        String[] tmpArray = arrayString.split(":");
        streetHasException_ = new int[tmpArray.length];

        for(int i = 0; i < tmpArray.length; i++) streetHasException_[i] = Integer.parseInt(tmpArray[i]);
    }

    /**
     * Write exception array in one string. Please check if Signal has exceptions before using.
     *
     * @return string with exceptions
     */
    public String getSignalExceptionsInString() {
        String tmpReturn = "";

        for(int i : streetHasException_) tmpReturn += i + ":";

        if(tmpReturn.length() > 0) tmpReturn = tmpReturn.substring(0, tmpReturn.length() - 1);

        return tmpReturn;
    }

    /**
     * ------------- TrafficLight 相關 ---------------
     * */











    /**
     * The mixZoneRadius
     *
     * @return the max zone radius
     */
    public int getMixZoneRadius() {
        return mixZoneRadius_;
    }

    /**
     * Returns the unique ID of this node.
     *
     * @return an integer
     */
    public int getNodeID(){
        return nodeID_;
    }

    /**
     * Gets the region in which this node is found.
     *
     * @return the region
     */
    public Region getRegion() {
        return region_;
    }




    public void setEncryptedRSU_(RSU encryptedRSU_) {
        this.encryptedRSU_ = encryptedRSU_;
    }

    /**
     * Sets the mix zone radius
     *
     * @param mixZoneRadius		the new mix zone radius
     */
    public void setMixZoneRadius(int mixZoneRadius) {
        mixZoneRadius_ = mixZoneRadius;
    }

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate
     */
    public int getX() {
        return x_;
    }

    /**
     * Sets the x coordinate
     *
     * @param x	the new coordinate
     */
    public void setX(int x) {
        x_ = x;
    }
    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate
     */
    public int getY() {
        return y_;
    }

    /**
     * Sets the y coordinate
     *
     * @param y	the new coordinate
     */
    public void setY(int y) {
        y_ = y;
    }

    public RSU getEncryptedRSU_() {
        return encryptedRSU_;
    }



    /**
     * Sets the region in which this node is found.
     *
     * @param region the region
     */
    public void setRegion(Region region) {
        region_ = region;
    }

    /**
     * Gets the number of streets going out from this node.
     *
     * @return the amount of streets
     */
    public int getOutgoingStreetsCount() {
        return outgoingStreets_.length;
    }


    /**
     * The maximum ID a node has.
     *
     * @return the maximum ID
     */
    public static int getMaxNodeID(){
        return counter_;
    }

}
