package vanetsim.scenario;

import vanetsim.gui.Renderer;
import vanetsim.map.Street;

/**
 * Stores all known penalties for streets. The streets are stored together with their direction and a penalty
 * in cm. Arrays are directly used here (in contrast to the KnownVehiclesList) as this allows easier and faster
 * usage in the routing algorithm. Extensibility is not a major concern here.
 * <br><br>
 * Note for developers: You need to make sure, that all used arrays always have the same size!
 */
public class KnownPenalties{

	/** The vehicle this data structure belongs to. */
	private final Vehicle vehicle_;

	/** The streets which have penalties. */
	private Street[] streets_;

	/** An array with directions corresponding to the streets. <code>1</code> means from endNode to startNode, 
	 * <code>0</code> means both directions and <code>-1</code> means from startNode to endNode */
	private int[] directions_;

	/** The penalties values. Stored in cm */
	private int[] penalties_ ;

	/** How long this entry will be valid. Measured in milliseconds from simulation start. */
	private int[] validUntil_;
	
	/** An array to store if a route update is necessary if this route is removed */
	private boolean[] routeUpdateNecessary_;

	/** The current size of the list. */
	private int size = 0;

	/**
	 * Constructor.
	 * 
	 * @param vehicle	the vehicle this data structure belongs to.
	 */
	public KnownPenalties(Vehicle vehicle){
		vehicle_ = vehicle;
		// just presize so that resizing isn't needed that often
		streets_ = new Street[2];
		directions_ = new int[2];
		penalties_ = new int[2];
		validUntil_ = new int[2];
		routeUpdateNecessary_ = new boolean[2];
	}

	/**
	 * Updates or adds a penalty. If a penalty already existed, the values for penalty and validUntil are
	 * overwritten! If the penalty is new or differs from the last one, a new route calculation is initiated.
	 * 
	 * @param street 		the street
	 * @param direction 	the direction. <code>1</code> means from endNode to startNode, <code>0</code> means
	 * 						both directions and <code>-1</code> means from startNode to endNode
	 * @param penalty		the penalty in cm
	 * @param validUntil	how long this entry will be valid. Measured in millseconds from simulation start
	 */
	public synchronized void updatePenalty(Street street, int direction, int penalty, int validUntil){
		/** 待增加 */
	}

	/**
	 * Check for outdated entries and remove them. Note that this function is not synchronized! You need to make
	 * sure that no other thread uses any function on this object at the same time!
	 */
	public void checkValidUntil(){
		/** 待增加 */
	}
	/**
	 * Gets all streets with known penalties.
	 * 
	 * @return an array with all streets
	 */

	public Street[] getStreets(){
		return streets_;
	}
	/**
	 * Gets an array with the directions corresponding to the getStreets()-function.
	 * <code>1</code> in the array means from endNode to startNode, <code>0</code> means
	 * both directions and <code>-1</code> means from startNode to endNode
	 * 
	 * @return an array with all directions
	 */

	public int[] getDirections(){
		return directions_;
	}

	/**
	 * Gets an array with the penalties corresponding to the getStreets()-function.
	 * Measured in cm. 
	 * 
	 * @return an array with all penalties
	 */
	public int[] getPenalties(){
		return penalties_;
	}

	/**
	 * Gets the amount of known penalties stored.
	 * 
	 * @return the size
	 */
	public int getSize(){
		return size;
	}

	/**
	 * Clears everything from this data structure.
	 */
	public void clear(){
		streets_ = new Street[2];	// just presize so that resizing isn't needed that often
		directions_ = new int[2];
		penalties_ = new int[2];
		validUntil_ = new int[2];
		routeUpdateNecessary_ = new boolean[2];
		size = 0;
	}
}