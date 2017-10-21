package vanetsim.scenario;

import java.awt.Color;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayDeque;

//import java16.util.ArrayDeque;


import vanetsim.VanetSimStart;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.gui.helpers.PrivacyLogWriter;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.routing.RoutingAlgorithm;
import vanetsim.routing.WayPoint;
import vanetsim.routing.A_Star.A_Star_Algorithm;
import vanetsim.scenario.messages.Message;
import vanetsim.scenario.messages.PenaltyMessage;

/**
 * A vehicle which can move and communicate (if wifi is enabled).
 */
public class Vehicle extends LaneObject{

    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */
    /** A reference to the map so that we don't need to call this over and over again. */
    private static final Map MAP = Map.getInstance();

    /** A reference to the reporting control panel so that we don't need to call this over and over again. */
    private static final ReportingControlPanel REPORT_PANEL = getReportingPanel();

    private static final int PRIORITY_TIMEOUT = 5000;

    /** When known vehicles are rechecked for outdated entries. Measured in milliseconds. */
    private static final int KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL = 30000;

    /** When known Road-Side-Units are rechecked for outdated entries. Measured in milliseconds. */
    private static final int KNOWN_RSUS_TIMEOUT_CHECKINTERVAL = 5000;

    /** How long to wait between searching the known penalties for outdated entries. Measured in milliseconds. */
    private static final int KNOWN_PENALTIES_TIMEOUT_CHECKINTERVAL = 30000;

    /** The minimum time between two newly created messages in milliseconds (does not apply to forwarded messages!). */
    private static final int MESSAGE_INTERVAL = 30000;

    /** The minimum time between two lane changes in milliseconds. */
    //private static final int LANE_CHANGE_INTERVAL = 5000;
    private static final int LANE_CHANGE_INTERVAL = 999999999;

    /** If a vehicle does not move (speed=0) for this time (in milliseconds), a jam is detected and a penalty message created. */
    private static final int TIME_FOR_JAM = 5000;

    /** The radius in cm around the message destination in which the message will be flooded. */
    private static final int PENALTY_MESSAGE_RADIUS = 5000;

    /** How long the penalty message will be valid (in milliseconds). */
    private static final int PENALTY_MESSAGE_VALID = 10000;

    /** The penalty which will be added through the message in cm. This value will be added in routing calculations. */
    private static final int PENALTY_MESSAGE_VALUE = 2000000;	// 20km

    /** How long the penalty itself will be valid (in milliseconds). */
    private static final int PENALTY_VALID = 120000;

    /** How long a vehicle waits to check if it's inside a mix. */
    private static final int MIX_CHECK_INTERVAL = 1000;

    /** How long the attacker waits (steps) to check if has new information about the attacked vehicle and needs to reroute. */
    private static final int ATTACKER_INTERVAL = 50;

    /** A global random number generator used to initialize the generators of the vehicles. */
    private static final Random RANDOM = new Random(1L);

    /** The routing algorithm used. */
    private static final RoutingAlgorithm ROUTING_ALGO = new A_Star_Algorithm();

    /** The routing mode used. See the A_Star_Algo for details. */
    private static int routingMode_ = 1;

    /** The minimum time a vehicle must have traveled to get recycled. This shall prevent very shortliving
     * vehicles from consuming lots of CPU time for recycling. */
    private static int minTravelTimeForRecycling_ = 60000;

    /** If communication is enabled */
    private static boolean communicationEnabled_ = true;

    /** If beacons are enabled */
    private static boolean beaconsEnabled_ = true;

    /** If mix zones are enabled */
    private static boolean mixZonesEnabled_ = true;

    /** If a fallback to the beaconless method shall be done in mix zones */
    private static boolean mixZonesFallbackEnabled_ = true;

    /** If the fallback mode only sends messages which are in flooding/broadcast mode. */
    private static boolean mixZonesFallbackFloodingOnly_ = true;

    /** How large a mix is in cm. Set in the common settings */
    private static int mixZoneRadius_ = 10000;

    /** How large a mix is in cm max. */
    private static int maxMixZoneRadius_ = 0;

    /** How long a vehicle waits to communicate again (in milliseconds). Also used for cleaning up outdated known messages. */
    private static int communicationInterval_ = 160;

    /** How long a vehicle waits to send its beacons again. */
    private static int beaconInterval_ = 240;

    /** The maximum communication distance a vehicle has. */
    private static int maximumCommunicationDistance_ = 0;

    /** An array holding all regions of the map. */
    private static Region[][] regions_;

    /** If monitoring the beacon is enabled or not. */
    private static boolean beaconMonitorEnabled_ = false;

    /** The minimum x coordinate which is checked during beacon monitoring. */
    private static int beaconMonitorMinX_ = -1;

    /** The maximum x coordinate which is checked during beacon monitoring. */
    private static int beaconMonitorMaxX_ = -1;

    /** The minimum y coordinate which is checked during beacon monitoring. */
    private static int beaconMonitorMinY_ = -1;

    /** The maximum y coordinate which is checked during beacon monitoring. */
    private static int beaconMonitorMaxY_ = -1;

    /** If recycling of vehicles is allowed or not */
    private static boolean recyclingEnabled_ = true;

    /** List of all AttackRSUs */
    private static AttackRSU arsuList[] = new AttackRSU[0];

    /** If attacker logging is enabled */
    private static boolean attackerDataLogged_ = false;

    /** If attacker encrypted logging is enabled */
    private static boolean attackerEncryptedDataLogged_ = false;

    /** If attacker logging is enabled */
    private static boolean privacyDataLogged_ = false;

    /** ID of the attacked vehicle */
    private static long attackedVehicleID_ = 0;

    /** Time for reroute of attacker */
    private static int reRouteTime_ = -1;

    /** encrypted beacon communication in Mix-Zones */
    private static boolean encryptedBeaconsInMix_ = false;

    /** A counter for the steady id */
    private static int steadyIDCounter = 0;

    /** time between silent-periods (in ms)*/
    private static int TIME_BETWEEN_SILENT_PERIODS = 10000;

    /** time of silent-periods (in ms)*/
    private static int TIME_OF_SILENT_PERIODS = 2000;

    /** flag to show if there is a silent period at the moment */
    private static boolean silent_period = false;

    /** flag to turn silent periods on/off */
    private static boolean silentPeriodsOn = false;

    // object variables begin here

    /** The destinations this vehicle wants to visit. */
    public ArrayDeque<WayPoint> originalDestinations_;

    /** The <code>WayPoint</code> where this vehicles started. */
    private WayPoint startingWayPoint_ ;   /** 待修改 */

    /** The vehicle length in cm. */
    private int vehicleLength_;

    /** The maximum speed of this car in cm/s. */
    private int maxSpeed_;

    /** The color of the vehicle. */
    private Color color_;

    /** The braking rate in cm/s^2. */
    private int brakingRate_;

    /** The acceleration rate in cm/s^2. */
    private int accelerationRate_ ;

    /** if activated the vehicle is an emergency vehicle */
    private boolean emergencyVehicle_;

    /** The maximum braking distance. */
    private int maxBrakingDistance_;

    /** A class storing messages of different states: execute, forward and old ones. Could also be stored inside the
     * vehicle class but it's a lot more clearly arranged like that. */
    private final KnownMessages knownMessages_ = new KnownMessages(this);

    /** A list of all vehicles currently known because of received beacons. */
    private final KnownVehiclesList knownVehiclesList_ = new KnownVehiclesList();

    /** A list of all Road-Side-Units currently known because of received beacons. */
    private final KnownRSUsList knownRSUsList_ = new KnownRSUsList();

    /** All known penalties. */
    private final KnownPenalties knownPenalties_ = new KnownPenalties(this);

    /** <code>true</code> if this vehicle has a communication device (WiFi), else <code>false</code> . */
    private boolean wiFiEnabled_;

    /** A random number generator for each vehicle. Primarily used for ID generation but can be used for other tasks, too. */
    private Random ownRandom_;

    /** An ID used in communication (beacons). This might change (=> mixing zone)!It cannot be guaranteed
     * that this is really an unique ID as it's generated randomly! */
    private long ID_;

    /** An ID used to track vehicles after changing pseudonyms (for logging purpose only) */
    private int steadyID_;

    /** The destinations this vehicle wants to visit. */
    private ArrayDeque<WayPoint> destinations_;

    /** The new speed after the step. <code>curSpeed_</code> will be set to this in the moving-process to circumvent synchronisation problems. */
    private double newSpeed_;

    /** The new lane after the step. <code>curLane_</code> will be set to this in the moving-process to circumvent synchronisation problems. */
    private int newLane_ = 1;

    /** If set to true, this car is active and thus is drawn and moves. */
    private boolean active_ = false;

    /** An array containing ALL streets on which this vehicle will move until the next destination is reached. This can change when a rerouting is done! */
    private Street[] routeStreets_;

    /** An array with the directions on the streets corresponding to <code>routeStreets_</code> */
    private boolean[] routeDirections_;

    /** The current position in the <code>routeStreets_</code> and <code>routeDirections_</code> array */
    private int routePosition_;

    /** The current braking distance. */
    private int curBrakingDistance_;

    /** The speed at last braking distance calculation. */
    private double speedAtLastBrakingDistanceCalculation_ = 0;

    /** If the vehicle is currently in a mixing zone */
    private boolean isInMixZone_ = false;

    /** A node that we are allowed to pass. */
    private Node junctionAllowed_ = null;

    /** The maximum distance in cm this car can communicate. */
    private int maxCommDistance_;

    /** The current region. */
    private Region curRegion_;

    /** The time in milliseconds before doing the next movement. During waiting the vehicle communicates but does not
     * block other cars from passing. */
    private int curWaitTime_;

    /** The total time in milliseconds this vehicle traveled (excludes predefined waittimes!) */
    private int totalTravelTime_;

    private int accuWaitTime_ = 0;
    private int accuBlockingTime_ = 0;

    /** The total distance in cm this vehicle traveled */
    private long totalTravelDistance_;

    /** If braking for the next destination should be done currently. */
    private boolean brakeForDestination_ = false;

    /** A countdown for braking. */
    private int brakeForDestinationCountdown_ = Integer.MAX_VALUE;

    /** A countdown for doing the next destination check. */
    private int destinationCheckCountdown_ = 0;

    /** A countdown to check if the minimum time between two lane changes has been reached. */
    private int laneChangeCountdown = 0;

    /** A countdown for communication. Also used for cleaning up outdated known messages. */
    private int communicationCountdown_;

    /** A countdown for sending beacons. */
    private int beaconCountdown_;

    /** A countdown for checking if inside a mix or not. */
    private int mixCheckCountdown_;

    /** A countdown for rechecking if known vehicles are outdated. */
    private int knownVehiclesTimeoutCountdown_;

    /** A countdown for rechecking if known RSUs are outdated. */
    private int knownRSUsTimeoutCountdown_;

    /** A countdown for rechecking if known penalties are outdated. */
    private int knownPenaltiesTimeoutCountdown_;

    /** How much time has passed since the last message was created*/
    private int lastMessageCreated = 0;

    /** How long this vehicle is waiting in a jam (in milliseconds). */
    private int stopTime_ = 0;

    /** How many messages this vehicle has created. */
    private int messagesCreated_ = 0;

    /** How many times this vehicle changed it's ID (due to mixes) */
    private int IDsChanged_ = 0;

    /** If the vehicle may be reused. */
    private boolean mayBeRecycled_ = false;

    /** Used to reroute the attacker after leaving the mix-zone */
    private Boolean attackerWasInMix = false;

    /** Used to reroute the attacker after leaving the mix-zone */
    private Boolean attackedWasInMix = false;

    /** Flag is set true when the attacker finds the attacked vehicle the first time */
    private Boolean firstContact = false;

    /** Saves the node of the current mix-zone. Used for encrypted Beacons*/
    private Node curMixNode_ = null;

    /** Vehicle is waiting behind a traffic signal (do not send any message)*/
    private boolean waitingForSignal_ = false;



    /** A distance between vehicles based on time (ms) (between 0 - 1000)*/
    private int timeDistance_ = 1000;

    /** A politeness factor in % */
    private int politeness_ = 0;

    /** Flag to log begin and end of silent periods */
    private boolean silentPeriod = false;

    /** Saved Beacon 1 */
    private String savedBeacon1 = "";

    /** Saved Beacon 2 */
    private String savedBeacon2 = "";

    /** variable to log next x beacons */
    private int logNextBeacons = 0;


    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */


    /**
     * Instantiates a new vehicle. You will get an exception if the destinations don't contain at least two <b>valid</b> elements.<br>
     * Elements are considered as invalid if
     * <ul>
     * <li>no route can be found between them and the first destination</li>
     * <li>the destination is on the same street as the first destination</li>
     * </ul>
     *
     * @param destinations		an <code>ArrayDeque</code> with at least 2 elements (start and target) indicating where to move.
     * @param vehicleLength		the vehicle length
     * @param maxSpeed			the maximum speed of this vehicle in cm/s
     * @param maxCommDist		the maximum distance in cm this vehicle can communicate
     * @param wiFiEnabled		<code>true</code> if this vehicle has a communication device (WiFi), else <code>false</code>
     * @param emergencyVehicle	<code>true</code> vehicle is an emergency vehicle
     * @param brakingRate		the braking rate in cm/s^2
     * @param accelerationRate	the acceleration rate in cm/s^2
     * @param color				the color of the vehicle, if empty the default (color.black) is used
     * @throws ParseException an Exception indicating that you did not supply a valid destination list.
     */
    public Vehicle(ArrayDeque<WayPoint> destinations, int vehicleLength, int maxSpeed, int maxCommDist, boolean wiFiEnabled, boolean emergencyVehicle, int brakingRate, int accelerationRate, int timeDistance, int politeness, Color color) throws ParseException {
        if(destinations != null && destinations.size()>1) {
            originalDestinations_ = destinations;
            destinations_ = originalDestinations_.clone();
            ID_ = RANDOM.nextLong();
            steadyID_ = steadyIDCounter++;
            vehicleLength_ = vehicleLength;
            maxSpeed_ = maxSpeed;
            emergencyVehicle_ = emergencyVehicle;
            color_ = color;
            brakingRate_ = brakingRate;
            accelerationRate_ = accelerationRate;
            timeDistance_ = timeDistance;
            politeness_ = politeness;
            maxBrakingDistance_ = maxSpeed_ + maxSpeed_ * maxSpeed_ / (2 * brakingRate_);    // see http://de.wikipedia.org/wiki/Bremsweg
            startingWayPoint_ = destinations_.pollFirst();        // take the first element and remove it from the destinations!
            wiFiEnabled_ = wiFiEnabled;
            ownRandom_ = new Random(RANDOM.nextLong());

            /** 待增加 */
        }

            /** 待增加 */
    }

    /**
     * ///////// getter & setter (start) ///////////
     */
    public static ReportingControlPanel getReportingPanel(){
        if(Renderer.getInstance().isConsoleStart()) return null;
        else return VanetSimStart.getMainControlPanel().getReportingPanel();
    }

    /**
     * Indicates if this vehicle may be reset and reused.
     *
     * @return	<code>true</code> if it may be reused, else <code>false</code>
     */
    public boolean getMayBeRecycled(){
        return mayBeRecycled_;
    }

    /**
     * Gets an array with all streets which will be visited until arriving at the next destination.
     *
     * @return the array with all streets
     */
    public Street[] getRouteStreets(){
        return routeStreets_;
    }

    /**
     * Get the directions corresponding to the array returned <code>getRouteStreets()</code>.
     *
     * @return the array with all directions
     */
    public boolean[] getRouteDirections(){
        return routeDirections_;
    }

    /**
     * Gets the current position in the array returned by <code>getRouteStreets()</code> and <code>getRouteDirections()</code>.
     *
     * @return the position
     */
    public int getRoutePosition(){
        return routePosition_;
    }


    /**
     * Gets the current ID of this vehicle. This ID might change if mixing is enabled!
     *
     * @return the ID
     */
    public int getVehicleID(){
        return 0;
    }

    /**
     * Gets the current communication countdown
     *
     * @return the communication countdown
     */
    public int getCommunicationCountdown(){
        return communicationCountdown_;
    }

    /**
     * Gets the current beacon countdown
     *
     * @return the beacon countdown
     */
    public int getBeaconCountdown(){
        return beaconCountdown_;
    }

    /**
     * Gets the starting point of the vehicle.
     *
     * @return the <code>WayPoint</code>
     */
    public WayPoint getStartPoint() {
        return startingWayPoint_;
    }

    public WayPoint getDestinationPoint()
    {
        ArrayDeque<WayPoint> wayPoints = destinations_.clone();
        return wayPoints.peekLast();
    }

    /**
     * Gets the regions x-coordinate in which this vehicle is found.
     *
     * @return An Integer representing the x-coordinate of the Region
     */
    public int getRegionX() {
        return curRegion_.getX();
    }

    /**
     * Gets the regions y-coordinate in which this vehicle is found.
     *
     * @return An Integer representing the y-coordinate of the Region
     */
    public int getRegionY() {
        return curRegion_.getY();
    }

    /**
     * Gets the maximum speed of this vehicle.
     *
     * @return the maximum speed in cm/s
     */
    public int getMaxSpeed(){
        return maxSpeed_;
    }

    /**
     * Gets the current waittime.
     *
     * @return the current waittime in milliseconds
     */
    public int getWaittime(){
        if(curWaitTime_ < 0) return 0;
        else return curWaitTime_;
    }

    /**
     * Gets the destinations of this vehicle.
     *
     * @return the <code>ArrayDeque</code> with all destinations
     */
    public ArrayDeque<WayPoint> getDestinations(){
        return destinations_;
    }

    /**
     * Gets the maximum communication distance of this vehicle.
     *
     * @return the distance in cm
     */
    public int getMaxCommDistance(){
        return maxCommDistance_;
    }

    /**
     * Returns if this vehicle is currently active.
     *
     * @return <code>true</code> if it's active
     */
    public boolean isActive(){
        return active_;
    }

    /**
     * Returns if this vehicle is currently in a mix zone.
     *
     * @return <code>true</code> if it's in a mix zone
     */
    public boolean isInMixZone(){
        return isInMixZone_;
    }

    /**
     * Returns if this vehicle has WiFi functionality.
     *
     * @return <code>true</code> if it has WiFi
     */
    public boolean isWiFiEnabled(){
        return wiFiEnabled_;
    }

    /**
     * Gets the special data structure with all known messages.
     *
     * @return the data structure
     */
    public KnownMessages getKnownMessages(){
        return knownMessages_;
    }

    /**
     * Gets the special data structure with all known penalties.
     *
     * @return the data structure
     */
    public KnownPenalties getKnownPenalties(){
        return knownPenalties_;
    }

    /**
     * Gets the special data structure with all known vehicles.
     *
     * @return the data structure
     */
    public KnownVehiclesList getKnownVehiclesList(){
        return knownVehiclesList_;
    }

    /**
     * Gets the special data structure with all known RSUs.
     *
     * @return the data structure
     */
    public KnownRSUsList getKnownRSUsList(){
        return knownRSUsList_;
    }

    /**
     * Returns how many messages this vehicle has created.
     *
     * @return the amount
     */
    public int getMessagesCreated(){
        return messagesCreated_;
    }

    /**
     * Returns how often this vehicle has changed it's ID (excluding the initial ID).
     *
     * @return the amount
     */
    public int getIDsChanged(){
        return IDsChanged_;
    }

    /**
     * Gets how long this vehicle traveled. This excludes predefined waiting times but includes
     * all other stops.
     *
     * @return the total time in milliseconds
     */
    public int getTotalTravelTime(){
        return totalTravelTime_;
    }

    public int getAccuWaitTime(){
        return accuWaitTime_;
    }
    public int getAccuBlockingTime(){
        return accuBlockingTime_;
    }

    /**
     * The total distance traveled. This is not completely exact but should suffice in most cases.
     * Small aberration from the real value occur if this vehicle reaches a destination (which should
     * not happen too often).
     *
     * @return the total distance in cm
     */
    public long getTotalTravelDistance(){
        return totalTravelDistance_;
    }

    /**
     * Sets the region in which this vehicle is found.
     *
     * @param region	the region
     */
    public void setRegion(Region region) {
        curRegion_ = region;
    }

    /**
     * Gets the ID used in beacons encoded in HEX so that it's shorter. If the vehicle is not wifi
     * enabled, brackets are used to indicate this.
     *
     * @return the ID as an hex string
     */
    public String getHexID(){
        if(wiFiEnabled_) return Long.toHexString(ID_);
        else return "(" + Long.toHexString(ID_) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Returns the interval between messages.
     *
     * @return the interval in milliseconds
     */
    public static int getCommunicationInterval(){
        return communicationInterval_;
    }

    /**
     * Returns the interval between beacons.
     *
     * @return the interval in milliseconds
     */
    public static int getBeaconInterval(){
        return beaconInterval_;
    }


    /**
     * Signals if communication is enabled.
     *
     * @return	<code>true</code> if communication is enabled, else <code>false</code>
     */
    public static boolean getCommunicationEnabled(){
        return communicationEnabled_;
    }

    /**
     * Signals if recycling of vehicles is enabled or not
     *
     * @return	<code>true</code> if recycling is enabled, else <code>false</code>
     */
    public static boolean getRecyclingEnabled(){
        return recyclingEnabled_;
    }

    /**
     * Signals if beacons are enabled.
     *
     * @return <code>true</code> if beacons are enabled, else <code>false</code>
     */
    public static boolean getBeaconsEnabled(){
        return beaconsEnabled_;
    }

    /**
     * Signals if mix zones are enabled.
     *
     * @return <code>true</code> if mix zones are enabled, else <code>false</code>
     */
    public static boolean getMixZonesEnabled(){
        return mixZonesEnabled_;
    }

    /**
     * If the fallback mode shall be enabled in mix zones. This fallback mode enables the beaconless
     * communication inside mix zones.
     *
     * @return <code>true</code> if the fallback mode is enabled, else <code>false</code>
     */
    public static boolean getMixZonesFallbackEnabled(){
        return mixZonesFallbackEnabled_;
    }

    /**
     * If the fallback mode only sends messages which are in flooding/broadcast mode.
     *
     * @return <code>true</code> if only flooding messages are sent, else <code>false</code>
     */
    public static boolean getMixZonesFallbackFloodingOnly(){
        return mixZonesFallbackFloodingOnly_;
    }

    /**
     * Returns the current routing mode.
     *
     * @return the routing mode
     */
    public static int getRoutingMode(){
        return routingMode_;
    }

    /**
     * Returns the maximum communication distance.
     *
     * @return the maximum communication distance in cm
     */
    public static int getMaximumCommunicationDistance(){
        return maximumCommunicationDistance_;
    }

    /**
     * Gets the minimum time a vehicle needs to have traveled in order to be able to be recycled. Vehicles
     * which travel shorter than this time will NOT get recycled.
     *
     * @return the time in milliseconds
     */
    public static int getMinTravelTimeForRecycling(){
        return minTravelTimeForRecycling_;
    }

    /**
     * Returns the radius of the mix zones.
     *
     * @return the mix zone radius in cm
     */
    public static int getMixZoneRadius(){
        return mixZoneRadius_;
    }

    /**
     * Set the maximum radius of the mix zones.
     *
     * @param maxMixZoneRadius	the maximum radius of the mix zones in cm
     */
    public static void setMaxMixZoneRadius(int maxMixZoneRadius) {
        maxMixZoneRadius_ = maxMixZoneRadius;
    }

    /**
     * Gets the maximum mix zone radius used in the scenario.
     *
     * @return maxMixZoneRadius_ the maximum mix zone radius in cm
     */
    public static int getMaxMixZoneRadius() {
        return maxMixZoneRadius_;
    }


    /**
     * Set the default radius of the mix zones (in the common settings panel).
     *
     * @param mixZoneRadius	the radius of the mix zones in cm
     */
    public static void setMixZoneRadius(int mixZoneRadius){
        mixZoneRadius_ = mixZoneRadius;
    }

    /**
     * Sets the minimum time a vehicle needs to have traveled in order to be able to be recycled. Vehicles
     * which travel shorter than this time will NOT get recycled.
     *
     * @param minTravelTimeForRecycling	the time in milliseconds
     */
    public static void setMinTravelTimeForRecycling(int minTravelTimeForRecycling){
        minTravelTimeForRecycling_ = minTravelTimeForRecycling;
    }

    /**
     * Set the maximum communication distance.
     *
     * @param maximumCommunicationDistance	the maximum communication distance in cm
     */
    public static void setMaximumCommunicationDistance(int maximumCommunicationDistance){
        maximumCommunicationDistance_ = maximumCommunicationDistance;
    }

    /**
     * Sets the reference to all regions. Call this on map reload!
     *
     * @param regions	the array with all regions
     */
    public static void setRegions(Region[][] regions){
        regions_ = regions;
    }

    /**
     * Sets a new value for the communication interval. Common to all vehicles.
     *
     * @param communicationInterval	the new value
     */
    public static void setCommunicationInterval(int communicationInterval){
        communicationInterval_ = communicationInterval;
    }

    /**
     * Sets a new value for the beacon interval. Common to all vehicles.
     *
     * @param beaconInterval	the new value
     */
    public static void setBeaconInterval(int beaconInterval){
        beaconInterval_ = beaconInterval;
    }

    /**
     * Sets if communication is enabled or not. Common to all vehicles.
     *
     * @param state	<code>true</code> to enable communication, else <code>false</code>
     */
    public static void setCommunicationEnabled(boolean state){
        RSU.setCommunicationEnabled(state);
        communicationEnabled_ = state;
    }

    /**
     * Sets if recycling of vehicles is enabled or not. Common to all vehicles.
     *
     * @param state	<code>true</code> to enable recycling, else <code>false</code>
     */
    public static void setRecyclingEnabled(boolean state){
        recyclingEnabled_ = state;
    }

    /**
     * Sets if beacons are enabled or not. Common to all vehicles.
     *
     * @param state	<code>true</code> to enable beacons, else <code>false</code>
     */
    public static void setBeaconsEnabled(boolean state){
        RSU.setBeaconsEnabled(state);
        beaconsEnabled_ = state;
    }

    /**
     * Sets if mix zones are enabled or not. Common to all vehicles.
     *
     * @param state	<code>true</code> to enable mix zones, else <code>false</code>
     */
    public static void setMixZonesEnabled(boolean state){
        mixZonesEnabled_ = state;
    }

    /**
     * Sets if the fallback mode shall be enabled in mix zones. This fallback mode enables the beaconless
     * communication inside mix zones.
     *
     * @param state	<code>true</code> if the fallback mode is enabled, else <code>false</code>
     */
    public static void setMixZonesFallbackEnabled(boolean state){
        mixZonesFallbackEnabled_ = state;
    }

    /**
     * Sets if the fallback mode only sends messages which are in flooding/broadcast mode.
     *
     * @param state	<code>true</code> if only flooding messages are sent, else <code>false</code>
     */
    public static void setMixZonesFallbackFloodingOnly(boolean state){
        mixZonesFallbackFloodingOnly_ = state;
    }

    /**
     * Sets if beacon zones should be monitored or not. Common to all vehicles.
     *
     * @param beaconMonitorEnabled	<code>true</code> to enable monitoring mix zones, else <code>false</code>
     */
    public static void setBeaconMonitorZoneEnabled(boolean beaconMonitorEnabled){
        beaconMonitorEnabled_ = beaconMonitorEnabled;
        RSU.setBeaconMonitorZoneEnabled(beaconMonitorEnabled);
    }

    /**
     * Gets beacon monitor status
     *
     * @return beaconMonitorEnabled_ <code>true</code> if beacon monitor is enabled
     */
    public static boolean getbeaconMonitorEnabled() {
        return beaconMonitorEnabled_;
    }

    /**
     * Sets the values for the monitored beacon zone. A rectangular bounding box within the specified coordinates
     * is monitored if {@link #setBeaconMonitorZoneEnabled(boolean)} is set to <code>true</code>.
     *
     * @param beaconMonitorMinX	the minimum x coordinate
     * @param beaconMonitorMaxX	the maximum x coordinate
     * @param beaconMonitorMinY	the minimum y coordinate
     * @param beaconMonitorMaxY	the maximum y coordinate
     */
    public static void setMonitoredMixZoneVariables(int beaconMonitorMinX, int beaconMonitorMaxX, int beaconMonitorMinY, int beaconMonitorMaxY){
        beaconMonitorMinX_ = beaconMonitorMinX;
        beaconMonitorMaxX_ = beaconMonitorMaxX;
        beaconMonitorMinY_ = beaconMonitorMinY;
        beaconMonitorMaxY_ = beaconMonitorMaxY;
        RSU.setMonitoredMixZoneVariables(beaconMonitorMinX,beaconMonitorMaxX,beaconMonitorMinY,beaconMonitorMaxY);
    }

    /**
     * Gets beacon monitor minX coordinate
     *
     * @return beaconMonitorMinX_ the minX coordinate of the beacon monitor window
     */
    public static int getbeaconMonitorMinX() {
        return beaconMonitorMinX_;
    }

    /**
     * Gets beacon monitor maxX coordinate
     *
     * @return beaconMonitorMaxX_ the maxX coordinate of the beacon monitor window
     */
    public static int getbeaconMonitorMaxX() {
        return beaconMonitorMaxX_;
    }

    /**
     * Gets beacon monitor minY coordinate
     *
     * @return beaconMonitorMinY_ the minY coordinate of the beacon monitor window
     */
    public static int getbeaconMonitorMinY() {
        return beaconMonitorMinX_;
    }

    /**
     * Gets beacon monitor maxY coordinate
     *
     * @return beaconMonitorMaxY_ the maxY coordinate of the beacon monitor window
     */
    public static int getbeaconMonitorMaxY() {
        return beaconMonitorMaxY_;
    }

    /**
     * Gets the report panel for beacon monitoring
     *
     * @return REPORT_PANEL the beacon report panel
     */
    public static ReportingControlPanel getREPORT_PANEL() {
        return REPORT_PANEL;
    }
    /**
     * Sets a new routing mode. See the A_Star_Algor for details. Common to all vehicles.
     *
     * @param mode	the new routing mode
     */
    public static void setRoutingMode(int mode){
        routingMode_ = mode;
    }

    /**
     * Gets the vehicle ID
     *
     * @return ID_ the vehicle ID
     */
    public long getID() {
        return ID_;
    }

    /**
     * Set vehicle WiFi
     *
     * @param wiFiEnabled <code>true</code> to enable WiFi for the vehicle
     */
    public void setWiFiEnabled(boolean wiFiEnabled) {
        wiFiEnabled_ = wiFiEnabled;
    }


    /**
     * Sets the maximum speed
     *
     * @param maxSpeed the speed in cm/s
     */
    public void setMaxSpeed(int maxSpeed) {
        maxSpeed_ = maxSpeed;
    }

    /**
     * Sets the maximum communication distance
     *
     * @param maxCommDistance the maximum communication distance in cm
     */
    public void setMaxCommDistance(int maxCommDistance) {
        maxCommDistance_ = maxCommDistance;
    }

    /**
     * Sets the current wait time.
     *
     * @param curWaitTime the current wait time in ms.
     */
    public void setCurWaitTime(int curWaitTime) {
        curWaitTime_ = curWaitTime;
    }

    /**
     * Gets the current wait time
     *
     * @return curWaitTime_ the current wait time
     */
    public int getCurWaitTime() {
        return curWaitTime_;
    }

    /**
     * Sets the color
     *
     * @param color the new color
     */
    public void setColor(Color color) {
        color_ = color;
    }

    /**
     * Gets the vehicle color.
     *
     * @return color_ the vehicle color.
     */
    public Color getColor() {
        return color_;
    }

    /**
     * Sets the braking rate
     *
     * @param brakingRate the braking rate in cm/s^2
     */
    public void setBrakingRate(int brakingRate) {
        if(brakingRate <= 0) brakingRate_ = 300;
        else brakingRate_ = brakingRate;
    }

    /**
     * Gets the braking rate
     *
     * @return brakingRate_ the braking rate in cm/s^2
     */
    public int getBrakingRate() {
        return brakingRate_;
    }

    /**
     * Sets the acceleration rate
     *
     * @param accelerationRate the acceleration rate in cm/s^2
     */
    public void setAccelerationRate(int accelerationRate) {
        if(accelerationRate <= 0) accelerationRate_ = 800;
        else accelerationRate_ = accelerationRate;
    }

    /**
     * Gets the acceleration rate
     *
     * @return accelerationRate_ the acceleration rate in cm/s^2
     */
    public int getAccelerationRate() {
        return accelerationRate_;
    }

    /**
     * Sets the emergency vehicle mode
     *
     * @param emergencyVehicle <code>true</code> to enable emergency mode
     */
    public void setEmergencyVehicle(boolean emergencyVehicle) {
        emergencyVehicle_ = emergencyVehicle;
    }

    /**
     * Signals if vehicle is an emergency vehicle
     *
     * @return <code>true</code> if vehicle is an emergency vehicle
     */
    public boolean isEmergencyVehicle() {
        return emergencyVehicle_;
    }

    /**
     * Sets the vehicle length
     *
     * @param vehicleLength the vehicle length in cm.
     */
    public void setVehicleLength(int vehicleLength) {
        vehicleLength_ = vehicleLength;
    }

    /**
     * Gets the vehicle length.
     *
     * @return vehicleLength_ the vehicle length in cm.
     */
    public int getVehicleLength() {
        return vehicleLength_;
    }


    public static AttackRSU[] getArsuList() {
        return arsuList;
    }


    public static void setArsuList(AttackRSU[] arsuList) {
        Vehicle.arsuList = arsuList;
    }


    public static boolean isAttackerDataLogged_() {
        return attackerDataLogged_;
    }


    public static void setAttackerDataLogged_(boolean attackerDataLogged_) {
        Vehicle.attackerDataLogged_ = attackerDataLogged_;
    }


    public static long getAttackedVehicleID_() {
        return attackedVehicleID_;
    }


    public static void setAttackedVehicleID_(long attackedVehicleID_) {
        Vehicle.attackedVehicleID_ = attackedVehicleID_;
    }


    public static boolean isEncryptedBeaconsInMix_() {
        return encryptedBeaconsInMix_;
    }


    public static void setEncryptedBeaconsInMix_(boolean encryptedBeaconsInMix_) {
        Vehicle.encryptedBeaconsInMix_ = encryptedBeaconsInMix_;
    }


    public static boolean isAttackerEncryptedDataLogged_() {
        return attackerEncryptedDataLogged_;
    }


    public static void setAttackerEncryptedDataLogged_(
            boolean attackerEncryptedDataLogged_) {
        Vehicle.attackerEncryptedDataLogged_ = attackerEncryptedDataLogged_;
    }


    public Node getCurMixNode_() {
        return curMixNode_;
    }


    public void setCurMixNode_(Node curMixNode_) {
        this.curMixNode_ = curMixNode_;
    }


    /**
     * @return the waitingForSignal_
     */
    public boolean isWaitingForSignal_() {
        return waitingForSignal_;
    }


    /**
     * @param waitingForSignal_ the waitingForSignal_ to set
     */
    public void setWaitingForSignal_(boolean waitingForSignal_) {
        this.waitingForSignal_ = waitingForSignal_;
    }


    public static boolean isPrivacyDataLogged_() {
        return privacyDataLogged_;
    }


    public static void setPrivacyDataLogged_(boolean privacyDataLogged_) {
        Vehicle.privacyDataLogged_ = privacyDataLogged_;
    }

    public void setTimeDistance(int timeDistance) {
        timeDistance_ = timeDistance;
    }


    public int getTimeDistance() {
        return timeDistance_;
    }

    public void setPoliteness(int politeness_) {
        this.politeness_ = politeness_;
    }


    public int getPoliteness() {
        return politeness_;
    }

    public static int getTIME_BETWEEN_SILENT_PERIODS() {
        return TIME_BETWEEN_SILENT_PERIODS;
    }

    public static void setTIME_BETWEEN_SILENT_PERIODS(int i){
        TIME_BETWEEN_SILENT_PERIODS = i;
    }

    public static int getTIME_OF_SILENT_PERIODS() {
        return TIME_OF_SILENT_PERIODS;
    }

    public static void setTIME_OF_SILENT_PERIODS(int i){
        TIME_OF_SILENT_PERIODS = i;
    }

    public static boolean isSilent_period() {
        return silent_period;
    }


    public static void setSilent_period(boolean silent_period) {
        Vehicle.silent_period = silent_period;
    }


    public static boolean isSilentPeriodsOn() {
        return silentPeriodsOn;
    }


    public static void setSilentPeriodsOn(boolean silentPeriodsOn) {
        Vehicle.silentPeriodsOn = silentPeriodsOn;
    }

    /**
     * ///////// getter & setter (end) ///////////
     */

}
