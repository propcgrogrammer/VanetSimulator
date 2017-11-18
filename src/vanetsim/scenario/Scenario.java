package vanetsim.scenario;


import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.MouseClickManager;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.scenario.events.EventList;

import java.io.File;

/**
 * A scenario saves the vehicles and events.
 */
public class Scenario {


    /**
     * ////////////////////////
     * //  instance variable
     * ////////////////////////
     */
    /** The only instance of this class (singleton). */
    private static final Scenario INSTANCE = new Scenario();

    /** A flag to signal if loading is ready. While loading is in progress, simulation and rendering is not possible. */
    private boolean ready_ = true;

    /** File name of the Scenario. Used to name log files */
    private String scenarioName = "";


    /**
     * ////////////////////////
     * //  method
     * ////////////////////////
     */
    /**
     * Empty, private constructor in order to disable instancing.
     */
    private Scenario() {

        Debug.whereru(this.getClass().getName(),Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(),"Scenario()",Debug.ISLOGGED);
    }


    /**
     * Initializes a new (empty) scenario.
     * 初始化所有Scenario包含 Vehicle / RSU / Street / Event
     * 於 2017/10/24_0425 新增
     */
    public void initNewScenario(){

        Debug.callFunctionInfo(this.getClass().getName(),"initNewScenario()",Debug.ISLOGGED);

        if(ready_ == true){ /** 預設ready為true，所以初次必會執行 */
            /** 初始化地圖，lock不讓模擬執行 */
            ready_ = false;
            /** 停止SimulationMaster的執行緒 */
            if(!Renderer.getInstance().isConsoleStart()) VanetSimStart.getSimulationMaster().stopThread();
            /** 設定SimulateControlPanel的控制面板 */
            if(!Renderer.getInstance().isConsoleStart()) VanetSimStart.getMainControlPanel().getSimulatePanel().setSimulationStop();

            KnownVehiclesList.setTimePassed(0);
            KnownRSUsList.setTimePassed(0);

            Renderer.getInstance().setTimePassed(0);
            Renderer.getInstance().setMarkedVehicle(null);
            Renderer.getInstance().setShowVehicles(false);
            Renderer.getInstance().setShowRSUs(false);
            Renderer.getInstance().setShowMixZones(false);
            Renderer.getInstance().setAttackedVehicle(null);
            Renderer.getInstance().setAttackerVehicle(null);
            Renderer.getInstance().setShowAttackers(false);


            Vehicle.setMaximumCommunicationDistance(0);
            Vehicle.resetGlobalRandomGenerator();
            Vehicle.setMinTravelTimeForRecycling(60000);	// standard value for recycle time
            Vehicle.setArsuList(new AttackRSU[0]);
            Vehicle.setAttackedVehicleID_(0);


            if(!Renderer.getInstance().isConsoleStart()) MouseClickManager.getInstance().cleanMarkings();
            /** Regions 其初始值為 null */
            Region[][] Regions = Map.getInstance().getRegions();
            /** Region_max_x 其初始值為 0 */
            int Region_max_x = Map.getInstance().getRegionCountX();
            /** Region_max_y 其初始值為 0 */
            int Region_max_y = Map.getInstance().getRegionCountY();

            /** 若沒有地圖載入，則迴圈不會被執行 */
            int i, j;
            for(i = 0; i < Region_max_x; ++i){
                for(j = 0; j < Region_max_y; ++j){
                    Regions[i][j].cleanVehicles();
                }
            }

            EventList.getInstance().clearEvents();

            /** 注意updateList（）未實作
             *  於2017/11/14_2319 完成實作
             * */

            if(!Renderer.getInstance().isConsoleStart())VanetSimStart.getMainControlPanel().getEditPanel().getEditEventPanel().updateList();
        }
    }

    /**
     * Gets the single instance of this scenario.
     *
     * @return single instance of this scenario
     */
    public static Scenario getInstance(){
        return INSTANCE;
    }

    /**
     * Sets the ready state of the scenario.
     *
     * @param ready	<code>true</code> to signal that this scenario is ready with loading, else <code>false</code>
     */
    public void setReadyState(boolean ready){
        ready_ = ready;
    }

    /**
     * Returns if the scenario is currently being loaded. While loading, simulation and rendering should not
     * be done because not all simulation elements are already existing!
     *
     * @return <code>true</code> if loading has finished, else <code>false</code>
     */
    public boolean getReadyState(){
        return ready_;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    /**
     * Save the scenario.
     *
     * @param file	the file in which to save
     * @param zip	if <code>true</code>, file is saved in a compressed zip file (extension .zip is added to <code>file</code>!). If <code>false</code>, no compression is made.
     */
    public void save(File file, boolean zip){

    }


}
