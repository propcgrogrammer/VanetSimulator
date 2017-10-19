package vanetsim.map;

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

    /** The position on the x axis (in relation to all other regions => does not correspond to map coordinates!). */
    private final int x_;

    /** The position on the y axis (in relation to all other regions => does not correspond to map coordinates!). */
    private final int y_;

    /** The coordinate representing the left boundary of this region */
    private final int leftBoundary_;

    /** The coordinate representing the right boundary of this region */
    private final int rightBoundary_;

    /** The coordinate representing the upper boundary of this region */
    private final int upperBoundary_;

    /** The coordinate representing the lower boundary of this region */
    private final int lowerBoundary_;


    /**
     *  vehicle變數
     */
    /** <code>true</code> to indicate that the vehicles have changed since the last call to getVehicleArray() */
    private boolean vehiclesDirty_ = true;

    /** An <code>ArrayList</code> storing all vehicles in this region. */
    private ArrayList<Vehicle> vehicles_;	// changes relatively often so use ArrayList here

    /** The simulation requests an array for the vehicles which is cached here. */
    private Vehicle[] vehiclesArray_;


    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */
    /**
     * /////////// setter & getter (start) ///////////
     */

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
