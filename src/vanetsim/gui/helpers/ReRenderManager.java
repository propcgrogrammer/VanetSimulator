package vanetsim.gui.helpers;

/**
 * A small manager to get better user experience through buffering ReRender-calls.
 * By using this, the GUI gets (almost) immediately responsive again upon clicking a button. If rendering the background takes
 * longer than the time between multiple button clicks (made by the user), some ReRender-events are kind of silently dropped.
 */
public class ReRenderManager extends Thread{


}
