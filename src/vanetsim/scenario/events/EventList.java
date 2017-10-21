package vanetsim.scenario.events;

import vanetsim.debug.Debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;


/**
 * This class stores all events.
 */
public final class EventList {

	/** The only instance of this class (singleton). */
	private static final EventList INSTANCE = new EventList();



	/**
	 * Empty, private constructor in order to disable instancing.
	 */
	private EventList(){
		Debug.whereru(this.getClass().getName(), true);
		Debug.callFunctionInfo(this.getClass().getName(), "EventList()", true);
		
	}	

	/**
	 * Gets the single instance of this EventList.
	 * 
	 * @return single instance of this EventList
	 */
	public static EventList getInstance(){
		return INSTANCE;
	}

	/**
	 * Process next event(s).
	 *
	 * @param time the current absolute time of the simulation
	 */
	public void processEvents(int time){


	}
}