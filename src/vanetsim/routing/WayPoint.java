package vanetsim.routing;

import java.text.ParseException;

import vanetsim.debug.Debug;
import vanetsim.localization.Messages;
import vanetsim.map.MapHelper;
import vanetsim.map.Street;

/**
 * A waypoint used for routing.
 */
public class WayPoint{

    /** The x coordinate. */
    private  int x_;

    /** The y coordinate. */
    private  int y_;

    /** The time to wait BEFORE movement to the next waypoint starts. */
    private int waitTime_;

    /** The street on which this waypoint is located. */
    private  Street street_;

    /** The position on the street measured from the StartNode in cm. */
    private  double positionOnStreet_;


    /**
     * Instantiates a new waypoint.
     *
     * @param x			the x coordinate
     * @param y			the y coordinate
     * @param waitTime	the time to wait BEFORE movement to the next waypoint starts.
     *
     * @throws ParseException if the coordinates supplied couldn't be matched to a street within 100m distance.
     */
    public WayPoint(int x, int y, int waitTime) throws ParseException{
        /** 待新增 */
    }

    /**
     * Gets the position on the street.
     *
     * @return the position in cm measured from the startNode
     */
    public double getPositionOnStreet(){
        return positionOnStreet_;
    }

    /**
     * Gets the street on which this waypoint is located.
     *
     * @return the street
     */
    public Street getStreet(){
        return street_;
    }

    /**
     * Gets the time to wait BEFORE movement to the next waypoint begins.
     *
     * @return the time to wait BEFORE movement to the next waypoint starts
     */
    public int getWaittime(){
        return waitTime_;
    }

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate
     */
    public int getX(){
        return x_;
    }

    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate
     */
    public int getY(){
        return y_;
    }

    /**
     * Sets the wait time
     * @param waitTime
     */
    public void setWaittime(int waitTime){
        waitTime_ = waitTime;
    }
}