package vanetsim.map;


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

    /** flag if the node has a traffic signal */
    private boolean hasTrafficSignal_;   public boolean isHasTrafficSignal(){return hasTrafficSignal_;}

    /** An array containing all streets going out from this node. */
    private Street[] outgoingStreets_ = new Street[0];		// only needed for storing/iterating of a small amount of streets so an array is by far the fastest solution!

    /** An array containing all streets coming into or going out from this node. */
    private Street[] crossingStreets_ = new Street[0];

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
     * Returns the unique ID of this node.
     *
     * @return an integer
     */
    public int getNodeID(){
        return nodeID_;
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
