package vanetsim.simulation;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.helpers.ReRenderManager;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.scenario.KnownRSUsList;
import vanetsim.scenario.KnownVehiclesList;
import vanetsim.scenario.Scenario;

import java.util.Date;
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

    /**  模擬執行狀態（執行中/停止/單步執行）變數   */
    /** Indicates if this simulation should run. If this flag is updated to false the current simulation step
     * is finished and afterwards the simulation stops */
    private volatile boolean running_ = false;

    /** The do one step. */
    private volatile boolean doOneStep_ = false;



    /** An array holding all worker threads. 考慮使用ArrayList來做thread管理 */
    private WorkerThread[] workers_ = null;



    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */
    /**
     * Instantiates a new simulation master.
     */
    public SimulationMaster(){
        Debug.whereru("SimulationMaster", Debug.ISLOGGED);
        Debug.debugInfo(this.getClass().getName(), "SimulationMaster()", Debug.ISLOGGED);
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

                    while(workers_ == null){

                        Debug.detailedInfo("when workers_ is null", Debug.ISLOGGED);

                        if (Map.getInstance().getReadyState() == true && Scenario.getInstance().getReadyState() == true){	// wait until map is ready

                            Debug.detailedInfo("when Map and Scenario are all ready for running", Debug.ISLOGGED);

                            if(Runtime.getRuntime().availableProcessors() < 2) threads = 1;	// on single processor systems or if system reports wrong (smaller 1) amount of CPUs => fallback to 1 CPU and 1 thread
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
