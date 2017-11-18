package vanetsim.simulation;


import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.localization.Messages;
import vanetsim.map.Region;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Vehicle;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.CyclicBarrier;

/**
 * This thread is meant to run parallel with multiple others to gain advantage of multiple CPUs.
 * All simulation tasks are initiated from this class!
 */
public class WorkerThread extends Thread {


    /**
     * ////////////////////////////
     * //    instance variable
     * ////////////////////////////
     */
    /** An array holding all regions this thread is working on. 該執行緒所管理的Region區域*/
    private final Region[] ourRegions_;

    /** The available time in milliseconds to render in one step. This also determines how far a car moves in one time tick. */
    private final int timePerStep_;

    /** The changed regions that need to be updated before doing the next step. */
    private final LinkedHashSet<Integer> changedRegions_ = new LinkedHashSet<Integer>(16);

    /** The <code>CyclicBarrier</code> called to schedule start of new work. */
    private CyclicBarrier barrierStart_;

    /** The <code>CyclicBarrier</code> called during the work steps. */
    private CyclicBarrier barrierDuringWork_;

    /** The <code>CyclicBarrier</code> called after performing all tasks. */
    private CyclicBarrier barrierFinish_;


    /**
     * ////////////////////////////
     * //    method
     * ////////////////////////////
     */
    /**
     * The main constructor for the worker thread. Don't use any other constructor inherited from the
     * Thread class as all parameters are essential!
     *
     * @param ourRegions the regions which are assigned to us.
     * @param timePerStep the time in milliseconds for one step
     */
    public WorkerThread(Region[] ourRegions, int timePerStep){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "WorkerThread(Region[] ourRegions, int timePerStep)", Debug.ISLOGGED);

        setName("Worker startX:" + ourRegions[0].getX() + " startY:" + + ourRegions[0].getY()); //$NON-NLS-1$ //$NON-NLS-2$

        Debug.ThreadInfo(this,Debug.ISLOGGED);
        ourRegions_ = ourRegions;
        timePerStep_ = timePerStep;
        ErrorLog.log(Messages.getString("WorkerThread.workerCreated") + ourRegions_.length + Messages.getString("WorkerThread.regions"), 1, this.getName(), "Worker constructor", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    /**
     * Set <code>CyclicBarriers</code> for thread synchronization.
     * 設定主程式模擬個階段（執行前/執行期間/執行後）所需的同步執行緒數量，由SimulationMaster:createWorkers()呼叫
     * @param barrierStart 				the barrier for starting
     * @param barrierDuringWork	the barrier after adjusting the speed
     * @param barrierFinish				the barrier after completing all tasks
     */
    public void setBarriers(CyclicBarrier barrierStart, CyclicBarrier barrierDuringWork, CyclicBarrier barrierFinish){
        Debug.callFunctionInfo(this.getClass().getName(), "setBarriers(CyclicBarrier barrierStart, CyclicBarrier barrierDuringWork, CyclicBarrier barrierFinish)", Debug.ISLOGGED);
        barrierStart_ = barrierStart;
        barrierDuringWork_ = barrierDuringWork;
        barrierFinish_ = barrierFinish;
    }

    /**
     * Adds a region to the list of changed region (so that it gets updated).
     *
     * @param i	the number of the region in this thread
     */
    public void addChangedRegion(int i){
        synchronized(changedRegions_){
            changedRegions_.add(Integer.valueOf(i));
        }
    }


    /**
     * The main method. All simulation is initiated from here!
     */
    public void run() {

        int i, j, length;
        int ourRegionsLength = ourRegions_.length;
        // An array copy of the vehicles instead of an ArrayList or something else is used because of three reasons:
        // 1. While iterating through all regions, a ConcurrentModificationExceptions is thrown when a vehicle gets into a new region.
        // 2. As we need the array anyways because of (1), we can use it cached for the other actions as well!
        // 3. Converting an ArrayList to an array is quite fast - it's basically a System.arraycopy()-operation which is made
        //    through a very fast system-memcpy(). The overhead for array construction is by far less than the overhead
        //    caused by working with iterators (if there are lots of vehicles)!

        Vehicle[][] vehicles = new Vehicle[ourRegionsLength][];
        Vehicle[] vehicleSubarray;	// it is better to cache lookups in the double-array.
        Vehicle vehicle;

        long tmpTimePassed = 999999999;
        long tmpTimePassedSaved = 99999999;
        int silentPeriodDuration = Vehicle.getTIME_OF_SILENT_PERIODS();
        int silentPeriodFrequency = Vehicle.getTIME_BETWEEN_SILENT_PERIODS();

        RSU[][] rsus = new RSU[ourRegionsLength][];
        RSU[] rsuSubarray;	// it is better to cache lookups in the double-array.
        RSU rsu;

        Iterator<Integer> changedRegionIterator;
        int tmp;

        for(i = 0; i < ourRegionsLength; ++i){
            ourRegions_[i].createBacklink(this, i);
            ourRegions_[i].calculateJunctions();	//recalculate because user might have edited map after loading
            vehicles[i] = ourRegions_[i].getVehicleArray();

            rsus[i] = ourRegions_[i].getRSUs();
        }

        boolean communicationEnabled = Vehicle.getCommunicationEnabled();
        boolean beaconsEnabled = Vehicle.getBeaconsEnabled();
        boolean recyclingEnabled = Vehicle.getRecyclingEnabled();

        //sleep if no barriers have been set yet
        while (barrierStart_ == null || barrierDuringWork_ == null || barrierFinish_ == null){
            try{
                sleep(50);
            } catch (Exception e){}
        }
        /**  ------------- 2017/11/19_0629 debug 至此行 ---------------------- */




    }



}
