package vanetsim.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.CyclicBarrier;
import java.awt.geom.Path2D;
import java.util.ArrayDeque;



//import java16.awt.geom.Path2D;
//import java16.util.ArrayDeque;

import javax.imageio.ImageIO;
import vanetsim.ErrorLog;
import vanetsim.localization.Messages;
import vanetsim.map.Junction;
import vanetsim.map.Map;
import vanetsim.map.MapHelper;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.AttackRSU;
import vanetsim.scenario.Vehicle;
import vanetsim.scenario.RSU;
import vanetsim.scenario.events.EventList;
import vanetsim.scenario.events.StartBlocking;

/**
 * This class performs all rendering tasks.自適應調整任務類別
 */
public final class Renderer{

    /** The only instance of this class (singleton). */
    private static final Renderer INSTANCE = new Renderer();

    /**
     * Gets the single instance of this renderer.
     *
     * @return single instance of this renderer
     */
    public static Renderer getInstance(){
        return INSTANCE;
    }

    /** The size of one single vehicle (2,5m). */
    //private static final int VEHICLE_SIZE = 250;
    private static final int VEHICLE_SIZE = 160;

    /** A global formatter to get locale-specific grouping separators from numeric values */
    private static final DecimalFormat FORMATTER = new DecimalFormat(",###"); //$NON-NLS-1$

    /** A reference to the singleton instance of the {@link vanetsim.map.Map} because we need this quite often and don't want to rely on compiler inlining. */
    private final Map map_ = Map.getInstance();

    /** The font used for displaying the current time. */
    private final Font timeFont_ = new Font("Default", Font.PLAIN, 25); //$NON-NLS-1$

    /** The font used for displaying the silent period status. */
    private final Font silentPeriodFont_ = new Font("Default", Font.BOLD, 30); //$NON-NLS-1$

    /** The font size for displaying the vehicle ID. */
    private final int vehicleIDFontSize_ = 950;

    /** The font used for displaying the vehicle ID. */
    private final Font vehicleIDFont_ = new Font("SansSerif", Font.BOLD, vehicleIDFontSize_); //$NON-NLS-1$

    /** A cached <code>BasicStroke</code> used as a (black) background for streets with 2 lanes total. */
    private final BasicStroke lineBackground_ = new BasicStroke((Map.LANE_WIDTH + 45)*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);

    /** A cached <code>BasicStroke</code> used for painting streets with 2 lanes total. */
    private final BasicStroke lineMain_ = new BasicStroke(Map.LANE_WIDTH*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);

    /** The <code>AffineTransform</code> which handles zooming and panning transparently for us. */
    private final AffineTransform transform_ = new AffineTransform();

    /** Used as source to transform the blockingImage into current coordinate space. */
    private final Point2D blockingImageTransformSource_ = new Point2D.Double(0,0);;

    /** To get the result of the transformation of the blockingImage. */
    private final Point2D blockingImageTransformDestination_ = new Point2D.Double(0,0);

    /** If a simulation thread is currently running. */
    private boolean simulationRunning_ = false;

    /** Used to display how long the relative time in milliseconds since the simulation started. */
    private int timePassed_ = 0;

    /** A boolean indicating if a new full render (=rendering of static objects) is needed. */
    private boolean scheduleFullRender_ = false;

    /** <code>true</code> if the current painting was forced by the simulation master, else <code>false</code> (by AWT/Swing or so). */
    private boolean doPaintInitializedBySimulation_ = false;

    /** The {@link DrawingArea} for the simulation. */
    private DrawingArea drawArea_;

    /** The width of the {@link DrawingArea}. */
    private int drawWidth_ = 0;

    /** The height of the {@link DrawingArea}. */
    private int drawHeight_ = 0;

    /** The x coordinate of the center of the currently drawn map part. */
    private double middleX_ = 0;

    /** The y coordinate of the center of the currently drawn map part. */
    private double middleY_ = 0;

    /** The minimum x coordinate to be painted. */
    private int mapMinX_ = 0;

    /** The minimum y coordinate to be painted. */
    private int mapMinY_ = 0;

    /** The maximum x coordinate to be painted. */
    private int mapMaxX_ = 0;

    /** The maximum y coordinate to be painted. */
    private int mapMaxY_ = 0;

    /** If the current rendered view displays over the borders of the map. Used to detect when efficient panning can NOT be used. */
    private boolean currentOverdrawn_ = true;

    /** If the last rendered view displays over the borders of the map. Used to detect when efficient panning can NOT be used. */
    private boolean lastOverdrawn_ = true;

    /** The current zooming factor. */
    private double zoom_ = Math.exp(1/100.0)/1000;

    /** The amount of pans in x direction (negative if moved down) since the last paint of the static objects. */
    private int panCountX_ = 0;

    /** The amount of pans in y direction (negative if moved down) since the last paint of the static objects. */
    private int panCountY_ = 0;

    /** The x-axis index of the leftmost region which is currently considered to be rendered. */
    private int regionMinX_ = 0;

    /** The y-axis index of the leftmost region which is currently considered to be rendered. */
    private int regionMinY_ = 0;

    /** The x-axis index of the rightmost region which is currently considered to be rendered. */
    private int regionMaxX_ = 0;

    /** The y-axis index of the rightmost region which is currently considered to be rendered. */
    private int regionMaxY_ = 0;

    /** A temporary <code>AffineTransform</code>. Prevents some unnecessary garbage collection. */
    private AffineTransform tmpAffine_ = new AffineTransform();

    /** The maximum translation in x direction we got from a <code>Graphics2D</code> object (used because this value sometimes is wrong when using substance themes). */
    private double maxTranslationX_ = 0;

    /** The maximum translation in y direction we got from a <code>Graphics2D</code> object (used because this value sometimes is wrong when using substance themes). */
    private double maxTranslationY_ = 0;

    /** A street which is to be drawn marked (selected by user). */
    private Street markedStreet_ = null;

    /** A vehicle which is to be drawn marked (selected by user). */
    private Vehicle markedVehicle_ = null;

    /** A vehicle which is to be drawn as attacker (selected by user). */
    private Vehicle attackerVehicle_ = null;

    /** A vehicle which is to be drawn as attacked vehicle (selected by user). */
    private Vehicle attackedVehicle_ = null;

    /** If circles shall be displayed to indicate communication distances. */
    private boolean highlightCommunication_ = false;

    /** If all nodes shall be highlighted. */
    private boolean highlightAllNodes_ = false;

    /** If mix zones shall be hided. */
    private boolean hideMixZones_ = false;

    /** If vehicle IDs shall be displayed. */
    private boolean displayVehicleIDs_ = false;

    /** <code>true</code> if all blockings shall be showed, else <code>false</code>. */
    private boolean showAllBlockings_;

    /** If the monitored beacon zone shall be indicated or not */
    private boolean showBeaconMonitorZone_ = false;

    /** If vehicles should be displayed (used in vehicle edit modes) */
    private boolean showVehicles_ = false;

    /** If mix zones should be displayed (used in mix zone edit mode)*/
    private boolean showMixZones_ = true;

    /** If RSUs should be displayed (used in RSU edit mode)*/
    private boolean showRSUs_ = false;

    /** If attacker and attacked vehicle should be displayed*/
    private boolean showAttackers_ = false;

    /** If a mix zone should be added at each street corner */
    private boolean autoAddMixZones_ = false;

    /** The minimum x coordinate which is checked during beacon monitoring. */
    private int beaconMonitorMinX_ = -1;

    /** The maximum x coordinate which is checked during beacon monitoring. */
    private int beaconMonitorMaxX_ = -1;

    /** The minimum y coordinate which is checked during beacon monitoring. */
    private int beaconMonitorMinY_ = -1;

    /** The maximum y coordinate which is checked during beacon monitoring. */
    private int beaconMonitorMaxY_ = -1;

    /** The RSU color */
    private Color rsuColor = new Color(0,100,0);

    /** The RSU color */
    private Color arsuColor = new Color(100,0,0);

    /** A <code>CyclicBarrier</code> to signal the SimulationMaster that we are ready with rendering the dynamic objects. */
    private CyclicBarrier barrierForSimulationMaster_;

    /** An image to indicate that there's a blocking. */
    private BufferedImage blockingImage_;

    /** A scaled instance of the <code>blockingImage_</code>. Is updated on every zoom change. */
    private BufferedImage scaledBlockingImage_;

    /** flag for console start*/
    private boolean consoleStart = false;

    /** the marked junction*/
    private Junction markedJunction_ = null;

    /** arraylist to show if a beacon was guessed correctly after mix. Is not used while simulation is running, so performance doesn't have to be that good*/
    private ArrayList<String> locationInformationMix_ = null;

    /** arraylist to show if a beacon was guessed correctly after silent period. Is not used while simulation is running, so performance doesn't have to be that good*/
    private ArrayList<String> locationInformationSilentPeriod_ = null;

    /** counts passed mix-zones. Used to show location information */
    private int mixZoneAmount = 0;

    /** flag to activate the debug mode. In this mode more information is shown to the user */
    private boolean debugMode = true;

    /**
     * Private constructor in order to disable instancing. Creates the blocking images.
     */
    private Renderer(){
        try{
            //We're not directly using the BufferedImage returned by ImageIO as this might not be suitable for the graphics environment and thus slow. The small overhead here does not hurt
            //as it's just done on program startup
            BufferedImage tmpImage;
            URL url = ClassLoader.getSystemResource("vanetsim/images/blocking.png"); //$NON-NLS-1$
            tmpImage = ImageIO.read(url);
            blockingImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(tmpImage.getWidth(), tmpImage.getHeight(), 3);
            blockingImage_.getGraphics().drawImage(tmpImage, 0, 0, null);
        } catch(Exception e){
            ErrorLog.log(Messages.getString("Renderer.noBlockingImage"), 7, Renderer.class.getName(), "Constructor", e);  //$NON-NLS-1$//$NON-NLS-2$
            //create just empty transparent images.
            blockingImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(1, 1, 3);
        }
    }

    /**
     * Schedules an update of the {@link DrawingArea}. Note that depending on if a simulation is running or not, the update
     * might be performed at a later time!
     *
     * @param fullRender 	<code>true</code> if a full update including all static objects should be done, else <code>false</code>
     * @param forceRenderNow <code>true</code> to force an immediate update regardless of consistency considerations (should only be used by the {@link vanetsim.simulation.SimulationMaster})
     */
    public void ReRender(boolean fullRender, boolean forceRenderNow){
        if(!isConsoleStart()){
            if(fullRender) scheduleFullRender_ = true;
            if (drawArea_ != null){
                if(!simulationRunning_ || forceRenderNow){
                    if(scheduleFullRender_){
                        scheduleFullRender_ = false;
                       // drawArea_.prepareBufferedImages();
                        /** 補足prepareBufferedImages（）未實作 */
                    }
                    doPaintInitializedBySimulation_ = true;
                    drawArea_.repaint();
                }
            }
        }



    }

    /**
     * Sets a new zooming factor.
     *
     * @param zoom the new zooming factor
     */
    public synchronized void setMapZoom(double zoom){
        /** 待增加 */
    }

    /**
     * Updates various internal parameters after changes through panning or zooming.<br>
     * The regions which shall be drawn are calculated here. They are a little bit larger than
     * normally necessary in order to correctly display communication distance and mix zones. Otherwise,
     * the distance of a vehicle, which is a little bit outside the currently viewable area wouldn't
     * be drawn. This all induces a little bit of an unnecessary overdraw to the static objects but this
     * should not matter a lot.
     */
    public synchronized void updateParams(){
        /** 待增加 */
    }

    /**
     * Gets the x coordinate of the middle of the current view.
     *
     * @return the x coordinate in map scale
     */
    public double getMiddleX(){
        return middleX_;
    }

    /**
     * Gets the y coordinate of the middle of the current view.
     *
     * @return the y coordinate in map scale
     */
    public double getMiddleY(){
        return middleY_;
    }

    /**
     * Gets the current zooming factor.
     *
     * @return the current zooming factor
     */
    public double getMapZoom(){
        return zoom_;
    }

    /**
     * Gets the time passed since simulation start.
     *
     * @return the time passed in milliseconds
     */
    public int getTimePassed(){
        return timePassed_;
    }


    /**
     * Gets the currently active coordinate transformation.
     *
     * @return the transform
     */
    public AffineTransform getTransform(){
        return transform_;
    }

    /**
     * Notify if the simulation is running or not.
     *
     * @param running	<code>true</code> if a simulation is currently running, <code>false</code> if it's suspended
     */
    public void notifySimulationRunning(boolean running){
        simulationRunning_ = running;
    }

    /**
     * Sets a new marked street.
     *
     * @param markedStreet the street to mark
     */
    public synchronized void setMarkedStreet(Street markedStreet){
        markedStreet_ = markedStreet;
    }

    /**
     * Sets a new marked vehicle.
     *
     * @param markedVehicle the vehicle to mark
     */
    public synchronized void setMarkedVehicle(Vehicle markedVehicle){
        markedVehicle_ = markedVehicle;
    }

    /**
     * Gets a marked vehicle.
     *
     */
    public synchronized Vehicle getMarkedVehicle(){
        return markedVehicle_;
    }

    /**
     * Sets a new attacker vehicle.
     *
     * @param attackerVehicle the attacker vehicle
     */
    public synchronized void setAttackerVehicle(Vehicle attackerVehicle){
        attackerVehicle_ = attackerVehicle;
    }

    /**
     * Gets the attacker vehicle.
     *
     */
    public synchronized Vehicle getAttackerVehicle(){
        return attackerVehicle_;
    }

    /**
     * Sets the coordinates of the center of the viewable area.
     *
     * @param x	the new x coordinate for the center of the viewable area
     * @param y	the new y coordinate for the center of the viewable area
     */
    public synchronized void setMiddle(int x, int y){
        middleX_ = x;
        middleY_ = y;
        updateParams();
    }

    /**
     * Sets the {@link DrawingArea} this Renderer is associated with.
     * 由 VanetSimStart --> addComponentsToPane() 呼叫
     * @param drawArea 	the area on which this Renderer draws
     */
    public void setDrawArea(DrawingArea drawArea){
        drawArea_ = drawArea;
    }

    /**
     * Set the height of the {@link DrawingArea}.
     *
     * @param drawHeight the new height
     */
    public void setDrawHeight(int drawHeight) {
        drawHeight_ = drawHeight;
    }

    /**
     * Set the width of the {@link DrawingArea}.
     *
     * @param drawWidth the new width
     */
    public void setDrawWidth(int drawWidth) {
        drawWidth_ = drawWidth;
    }

    /**
     * Sets the time passed since simulation start.
     *
     * @param timePassed the new time in milliseconds
     */
    public void setTimePassed(int timePassed){
        timePassed_ = timePassed;
    }

    /**
     * Sets the barrier for synchronization with the SimulationMaster.
     *
     * @param barrier the barrier to use
     */
    public void setBarrierForSimulationMaster(CyclicBarrier barrier){
        barrierForSimulationMaster_ = barrier;
    }

    /**
     * If all nodes shall be highlighted.
     *
     * @param highlightNodes <code>true</code> if you want to highlight nodes, else <code>false</code>
     */
    public void setHighlightNodes(boolean highlightNodes){
        highlightAllNodes_ = highlightNodes;
    }

    /**
     * If circles shall be displayed to show communication distance.
     *
     * @param highlightCommunication <code>true</code> if you want to show the circles, else <code>false</code>
     */
    public void setHighlightCommunication(boolean highlightCommunication){
        highlightCommunication_ = highlightCommunication;
    }

    /**
     * If filled circles shall be displayed to hide the mix zones.
     *
     * @param hideMixZones <code>true</code> if you want to show the circles, else <code>false</code>
     */
    public void setHideMixZones(boolean hideMixZones){
        hideMixZones_ = hideMixZones;
    }

    /**
     * If the IDs of the vehicle shall be drawn on the map.
     *
     * @param displayVehicleIDs <code>true</code> if you want to show the IDs, else <code>false</code>
     */
    public void setDisplayVehicleIDs(boolean displayVehicleIDs){
        displayVehicleIDs_ = displayVehicleIDs;
    }

    /**
     * If you want to show all blockings.
     *
     * @param showAllBlockings <code>true</code> if you want to show all blockings, else <code>false</code>
     */
    public void setShowAllBlockings(boolean showAllBlockings){
        showAllBlockings_ = showAllBlockings;
    }

    /**
     * If you want to display the monitored beacon zone.
     *
     * @param showBeaconMonitorZone	<code>true</code> if you want to display the monitored beacon zones, else <code>false</code>
     */
    public void setShowBeaconMonitorZone(boolean showBeaconMonitorZone){
        showBeaconMonitorZone_ = showBeaconMonitorZone;
    }

    /**
     * Sets the values for the monitored beacon zone. A rectangular bounding box within the specified coordinates
     * is monitored if {@link #setShowBeaconMonitorZone(boolean)} is set to <code>true</code>.
     *
     * @param beaconMonitorMinX	the minimum x coordinate
     * @param beaconMonitorMaxX	the maximum x coordinate
     * @param beaconMonitorMinY	the minimum y coordinate
     * @param beaconMonitorMaxY	the maximum y coordinate
     */
    public void setMonitoredBeaconZoneVariables(int beaconMonitorMinX, int beaconMonitorMaxX, int beaconMonitorMinY, int beaconMonitorMaxY){
        beaconMonitorMinX_ = beaconMonitorMinX;
        beaconMonitorMaxX_ = beaconMonitorMaxX;
        beaconMonitorMinY_ = beaconMonitorMinY;
        beaconMonitorMaxY_ = beaconMonitorMaxY;
    }

    /**
     * If you want to show all vehicles.
     *
     * @param showVehicles <code>true</code> if you want to show all vehicles, else <code>false</code>
     */
    public void setShowVehicles(boolean showVehicles) {
        showVehicles_ = showVehicles;
    }

    /**
     * Gets if vehicles are displayed.
     *
     * @return <code>true</code> if vehicles are displayed
     */
    public boolean isShowVehicles() {
        return showVehicles_;
    }

    /**
     * If you want to show all mix zones.
     *
     * @param showMixZones <code>true</code> if you want to show all mix zones, else <code>false</code>
     */
    public void setShowMixZones(boolean showMixZones) {
        showMixZones_ = showMixZones;
    }

    /**
     * Gets if mix zones are displayed
     *
     * @return true if mix zones are displayed
     */
    public boolean isShowMixZones() {
        return showMixZones_;
    }

    /**
     * If you want to add mix zones to all street corners automatically.
     *
     * @param autoAddMixZones <code>true</code> if you want to add mix zones to all street corners automatically else <code>false</code>
     */
    public void setAutoAddMixZones(boolean autoAddMixZones) {
        autoAddMixZones_ = autoAddMixZones;
    }

    /**
     * Gets if mix zones are added automatically on each street corner
     *
     * @return true if mix zones are added automatically
     */
    public boolean isAutoAddMixZones() {
        return autoAddMixZones_;
    }

    /**
     * If you want to show all RSUs.
     *
     * @param showRSUs <code>true</code> if you want to show all RSUs, else <code>false</code>
     */
    public void setShowRSUs(boolean showRSUs) {
        showRSUs_ = showRSUs;
    }

    /**
     * Gets if RSUs are displayed
     *
     * @return true if RSUs are displayed
     */
    public boolean isShowRSUs() {
        return showRSUs_;
    }

    public Vehicle getAttackedVehicle() {
        return attackedVehicle_;
    }

    public void setAttackedVehicle(Vehicle attackedVehicle_) {
        this.attackedVehicle_ = attackedVehicle_;
    }

    public boolean isShowAttackers() {
        return showAttackers_;
    }

    public void setShowAttackers(boolean showAttackers_) {
        this.showAttackers_ = showAttackers_;
    }

    public boolean isConsoleStart() {
        return consoleStart;
    }

    public void setConsoleStart(boolean consoleStart) {
        this.consoleStart = consoleStart;
    }

    /**
     * @param markedJunction_ the markedJunction_ to set
     */
    public void setMarkedJunction_(Junction markedJunction_) {
        this.markedJunction_ = markedJunction_;
    }

    /**
     * @return the markedJunction_
     */
    public Junction getMarkedJunction_() {
        return markedJunction_;
    }

    public ArrayList<String> getLocationInformationMix() {
        return locationInformationMix_;
    }

    public void setLocationInformationMix(ArrayList<String> locationInformation) {
        this.locationInformationMix_ = locationInformation;
    }

    public ArrayList<String> getLocationInformationSilentPeriod_() {
        return locationInformationSilentPeriod_;
    }

    public void setLocationInformationSilentPeriod_(
            ArrayList<String> locationInformationSilentPeriod_) {
        this.locationInformationSilentPeriod_ = locationInformationSilentPeriod_;
    }

    public int getMixZoneAmount() {
        return mixZoneAmount;
    }

    public void setMixZoneAmount(int mixZoneAmount) {
        this.mixZoneAmount = mixZoneAmount;
    }

}