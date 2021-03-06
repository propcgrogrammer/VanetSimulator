package vanetsim.gui.helpers;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;

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

        Debug.callFunctionInfo(this.getClass().getName(),"doReRender()",Debug.ISLOGGED);

        doRender_ = true;
    }


    /**
     * A thread which checks if a re-rendering is scheduled and then sleeps 50ms. If necessary, it
     * calls the {@link Renderer} to perform a full re-render.
     *
     * @see java.lang.Thread#run()
     */
    public void run(){
        Debug.callFunctionInfo(this.getClass().getName(), "run()", Debug.ISLOGGED);
        setName("ReRenderManager"); //$NON-NLS-1$
        setPriority(Thread.MIN_PRIORITY);
        Renderer renderer = Renderer.getInstance();
        while(true){
            if(doRender_){
                doRender_ = false;
                renderer.ReRender(true, false);
            }
            try{
                Thread.sleep(10);
            } catch (Exception e){};
        }
    }

}
