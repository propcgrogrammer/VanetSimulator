package vanetsim.gui.helpers;

import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.EditControlPanel;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.map.Node;
import vanetsim.map.Street;
import vanetsim.scenario.Vehicle;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

/**
 * A class to correctly handle mouseclicks and drags on the DrawingArea. Furthermore, this class also handles the display in the
 * information text area.
 */
public class MouseClickManager extends Thread{


    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /** The only instance of this class (singleton). */
    private static final MouseClickManager INSTANCE = new MouseClickManager();

    private static final int DESTINATION_INTERVALS = 1000;

    /** How often the information panel is refreshed in milliseconds (only achieved approximately!). */
    private static final int INFORMATION_REFRESH_INTERVAL = 800;

    /** After which time dragging shall be activated (in milliseconds) */
    private static final int DRAG_ACTIVATION_INTERVAL = 140;

    /** A formatter for integers with fractions */
    private static final DecimalFormat INTEGER_FORMAT_FRACTION = new DecimalFormat(",##0.00"); //$NON-NLS-1$

    /** A reference to the edit control panel. */
    private final EditControlPanel editPanel_ = VanetSimStart.getMainControlPanel().getEditPanel();

    /** A reference to the reporting control panel. */
    private final ReportingControlPanel reportPanel_ = VanetSimStart.getMainControlPanel().getReportingPanel();

    /** A StringBuilder for the information text. Reused to prevent creating lots of garbage */
    private final StringBuilder informationText_ = new StringBuilder();
    private final StringBuilder informationTextCht_ = new StringBuilder();

    /** The default mouse cursor. */
    private Cursor defaultCursor_ = new Cursor(Cursor.DEFAULT_CURSOR);

    /** The move mouse cursor. */
    private Cursor moveCursor_ = new Cursor(Cursor.MOVE_CURSOR);

    /** The DrawingArea (needed to change mouse cursor). */
    private DrawingArea drawArea_ = null;

    /** <code>true</code> if this manager currently is active,<code>false</code> if it's inactive. */
    public boolean active_ = false;

    /** The time when mouse button was last pressed. */
    private long pressTime_ = 0;

    /** The x coordinate where mouse was pressed. */
    private int pressedX_ = -1;

    /** The y coordinate where mouse was pressed. */
    private int pressedY_ = -1;

    /** The time when mouse button was last released. */
    private long releaseTime_ = 0;

    /** The last x coordinate where mouse was released. */
    private int releasedX_ = -1;

    /** The last y coordinate where mouse was released. */
    private int releasedY_ = -1;

    /** The time already waited to change from default cursor. If set to <code>-1</code> changing between the cursors is disabled. */
    private int waitingTime_ = -1;

    /** The marked node. */
    private Node markedNode_ = null;

    /** The marked street. */
    private Street markedStreet_ = null;

    /** The marked vehicle. */
    private Vehicle markedVehicle_ = null;

    /** The information about a street is cached here as it doesn't change that often. */
    private String cachedStreetInformation_ = ""; //$NON-NLS-1$

    /** Which street is currently cached */
    private Street cachedStreet_ = null;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    /**
     * Gets the single instance of this manager.
     *
     * @return single instance of this manager
     */
    public static MouseClickManager getInstance(){
        return INSTANCE;
    }

    /**
     * Empty, private constructor in order to disable instancing.
     */
    private MouseClickManager(){
        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "MouseClickManager()", Debug.ISLOGGED);
    }


    /**
     * Signals this manager that the mouse was pressed. If the edit mode is currently active, the click is forwarded to the edit panel.
     *
     * @param x	the x coordinate where mouse was pressed
     * @param y	the y coordinate where mouse was pressed
     */
    public synchronized void signalPressed(int x, int y){
        try{
            Point2D.Double mapposition_source = new Point2D.Double(0,0);
            Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
            boolean onEditingTab;
            if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
            else onEditingTab = false;
            if(editPanel_.getEditMode() && onEditingTab){	//editing enabled? then forward the transformed coordinates to the editing panel
                editPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
            } else if(reportPanel_.isInMonitoredMixZoneEditMode()){
                reportPanel_.receiveMouseEvent((int)Math.round(mapposition_source.getX()),(int)Math.round(mapposition_source.getY()));
            } else {
                waitingTime_ = 0;	//to enable cursor change (cursor changes to indicate dragging)
                pressedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
                pressedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
                pressTime_ = System.currentTimeMillis();
                releaseTime_ = pressTime_;
            }
        } catch (Exception e){}
    }

    /**
     * Signals this manager that the mouse was released (used for dragging).
     *
     * @param x	the x coordinate where mouse was released
     * @param y	the y coordinate where mouse was released
     */
    public synchronized void signalReleased(int x, int y){
        boolean onEditingTab;
        if(VanetSimStart.getMainControlPanel().getSelectedTabComponent() instanceof EditControlPanel) onEditingTab = true;
        else onEditingTab = false;
        if((!editPanel_.getEditMode() || !onEditingTab) && !reportPanel_.isInMonitoredMixZoneEditMode()){	//dragging only enabled when not editing!
            try{
                Point2D.Double mapposition_source = new Point2D.Double(0,0);
                Renderer.getInstance().getTransform().inverseTransform(new Point2D.Double(x,y), mapposition_source);
                waitingTime_ = -1;
                releasedX_ = (int)StrictMath.floor(0.5 + mapposition_source.getX());
                releasedY_ = (int)StrictMath.floor(0.5 + mapposition_source.getY());
                releaseTime_ = System.currentTimeMillis();
                if(drawArea_ != null) drawArea_.setCursor(defaultCursor_);
            } catch (Exception e){}
        }
    }

    /**
     * Sets the value for the <code>isActive</code> variable.
     *
     * @param active	<code>true</code> to signal this thread that the DrawingArea has been entered,<code>false</code> to signal that the area was left
     */
    public void setActive(boolean active){
        active_ = active;
        if(active_ == false && drawArea_ != null){
            waitingTime_ = -1;
            drawArea_.setCursor(defaultCursor_);	//to be sure that cursor is right if leaving the area
        }
    }

    /**
     * Cleans markings so that objects can be deleted through garbage collector.
     */
    public void cleanMarkings(){
        markedVehicle_ = null;
        markedStreet_ = null;
        markedNode_ = null;
    }



}
