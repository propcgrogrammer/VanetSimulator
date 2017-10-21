package vanetsim.gui.helpers;

import vanetsim.debug.Debug;

/**
 * A small manager to get better user experience through buffering ReRender-calls.
 * By using this, the GUI gets (almost) immediately responsive again upon clicking a button. If rendering the background takes
 * longer than the time between multiple button clicks (made by the user), some ReRender-events are kind of silently dropped.
 */
public class ReRenderManager extends Thread{


    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /** The only instance of this class (singleton). */
    private static final ReRenderManager INSTANCE = new ReRenderManager();

    /** A variable indicating if a ReRender is scheduled. */
    private boolean doRender_ = false;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    /**
     * Empty, private constructor in order to disable instancing.
     */
    private ReRenderManager(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "ReRenderManager()", Debug.ISLOGGED);
    }

    /**
     * Gets the single instance of this manager.
     *
     * @return single instance of this manager
     */
    public static ReRenderManager getInstance(){
        return INSTANCE;
    }

    /**
     * Schedule a re-render-Operation.
     */
    public void doReRender(){
        doRender_ = true;
    }


}
