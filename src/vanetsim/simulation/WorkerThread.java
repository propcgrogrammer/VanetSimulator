package vanetsim.simulation;


import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.localization.Messages;
import vanetsim.map.Region;

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
    /** An array holding all regions this thread is working on. */
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



}
