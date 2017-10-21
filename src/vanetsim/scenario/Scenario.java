package vanetsim.scenario;


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
