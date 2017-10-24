package vanetsim.gui.helpers;

import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.controlpanels.EditControlPanel;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.map.Node;
import vanetsim.map.Street;
import vanetsim.scenario.Vehicle;

import java.awt.*;
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
     * Cleans markings so that objects can be deleted through garbage collector.
     */
    public void cleanMarkings(){
        markedVehicle_ = null;
        markedStreet_ = null;
        markedNode_ = null;
    }



}
