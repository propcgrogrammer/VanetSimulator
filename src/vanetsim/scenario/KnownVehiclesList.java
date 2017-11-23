package vanetsim.scenario;

import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.AttackLogWriter;

/**
 * A list of all known vehicles which was discovered through beacons. In contrast to the KnownPenalties-
 * class, an own class is used for storing the information about the vehicles. Although this means slightly
 * more overhead, it should not be a big case and allows better extensibility.<br>
 * A simple hash algorithm based on the vehicle ID is used to get better performance. The hash determines the 
 * corresponding linked list(beginnings of linked lists are found in <code>head_</code>). Known vehicles with the
 * same hash are connected together through their <code>next_</code> and <code>previous_</code> values (see 
 * KnownVehicle-class).
 */
public class KnownVehiclesList{

	/** How long the timeout is in milliseconds. If a vehicle wasn't updated for this time, 
	 * it is dropped from the list! */
	private static final int VALID_TIME = 1000;

	/** How many hash buckets will be used. Increase if you expect lots of known vehicles! */
	private static final int HASH_SIZE = 32;

	/** How much time has passed since beginning of the simulation. Stored here as it's really needed often. */
	private static int timePassed_ = 0;



	/** The amount of items stored. */
	private int size_ = 0;




	/**
	 * Gets the amount of known vehicles stored.
	 *
	 * @return the size
	 */
	public int getSize(){
		return size_;
	}

	/**
	 * Sets the time passed since simulation start.
	 *
	 * @param time the new time in milliseconds
	 */
	public static void setTimePassed(int time){
		timePassed_ = time;
	}


}