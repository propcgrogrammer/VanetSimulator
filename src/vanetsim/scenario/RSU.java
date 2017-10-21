package vanetsim.scenario;

import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.ReportingControlPanel;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.Street;
import vanetsim.scenario.messages.Message;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashMap;

//import java16.util.ArrayDeque;


/**
 * A Road-Side-Unit to send and receive WiFi signals.
 */

public final class RSU {
	
	/** A reference to the reporting control panel so that we don't need to call this over and over again. */
	private static final ReportingControlPanel REPORT_PANEL = Vehicle.getREPORT_PANEL();
	
	/** A common counter to generate unique IDs */
	private static int counter_ = 1;
	
	/** How long a Road-Side-Unit waits to communicate again (in milliseconds). Also used for cleaning up outdated known messages. */
	private static int communicationInterval_ = Vehicle.getCommunicationInterval();

	/** How long a Road-Side-Unit waits to send its beacons again. */
	private static int beaconInterval_ = Vehicle.getBeaconInterval();
	
	/** If communication is enabled */
	private static boolean communicationEnabled_ = Vehicle.getCommunicationEnabled();

	/** If beacons are enabled */
	private static boolean beaconsEnabled_ = Vehicle.getBeaconsEnabled();
	
	/** A reference to the map so that we don't need to call this over and over again. */
	private static final Map MAP = Map.getInstance();
	
	/** An array holding all regions of the map. */
	private static Region[][] regions_;
	
	/** When known vehicles are rechecked for outdated entries. Measured in milliseconds. */
	private static final int KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL = 1000;
	
	/** A unique ID for this Road-Side-Unit */
	private final long rsuID_;
	
	/** The x coordinate. */
	private final int x_;
	
	/** The y coordinate. */
	private final int y_;	
	
	/** The wifi radius */
	private final int wifiRadius_;
	
	/** If the RSU is sending encrypted Messages */
	private final boolean isEncrypted_;
	
	/** The region in which this Road-Side-Unit is. */
	private Region region_;
	
	/** A countdown for sending beacons. */
	private int beaconCountdown_;
	
	/** A countdown for communication. Also used for cleaning up outdated known messages. */
	private int communicationCountdown_;
	
	/** A countdown for rechecking if known vehicles are outdated. */
	private int knownVehiclesTimeoutCountdown_;
	
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

	/** A class storing messages of different states: execute, forward and old ones. Also used in the Vehicle class */
	private final KnownMessages knownMessages_ = new KnownMessages();

	/** A list of all vehicles currently known because of received beacons. */
	private final KnownVehiclesList knownVehiclesList_ = new KnownVehiclesList();
	
	/** activates demonstration mode of encrypted Mix-Zones */
	private static boolean showEncryptedBeaconsInMix_ = false;
	
	/** static array to save the colored vehicles that are behind the marked vehicle */
	private Vehicle[] vehicleBehind_;
	
	/** static array to save the colored vehicles that are front the marked vehicle */
	private Vehicle[] vehicleFront_;
	
	/** static array to save the colored vehicles that are toward the marked vehicle */
	private Vehicle[] vehicleToward_;
	
	/** saves all colored vehicles */
	public static ArrayDeque<Vehicle> coloredVehicles = new ArrayDeque<Vehicle>();
	
	public static RSU lastSender = null;
	
	/** flag to clear vehicle color */
	public static boolean colorCleared = false;
	
	/**
	 * Instantiates a new RSU.
	 * 
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param radius the signal radius
	 * @param isEncrypted if message is encrypted
	 */
	public RSU(int x, int y, int radius, boolean isEncrypted) {
		x_ = x;
		y_ = y;
		wifiRadius_ = radius;
		isEncrypted_ = isEncrypted;
		rsuID_ = counter_;
		++counter_;
		
		//set the countdowns so that not all fire at the same time!
		beaconCountdown_ = (int)Math.round(x_)%beaconInterval_;
		communicationCountdown_ = (int)Math.round(x_)%communicationInterval_;
		knownVehiclesTimeoutCountdown_ = (int)Math.round(x_)%KNOWN_VEHICLES_TIMEOUT_CHECKINTERVAL;
	}
	
	/**
	 * send messages to all vehicles in reach. Uses broadcast, because vehicles cannot send beacons
	 * to the RSUs(often to far away)
	 */
	public void sendMessages(){
		/** 待增加 */
	}
	

	/**
	 * Find vehicles in neighborhood and send beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if the beacon countdown is 0 or less</li>
	 * </ul>
	 */
	public void sendBeacons(){
		/** 待增加 */
	}
	
	/**
	 * Find vehicles in neighborhood and send beacons to them. Please check the following conditions before calling this function:
	 * <ul>
	 * <li>communication is generally enabled</li>
	 * <li>beacons are generally enabled</li>
	 * <li>if the beacon countdown is 0 or less</li>
	 * </ul>
	 */
	public void sendEncryptedBeacons(){
		/** 待增加 */
	}	

	
	/**
	 * Receive a message from a vehicle.
	 * 
	 * @param sourceX	the x coordinate of the other vehicle
	 * @param sourceY	the y coordinate of the other vehicle
	 * @param message	the message
	 */
	public final void receiveMessage(int sourceX, int sourceY, Message message){
		//set broadcast mode, otherwise all vehicles would forward the broadcasted message (performance)
		message.setFloodingMode(true);
		
		//only redirect all messages
		knownMessages_.addMessage(message, false, true);
	}
	
	
	/**
	 * Cleanup all outdated messages
	 * 
	 * @param timePerStep	the actual time per step
	 */
	public void cleanup(int timePerStep){
		/** 待增加 */
	}
	
	/**
	 * Resets this rsu so that it can be reused.
	
	public void reset(){
		//reset countdowns and other variables
		communicationCountdown_ = 0;
		knownVehiclesTimeoutCountdown_ = 0;
		beaconCountdown_ = (int)Math.round(x_)%beaconInterval_;
		communicationCountdown_ = (int)Math.round(x_)%communicationInterval_;
		
		//reset communication info
		knownVehiclesList_.clear();
		knownMessages_.clear();
	}
	/*
	
	/**
	 * Returns the Road-Side-Unit id
	 * 
	 * @return the RSU id
	 */
	public long getRSUID() {
		return rsuID_;
	}

	/**
	 * Returns the x coordinate of the RSU
	 * 
	 * @return the x coordinate
	 */
	public int getX() {
		return x_;
	}

	/**
	 * Returns the y coordinate of the RSU
	 * 
	 * @return the y coordinate
	 */
	public int getY() {
		return y_;
	}

	/**
	 * Sets the region the RSU is placed
	 * 
	 * @param region	the region
	 */
	public void setRegion(Region region) {
		region_ = region;
	}

	/**
	 * Returns the region the RSU is placed in
	 * 
	 * @return the region
	 */
	public Region getRegion() {
		return region_;
	}

	/**
	 * Returns the wifi radius
	 * 
	 * @return the wifi radius in cm
	 */
	public int getWifiRadius() {
		return wifiRadius_;
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
	 * Gets the current communication countdown
	 * 
	 * @return the communication countdown
	 */
	public int getCommunicationCountdown(){
		return communicationCountdown_;
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
	 * Sets if beacons are enabled or not. Common to all Road-Side-Units.
	 * 
	 * @param state	<code>true</code> to enable beacons, else <code>false</code> 
	 */
	public static void setBeaconsEnabled(boolean state){
		beaconsEnabled_ = state;
	}
	
	/**
	 * Sets if communication is enabled or not. Common to all Road-Side-Units.
	 * 
	 * @param state	<code>true</code> to enable communication, else <code>false</code> 
	 */
	public static void setCommunicationEnabled(boolean state){
		communicationEnabled_ = state;
	}
	
	/**
	 * Sets a new value for the communication interval. Common to all Road-Side-Units.
	 * 
	 * @param communicationInterval	the new value 
	 */
	public static void setCommunicationInterval(int communicationInterval){
		communicationInterval_ = communicationInterval;
	}

	/**
	 * Sets a new value for the beacon interval. Common to all Road-Side-Units.
	 * 
	 * @param beaconInterval	the new value 
	 */
	public static void setBeaconInterval(int beaconInterval){
		beaconInterval_ = beaconInterval;
	}
	
	/**
	 * Sets if beacon zones should be monitored or not. Common to all RSUs.
	 * 
	 * @param beaconMonitorEnabled	<code>true</code> to enable monitoring mix zones, else <code>false</code> 
	 */
	public static void setBeaconMonitorZoneEnabled(boolean beaconMonitorEnabled){
		beaconMonitorEnabled_ = beaconMonitorEnabled;
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
	}

	public boolean isEncrypted_() {
		return isEncrypted_;
	}

	public KnownVehiclesList getKnownVehiclesList_() {
		return knownVehiclesList_;
	}

	public static boolean isShowEncryptedBeaconsInMix_() {
		return showEncryptedBeaconsInMix_;
	}

	public static void setShowEncryptedBeaconsInMix_(
			boolean showEncryptedBeaconsInMix_) {
		RSU.showEncryptedBeaconsInMix_ = showEncryptedBeaconsInMix_;
	}
}