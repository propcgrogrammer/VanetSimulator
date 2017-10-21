package vanetsim.simulation;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.scenario.KnownRSUsList;
import vanetsim.scenario.KnownVehiclesList;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.events.EventList;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * This thread delegates the simulation processing to subthreads and then calls a
 * repaint on the drawing area.
 */
public class SimulationMaster extends Thread {

    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /** How much time passes in one step (in milliseconds). 40ms results in a smooth animation with 25fps. */
    public static final int TIME_PER_STEP = 40;

    /** The list with all events */
    private static final EventList eventList_ = EventList.getInstance();


    /** a flag to indicate if the performance should be logged */
    private static boolean logPerformance_ = true;

    /** a variable to save the start time */
    private static long startTime = 0;


    /** The time, one step should have in realtime. Decrease to get a faster simulation, increase to get a slower simulation. */
    private volatile int targetStepTime_ = TIME_PER_STEP;

    /** A target time to jump to */
    private volatile int jumpTimeTarget_ = -1;


    /**  模擬執行狀態（執行中/停止/單步執行）變數   */
    /** Indicates if this simulation should run. If this flag is updated to false the current simulation step
     * is finished and afterwards the simulation stops */
    private volatile boolean running_ = false;

    /** The do one step. */
    private volatile boolean doOneStep_ = false;


    /** If the mode to jump to a specific time is enabled or not. */
    private volatile boolean jumpTimeMode_ = false;

    /** An array holding all worker threads. 考慮使用ArrayList來做thread管理 */
    private WorkerThread[] workers_ = null;


    /**
     * 同步執行緒（CyclicBarrier）類物件
     */
    /** Synchronization barrier for the start of the working threads. */
    private CyclicBarrier barrierStart_ = null;

    /** Synchronization barrier for the worker threads. */
    private CyclicBarrier barrierDuringWork_ = null;

    /** Synchronization barrier for the end of one step in the working process. */
    private CyclicBarrier barrierFinish_ = null;



    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */


    /**
     * Function to set up the worker threads with their corresponding regions. Each thread gets an equal amount of regions
     * (some might get one more because of rounding). The amount of items in a region is not used as a method to improve equality
     * between threads as this value might change over time (moving vehicles!).
     * This function expects to get 2x the amount of CPU cores as threads so that the negative effects of an unequal allocation
     * (some threads finishing faster as others => unused cpu power) are reduced.
     *
     * @param timePerStep	the time per step in milliseconds
     * @param threads		the amount of threads that shall be created  執行緒數量
     *
     * @return the worker thread array
     */
    public WorkerThread[] createWorkers(int timePerStep, int threads){



        Debug.callFunctionInfo(this.getClass().getName(), "createWorkers(int timePerStep, int threads)", Debug.ISLOGGED);

        Debug.detailedInfo("use ArrayList to store WorkerThread", Debug.ISLOGGED);

        /** 利用ArrayList來儲存WorkerThread */
        ArrayList<WorkerThread> tmpWorkers = new ArrayList<WorkerThread>();
        WorkerThread tmpWorker = null;

        Debug.detailedInfo("use Region[][] to store regions", Debug.ISLOGGED);

        /**  取得regions陣列物件，初次呼叫時為null */
        Region[][] regions = Map.getInstance().getRegions();

        System.out.println("DEBUG : regions =>"+regions);

        ArrayList<Region> tmpRegions = new ArrayList<Region>();

        Debug.detailedInfo("get regionCountX/regionCountY from Map", Debug.ISLOGGED);

        long regionCountX = Map.getInstance().getRegionCountX();
        long regionCountY = Map.getInstance().getRegionCountY();

        /** 每個執行緒管理幾個區域 */
        double regionsPerThread = regionCountX * regionCountY / (double)threads;
        System.out.println("DEBUG : regionsPerThread =>"+regionsPerThread);

        /** Debug檢查用 */
        /** ============================================================= */
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("regionCountX", String.valueOf(regionCountX));
        map.put("regionCountY", String.valueOf(regionCountY));
        map.put("threads", String.valueOf(threads));
        map.put("regionsPerThread", String.valueOf(regionsPerThread));

        Debug.getInstance().debugInfo(map , Debug.ISLOGGED);

        /** ============================================================= */


        long count = 0;
        double target = regionsPerThread;
        threads = 0;	// reset to 0, perhaps we're getting more/less because of rounding so we calculate this later!

        for(int i = 0; i < regionCountX; ++i){
            for(int j = 0; j < regionCountY; ++j){

                /** Debug檢查用 */
                /** ============================================================= */
                map.clear();
                map.put("i",String.valueOf(i));
                map.put("j",String.valueOf(j));
                map.put("threads",String.valueOf(threads));
                map.put("count", String.valueOf(count));

                /** ============================================================= */

                /** 計算目前計算到該地圖的哪一個區塊 */
                ++count;

                /** 將地圖每個區域儲存至 tmpRegions 的 ArrayList*/
                tmpRegions.add(regions[i][j]);

                if(count >= Math.round(target)){  /** Math.round() 取target的四捨五入數值 */


                    Debug.detailedInfo("when count >= Math.round(target)", Debug.ISLOGGED);
                    map.put("Math.round(target)", String.valueOf(Math.round(target)));
                    Debug.getInstance().debugInfo(map, Debug.ISLOGGED);



                    try{
                        Debug.detailedInfo("creating WorkerThread and puting the thread into the ArrayList", Debug.ISLOGGED);

                        /**
                         *  將ArrayList轉換為Array (ArrayList<Region> --> Region[])
                         *  <code>
                         *      ArrayList al = new ArrayList();
                         *                al.add("a");
                         *                al.add("b");
                         *                al.add("c");
                         *      String a[] = (String[]) al.toArray(new String[0]);
                         * <code/>
                         */
                        /** 產生個別單一執行緒tmpWorker物件 for WorkerThread */
                        tmpWorker = new WorkerThread(tmpRegions.toArray(new Region[0]), timePerStep);
                        /** 計算thread執行緒數量 */
                        ++threads;
                        /** 將該執行緒儲存在ArrayList<WorkerThread>中 */
                        tmpWorkers.add(tmpWorker);
                        /** 啟動執行緒 */
                        tmpWorker.start();

                    } catch (Exception e){
                        ErrorLog.log(Messages.getString("SimulationMaster.errorWorkerThread"), 7, SimulationMaster.class.getName(), "createWorkers", e); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    /** 清空 */
                    tmpRegions = new ArrayList<Region>();
                    /** 再次累加執行緒數量，預期達到兩倍CPU核心數量 */
                    target += regionsPerThread;
                }
                else{
                    Debug.getInstance().debugInfo(map, Debug.ISLOGGED);
                }
            }
        }
        if(tmpRegions.size() > 0){	// remaining items, normally this should never happen!
            ErrorLog.log(Messages.getString("SimulationMaster.regionsRemained"), 6, SimulationMaster.class.getName(), "createWorkers", null); //$NON-NLS-1$ //$NON-NLS-2$
            try{
                tmpWorker = new WorkerThread(tmpRegions.toArray(new Region[0]), timePerStep);
                ++threads;
                tmpWorkers.add(tmpWorker);
                tmpWorker.start();
            } catch (Exception e){
                ErrorLog.log(Messages.getString("SimulationMaster.errorAddingRemainingRegions"), 7, SimulationMaster.class.getName(), "createWorkers", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        Debug.detailedInfo("barrierStart_ hold (threads + 1) threads", Debug.ISLOGGED);

        barrierStart_ = new CyclicBarrier(threads + 1);

        Debug.detailedInfo("barrierDuringWork_ hold (threads) threads", Debug.ISLOGGED);

        barrierDuringWork_ = new CyclicBarrier(threads);

        Debug.detailedInfo("barrierFinish_ hold (threads + 1) threads", Debug.ISLOGGED);

        barrierFinish_ = new CyclicBarrier(threads + 1);

        Debug.detailedInfo("use Iterator<WorkerThread> to manage WorkThread", Debug.ISLOGGED);

        Iterator<WorkerThread> iterator = tmpWorkers.iterator();

        while(iterator.hasNext() ) {
            iterator.next().setBarriers(barrierStart_, barrierDuringWork_, barrierFinish_);
        }
        return tmpWorkers.toArray(new WorkerThread[0]);
    }


    /**
     * Instantiates a new simulation master.
     */
    public SimulationMaster(){
        Debug.whereru("SimulationMaster", Debug.ISLOGGED);
        Debug.debugInfo(this.getClass().getName(), "SimulationMaster()", Debug.ISLOGGED);
    }

    /**
     * Method to let this thread stop delegating work to subthreads. Work in the main function is suspended, the
     * subthreads (workers) will go to sleep and the Renderer is notified to get inactive.
     */
    public synchronized void stopThread(){

        Debug.callFunctionInfo(this.getClass().getName(), "stopThread()", Debug.ISLOGGED);
        Debug.ThreadInfo(this, Debug.ISLOGGED);

        if(running_) ErrorLog.log(Messages.getString("SimulationMaster.simulationStopped"), 2, SimulationMaster.class.getName(), "stopThread", null); //$NON-NLS-1$ //$NON-NLS-2$
        running_ = false;
        if ((Map.getInstance().getReadyState() == false || Scenario.getInstance().getReadyState() == false) && workers_ != null){
            //wait till all workers get to the start barrier
            while(barrierStart_.getParties() - barrierStart_.getNumberWaiting() != 1){
                try{
                    sleep(1);
                } catch (Exception e){}
            }
            //now interrupt the first one. The first will exit with an InterruptedException, all other workers will exit through a BrokenBarrierException
            workers_[0].interrupt();

            workers_ = null;
        }

        Debug.detailedInfo("notify renderer to stop the simulation", Debug.ISLOGGED);

        Renderer.getInstance().notifySimulationRunning(false);
    }

    /**
     * The main method for the simulation master initializes the worker threads, manages them and
     * initiates the render process and statistics updates.
     *
     * SimulationMaster 執行緒開始執行點 由VanetSimStart呼叫
     */
    public void run() {

        Debug.callFunctionInfo(this.getClass().getName(), "run()", Debug.ISLOGGED);
        Debug.ThreadInfo(this, Debug.ISLOGGED);

        setName("SimulationMaster"); //$NON-NLS-1$
        int time, threads;
        long renderTime;
        Renderer renderer = Renderer.getInstance();

        Debug.detailedInfo("CyclicBarrier ： A synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point. CyclicBarriers are useful in programs involving a fixed sized party of threads that must occasionally wait for each other. The barrier is called cyclic because it can be re-used after the waiting threads are released.", Debug.ISLOGGED);
        Debug.detailedInfo("creating CyclicBarrier 2 parties (threads) ", Debug.ISLOGGED);

        /** 使用 CyclicBarrier 執行緒可以等到SimulationMaster裡的所有執行緒到達某一狀態時再一起執行下一任務
         * 此例子為產生2個CyclicBarrier執行緒
         * */
        CyclicBarrier barrierRender = new CyclicBarrier(2);

        Debug.detailedInfo("set CyclicBarrier for Renderer", Debug.ISLOGGED);


        /** -----------------------  程式執行到此 2017/10/22_0057      --------------------- */

        /** 將barrierRender提交給Renderer做管理 */
        renderer.setBarrierForSimulationMaster(barrierRender);

        ReportingControlPanel statsPanel = null;
        if(!Renderer.getInstance().isConsoleStart()) statsPanel = VanetSimStart.getMainControlPanel().getReportingPanel();

        long timeOld = 0;
        long timeNew = 0;
        long timeDistance = 0;
        boolean consoleStart = Renderer.getInstance().isConsoleStart();


        while(true){
            try{
                if(running_ || doOneStep_){

                    Debug.detailedInfo("running on state running or doOneStep", Debug.ISLOGGED);

                    renderTime = System.nanoTime();

                    /** barrierRender 執行緒進行重設 */
                    barrierRender.reset();

                    while(workers_ == null){  /** 當執行緒還未被產生時，產生執行緒 */

                        Debug.detailedInfo("when workers_ is null", Debug.ISLOGGED);

                        if (Map.getInstance().getReadyState() == true && Scenario.getInstance().getReadyState() == true){	// wait until map is ready

                            Debug.detailedInfo("when Map and Scenario are all ready for running", Debug.ISLOGGED);

                            /** 若為單核心CPU，則產生單一執行緒 */
                            if(Runtime.getRuntime().availableProcessors() < 2) threads = 1;	// on single processor systems or if system reports wrong (smaller 1) amount of CPUs => fallback to 1 CPU and 1 thread
                            /** 若為多核心CPU，則產生多個執行緒 */
                            else threads = Runtime.getRuntime().availableProcessors() * 2;		// on multiprocessor systems use double the amount of threads to use ressources more efficiently
                            long max_heap = Runtime.getRuntime().maxMemory()/1048576;		// Heap memory in MB
                            ErrorLog.log(Messages.getString("SimulationMaster.preparingSimulation") + threads + Messages.getString("SimulationMaster.threadsDetected") + max_heap + Messages.getString("SimulationMaster.heapMemory"), 3, SimulationMaster.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            // Prepare multiple worker threads to gain advantage of multi-core processors

                            Debug.detailedInfo("use parameter TIME_PER_STEP and threads for createWorkers", Debug.ISLOGGED);


                            workers_ = createWorkers(TIME_PER_STEP, threads);

                            if(Renderer.getInstance().isConsoleStart()){
                                Renderer.getInstance().setMapZoom(0.4999999999);
                                VanetSimStart.getMainControlPanel().getSimulatePanel().setZoomValue((int)Math.round(Math.log(Renderer.getInstance().getMapZoom()*1000)*50));
                                ReRenderManager.getInstance().doReRender();
                            }


                        } else {
                            sleep(50);
                        }
                    }
                    time = renderer.getTimePassed() + TIME_PER_STEP;

                    //process events
                    eventList_.processEvents(time);

                    // (re)start the working threads
                    barrierStart_.await();

                    // wait for all working threads to finish to prevent drawing an inconsistent state!
                    barrierFinish_.await();

                    // Rendering itself can't be multithreaded and thus must be done here and not in the workers!
                    KnownVehiclesList.setTimePassed(time);
                    KnownRSUsList.setTimePassed(time);
                    renderer.setTimePassed(time);



                    if(!jumpTimeMode_){

                        renderer.ReRender(false, true);

                        statsPanel.checkUpdates(TIME_PER_STEP);

                        // wait until rendering has completed
                        Thread.yield();
                        barrierRender.await(3, TimeUnit.SECONDS);

                        // wait so that we get near the desired frames per second (no waiting if processing power wasn't enough!)
                        renderTime = ((System.nanoTime() - renderTime)/1000000);
                        if(renderTime > 0) renderTime = targetStepTime_ - renderTime;
                        else renderTime = targetStepTime_ + renderTime;	//nanoTime might overflow
                        if(renderTime > 0 && renderTime <= targetStepTime_){
                            sleep(renderTime);
                        }
                    } else {

                        if(consoleStart && time%10000 == 0){
                            timeNew = System.currentTimeMillis();
                            timeDistance = timeNew-timeOld;
                            System.out.println("Time:" + timeDistance);
                            timeOld = timeNew;
                            System.out.println(time);

                        }

                        if(time >= jumpTimeTarget_){
                            jumpTimeTarget_ = -1;
                            jumpTimeMode_ = false;
                            stopThread();
                            if(consoleStart){
                                System.out.println("Time:" + new Date());
                                System.out.println(Messages.getString("ConsoleStart.SimulationEnded"));
                                //if(logPerformance_)writeAnyTextToFile("timeForCalculation:" + (System.currentTimeMillis()-startTime) + "\n*********\n", System.getProperty("user.dir") + "/performance.log", true);

                                System.exit(0);
                            }
                            if(!consoleStart){
                                VanetSimStart.setProgressBar(false);
                                renderer.ReRender(false, true);
                                statsPanel.checkUpdates(TIME_PER_STEP);
                            }
                        }
                    }
                    if(doOneStep_){
                        doOneStep_ = false;
                        renderer.notifySimulationRunning(false);
                    }
                } else {
                    sleep(50);
                }
            } catch (Exception e){};
        }
    }

}
