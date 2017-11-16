package vanetsim.gui.controlpanels;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.scenario.RSU;
import vanetsim.scenario.Vehicle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.DecimalFormat;

/**
 * This class contains the control elements for display of statistics and mix zone information
 */
public class ReportingControlPanel extends JPanel implements ActionListener, ItemListener {


    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */

    /** The necessary constant for serializing. */
    private static final long serialVersionUID = 5121974914528330821L;

    /** How often statistics are updated. Measured in milliseconds. */
    private static final int STATISTICS_ACTUALIZATION_INTERVAL = 500;

    /** How often beacon zone information are updated. Measured in milliseconds. */
    private static final int BEACONINFO_ACTUALIZATION_INTERVAL = 500;

    /** A formatter for integers without fractions */
    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat(",##0"); //$NON-NLS-1$

    /** A formatter for integers with fractions */
    private static final DecimalFormat INTEGER_FORMAT_FRACTION = new DecimalFormat(",##0.00"); //$NON-NLS-1$

    /** An area to display the statistics. */
    private final JTextArea statisticsTextArea_;

    /** A checkbox to enable/disable autoupdating the . */
    private final JCheckBox autoUpdateStatisticsCheckBox_;

    /** An area to display the information about vehicles sending beacons. */
    private final JTextArea beaconInfoTextArea_;

    /** The scrollbar used for scrolling through the information about the beacon zone. Stored
     * so that it's possilbe to autoscroll. */
    private final JScrollBar beaconInfoVerticalScrollBar_;

    /** A checkbox to enable/disable monitoring the zone which is monitored for beacons. */
    private final JCheckBox doMonitorBeaconsCheckBox_;

    /** A checkbox to enable/disable editing the zone which is monitored for beacons. */
    private final JCheckBox monitoredBeaconZoneEditCheckBox_;

    /** A checkbox to enable/disable showing the zone which is monitored for beacons. */
    private final JCheckBox monitoredBeaconZoneShowCheckBox_;

    /** The StringBuilder for the display of statistics information. */
    private final StringBuilder statisticsText_ = new StringBuilder();

    /** The StringBuilder for the display of beacon info information. */
    private final StringBuilder beaconInfoText_ = new StringBuilder();

    /** If this panel is currently active. */
    private boolean active_ = false;

    /** If statistics are regularly updated. */
    private boolean updateStatistics_ = false;

    /** If beacons are regularly updated. */
    private boolean updateBeaconInfo_ = false;

    /** If monitored beacon zone edit mode is enabled or not */
    private boolean monitoredBeaconZoneEdit_ = false;

    /** The last x coordinate where mouse was pressed. */
    private int lastPressedX_ = -1;

    /** The last y coordinate where mouse was pressed. */
    private int lastPressedY_ = -1;

    /** A countdown for the statistics actualization */
    private int statisticsCountdown_ = 0;

    /** A countdown for the beacon zone actualization */
    private int beaconInfoCountdown_ = 0;


    /** JButton to open log cleaner. The log cleaner will search a log file and replace all coordinates with port names (like "1") */
    private final JButton privacyLogCleaner_;

    /** JButton to open log analyser. */
    private final JButton privacyLogAnalyzer_;

    /** FileFilter to choose only ".log" files from FileChooser */
    private FileFilter logFileFilter_;





    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */

    /**
     * Constructor for this control panel.
     */
    public ReportingControlPanel(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "ReportingControlPanel())", Debug.ISLOGGED);


        setLayout(new GridBagLayout());


        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 0.5;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.insets = new Insets(5,5,5,5);

        c.gridwidth = 3;

        //text area for display of statistics.
        JLabel jLabel1 = new JLabel("<html><u><b>" + Messages.getString("ReportingControlPanel.statistics") + "</u></b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ++c.gridy;
        add(jLabel1, c);
        c.gridwidth = 2;
        autoUpdateStatisticsCheckBox_ = new JCheckBox(Messages.getString("ReportingControlPanel.autoupdateStatistics"), false); //$NON-NLS-1$
        autoUpdateStatisticsCheckBox_.addItemListener(this);
        ++c.gridy;
        add(autoUpdateStatisticsCheckBox_,c);
        statisticsTextArea_ = new JTextArea(11,1);
        statisticsTextArea_.setEditable(false);
        statisticsTextArea_.setLineWrap(true);
        JScrollPane scrolltext = new JScrollPane(statisticsTextArea_);
        scrolltext.setMinimumSize(new Dimension(180,300));
        c.fill = GridBagConstraints.HORIZONTAL;
        ++c.gridy;
        add(scrolltext, c);

        c.gridwidth = 1;
        c.gridx = 1;
        ++c.gridy;
        c.fill = GridBagConstraints.NONE;
        JPanel tmpPanel = new JPanel();
        tmpPanel.add(ButtonCreator.getJButton("refresh.png", "refresh", Messages.getString("ReportingControlPanel.refresh"), this)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        tmpPanel.add(ButtonCreator.getJButton("clipboard.png", "copyStatisticsInfo", Messages.getString("ReportingControlPanel.copyStatisticsToClipboard"), this)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        add(tmpPanel, c);


        c.gridwidth = 3;
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        jLabel1 = new JLabel("<html><u><b>" + Messages.getString("ReportingControlPanel.monitoredBeaconZoneInfo") + "</u></b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ++c.gridy;
        c.insets = new Insets(25,5,5,5);
        add(jLabel1, c);
        c.insets = new Insets(5,5,0,5);

        doMonitorBeaconsCheckBox_ = new JCheckBox(Messages.getString("ReportingControlPanel.enableMonitoring"), false); //$NON-NLS-1$
        doMonitorBeaconsCheckBox_.addItemListener(this);
        ++c.gridy;
        add(doMonitorBeaconsCheckBox_,c);

        monitoredBeaconZoneShowCheckBox_ = new JCheckBox(Messages.getString("ReportingControlPanel.showMonitoredZone"), false); //$NON-NLS-1$
        monitoredBeaconZoneShowCheckBox_.addItemListener(this);
        ++c.gridy;
        add(monitoredBeaconZoneShowCheckBox_,c);

        monitoredBeaconZoneEditCheckBox_ = new JCheckBox(Messages.getString("ReportingControlPanel.editMonitoredZone"), false); //$NON-NLS-1$
        monitoredBeaconZoneEditCheckBox_.addItemListener(this);
        ++c.gridy;
        add(monitoredBeaconZoneEditCheckBox_,c);

        c.insets = new Insets(5,5,5,5);
        //text area for display of vehicles leaving mix zones.
        beaconInfoTextArea_ = new JTextArea(25,1);
        beaconInfoTextArea_.setEditable(false);
        beaconInfoTextArea_.setText(Messages.getString("ReportingControlPanel.legend")); //$NON-NLS-1$
        beaconInfoTextArea_.setLineWrap(true);
        scrolltext = new JScrollPane(beaconInfoTextArea_);
        scrolltext.setMinimumSize(new Dimension(180,300));
        scrolltext.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        beaconInfoVerticalScrollBar_ = scrolltext.getVerticalScrollBar();
        ++c.gridy;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(scrolltext, c);

        c.gridwidth = 1;
        c.gridx = 1;
        ++c.gridy;
        c.fill = GridBagConstraints.NONE;
        tmpPanel = new JPanel();
        tmpPanel.add(ButtonCreator.getJButton("delete.png", "deleteBeaconInfo", Messages.getString("ReportingControlPanel.deleteBeaconInfo"), this)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        tmpPanel.add(ButtonCreator.getJButton("clipboard.png", "copyBeaconInfo", Messages.getString("ReportingControlPanel.copyBeaconToClipBoard"), this)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        add(tmpPanel, c);

        //log analyser
        c.gridwidth = 2;
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        ++c.gridy;
        c.insets = new Insets(25,5,5,5);
        add(new JLabel("<html><u><b>" + Messages.getString("ReportingControlPanel.analyseLog") + "</u></b></html>"), c);
        c.insets = new Insets(5,5,0,5);

        //exchanges coordinates with port numbers in mix-zone-logs
        ++c.gridy;
        c.gridx = 0;
        privacyLogCleaner_ = new JButton(Messages.getString("EditLogControlPanel.privacyLogCleanerButton"));
        privacyLogCleaner_.setActionCommand("cleanLog");
        privacyLogCleaner_.setPreferredSize(new Dimension(200,20));
        privacyLogCleaner_.addActionListener(this);
        add(privacyLogCleaner_,c);

        //opens the log-analyzer component
        ++c.gridy;
        c.gridx = 0;
        privacyLogAnalyzer_ = new JButton(Messages.getString("EditLogControlPanel.privacyLogAnalyzerButton"));
        privacyLogAnalyzer_.setActionCommand("openAnalyzer");
        privacyLogAnalyzer_.setPreferredSize(new Dimension(200,20));
        privacyLogAnalyzer_.addActionListener(this);
        add(privacyLogAnalyzer_,c);

        //sums up the length of all streets on the map
        ++c.gridy;
        c.gridx = 0;
        JButton calculateStreetLengthButton = new JButton(Messages.getString("ReportingControlPanel.calculateStreetLength"));
        calculateStreetLengthButton.setActionCommand("openStreetLengthCalculator");
        calculateStreetLengthButton.setPreferredSize(new Dimension(200,20));
        calculateStreetLengthButton.addActionListener(this);
        add(calculateStreetLengthButton,c);

        //show the location information on a map
        ++c.gridy;
        c.gridx = 0;
        JButton advancedLocationInformation = new JButton(Messages.getString("ReportingControlPanel.showAdvancedLocationInformation"));
        advancedLocationInformation.setActionCommand("showAdvancedLocationInformation");
        advancedLocationInformation.setPreferredSize(new Dimension(200,20));
        advancedLocationInformation.addActionListener(this);
        add(advancedLocationInformation,c);

        //define FileFilter for fileChooser
        logFileFilter_ = new FileFilter(){
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return f.getName().toLowerCase().endsWith(".log"); //$NON-NLS-1$
            }
            public String getDescription () {
                return Messages.getString("EditLogControlPanel.logFiles") + " (*.log)"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };

        //to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        add(new JPanel(), c);
    }


    /**
     * This function should be called after each simulation step to determine if an update of statistics/beacon information is necessary
     *
     * @param timePerStep	the time of one simulation step in milliseconds
     */
    public void checkUpdates(int timePerStep){

        Debug.callFunctionInfo(this.getClass().getName(),"checkUpdates(int timePerStep)",Debug.ISLOGGED);

        if(updateStatistics_ || statisticsCountdown_ == -1){
            statisticsCountdown_ -= timePerStep;
            if(statisticsCountdown_ < 1){
                statisticsCountdown_ += STATISTICS_ACTUALIZATION_INTERVAL;
                updateStatistics();
            }
        }
        if(updateBeaconInfo_){
            beaconInfoCountdown_ -= timePerStep;
            if(beaconInfoCountdown_ < 1){
                beaconInfoCountdown_ += BEACONINFO_ACTUALIZATION_INTERVAL;
                updateBeaconInfo();
            }
        }
    }

    /**
     * Updates the statistics. You need to make sure that the vehicles are not modified while executing this.
     */
    private final void updateStatistics(){

        Debug.callFunctionInfo(this.getClass().getName(),"updateStatistics()",Debug.ISLOGGED);


        statisticsText_.setLength(0); 	//reset

        Region[][] regions = Map.getInstance().getRegions();
        Vehicle[] vehicles;
        Vehicle vehicle;
        int i, j, k;
        int activeVehicles = 0;
        int travelledVehicles = 0;
        int wifiVehicles = 0;
        long messagesCreated = 0;
        long IDsChanged = 0;
        double messageForwardFailed = 0;
        double travelDistance = 0;
        double travelTime = 0;
        double speed = 0;
        double knownVehicles = 0;
        for(i = 0; i < regions.length; ++i){
            for(j = 0; j < regions[i].length; ++j){
                vehicles = regions[i][j].getVehicleArray();
                for(k = 0; k < vehicles.length; ++k){
                    vehicle = vehicles[k];


                    if(vehicle.getTotalTravelTime() > 0){
                        ++travelledVehicles;
                        travelDistance += vehicle.getTotalTravelDistance();
                        travelTime += vehicle.getTotalTravelTime();
                    }
                    if(vehicle.isActive()){
                        ++activeVehicles;
                        speed += vehicle.getCurSpeed();
                        if(vehicle.isWiFiEnabled()){
                            ++wifiVehicles;
                            messageForwardFailed += vehicle.getKnownMessages().getFailedForwardCount();
                            knownVehicles += vehicle.getKnownVehiclesList().getSize();
                            IDsChanged += vehicle.getIDsChanged();
                            messagesCreated += vehicle.getMessagesCreated();
                        }
                    }
                }
            }
        }
        statisticsText_.append(Messages.getString("ReportingControlPanel.currentTime")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(Renderer.getInstance().getTimePassed()));
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.activeVehicles")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(activeVehicles));
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.averageSpeed")); //$NON-NLS-1$
        if(activeVehicles > 0) statisticsText_.append(INTEGER_FORMAT_FRACTION.format(speed/activeVehicles/100000*3600));
        else statisticsText_.append("0"); //$NON-NLS-1$
        statisticsText_.append(" km/h\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.averageTravelDistance")); //$NON-NLS-1$
        if(travelledVehicles > 0) statisticsText_.append(INTEGER_FORMAT_FRACTION.format(travelDistance/travelledVehicles/100));
        else statisticsText_.append("0"); //$NON-NLS-1$
        statisticsText_.append(" m\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.averageTravelTime")); //$NON-NLS-1$
        if(travelledVehicles > 0) statisticsText_.append(INTEGER_FORMAT_FRACTION.format(travelTime/travelledVehicles/1000));
        else statisticsText_.append("0"); //$NON-NLS-1$
        statisticsText_.append(" s\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.wifiVehicles")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(wifiVehicles));
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.averageKnownVehicles")); //$NON-NLS-1$
        if(wifiVehicles > 0) statisticsText_.append(INTEGER_FORMAT_FRACTION.format(knownVehicles/wifiVehicles));
        else statisticsText_.append("0"); //$NON-NLS-1$
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.uniqueMessages")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(messagesCreated));
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.failedMessages")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(messageForwardFailed));
        statisticsText_.append("\n"); //$NON-NLS-1$
        statisticsText_.append(Messages.getString("ReportingControlPanel.totalIDchanges")); //$NON-NLS-1$
        statisticsText_.append(INTEGER_FORMAT.format(IDsChanged));
        statisticsText_.append("\n"); //$NON-NLS-1$

        statisticsTextArea_.setText(statisticsText_.toString());
    }

    /**
     * Updates the statistics. You need to make sure that the vehicles are not modified while executing this.
     */
    public void updateBeaconInfo(){
        boolean autoScroll;
        int scrollDiff = beaconInfoVerticalScrollBar_.getValue() + beaconInfoVerticalScrollBar_.getVisibleAmount() - beaconInfoVerticalScrollBar_.getMaximum();
        if(scrollDiff > -10) autoScroll = true;
        else autoScroll = false;
        beaconInfoTextArea_.append(beaconInfoText_.toString());
        if(autoScroll) beaconInfoTextArea_.setCaretPosition(beaconInfoTextArea_.getDocument().getLength());
        beaconInfoText_.setLength(0);
    }

    /**
     * Receive a beacon from a vehicle in the monitored zone.
     *
     * @param vehicle	the vehicle
     * @param ID			the ID of the vehicle
     * @param x				the x coordinate of the vehicle
     * @param y				the y coordinate of the vehicle
     * @param speed			the speed of the vehicle
     * @param isEncrypted	if the beacon is encrypted
     */
    public synchronized void addBeacon(Vehicle vehicle, long ID, long x, long y, double speed, boolean isEncrypted){
        beaconInfoText_.append("\n\nVehicle\n"); //$NON-NLS-1$
        beaconInfoText_.append(Renderer.getInstance().getTimePassed());
        beaconInfoText_.append("ms\n"); //$NON-NLS-1$
        beaconInfoText_.append(Long.toHexString(ID));
        beaconInfoText_.append(","); //$NON-NLS-1$
        beaconInfoText_.append(speed);
        beaconInfoText_.append("\n"); //$NON-NLS-1$
        beaconInfoText_.append(x);
        beaconInfoText_.append(","); //$NON-NLS-1$
        beaconInfoText_.append(y);
        beaconInfoText_.append(", encrypted:"); //$NON-NLS-1$
        beaconInfoText_.append(isEncrypted);
    }

    /**
     * Receive a beacon from a vehicle in the monitored zone.
     *
     * @param vehicle	the vehicle
     * @param ID			the ID of the vehicle
     * @param x				the x coordinate of the vehicle
     * @param y				the y coordinate of the vehicle
     * @param speed			the speed of the vehicle
     * @param isEncrypted	if the beacon is encrypted
     * @param isForwared	if a beacon is forwared by an RSU
     */
    public synchronized void addBeacon(Vehicle vehicle, long ID, long x, long y, double speed, boolean isEncrypted, boolean isForwared){
        beaconInfoText_.append("\n\nVehicle (forwarded by RSU) \n"); //$NON-NLS-1$
        beaconInfoText_.append(Renderer.getInstance().getTimePassed());
        beaconInfoText_.append("ms\n"); //$NON-NLS-1$
        beaconInfoText_.append(Long.toHexString(ID));
        beaconInfoText_.append(","); //$NON-NLS-1$
        beaconInfoText_.append(speed);
        beaconInfoText_.append("\n"); //$NON-NLS-1$
        beaconInfoText_.append(x);
        beaconInfoText_.append(","); //$NON-NLS-1$
        beaconInfoText_.append(y);
        beaconInfoText_.append(", encrypted:"); //$NON-NLS-1$
        beaconInfoText_.append(isEncrypted);
    }

    /**
     * Receive a beacon from a RSU in the monitored zone.
     *
     * @param rsu	the RSU
     * @param ID		the ID of the vehicle
     * @param x			the x coordinate of the vehicle
     * @param y			the y coordinate of the vehicle
     * @param isEncrypted	if Beacon was encrypted
     */
    public synchronized void addBeacon(RSU rsu, long ID, long x, long y, boolean isEncrypted){
        beaconInfoText_.append("\n\nRSU\n"); //$NON-NLS-1$
        beaconInfoText_.append(Renderer.getInstance().getTimePassed());
        beaconInfoText_.append("ms\n"); //$NON-NLS-1$
        beaconInfoText_.append(ID);
        beaconInfoText_.append("\n"); //$NON-NLS-1$
        beaconInfoText_.append(x);
        beaconInfoText_.append(","); //$NON-NLS-1$
        beaconInfoText_.append(y);
        beaconInfoText_.append(", encrypted:"); //$NON-NLS-1$
        beaconInfoText_.append(isEncrypted);
    }

    /**
     * /////////////  事件觸發方法(start) //////////
     */

    /**
     * Receives a mouse event for changing the monitored mix zone.
     *
     * @param x	the x coordinate
     * @param y	the y coordinate
     */
    public void receiveMouseEvent(int x, int y){
        if(x < 0) x = 0;
        else if(x > Map.getInstance().getMapWidth()) x = Map.getInstance().getMapWidth();
        if(y < 0) y = 0;
        else if(y > Map.getInstance().getMapHeight()) y = Map.getInstance().getMapHeight();
        if(lastPressedX_ == -1 && lastPressedY_ == -1){
            lastPressedX_ = x;
            lastPressedY_ = y;
        } else {
            //get bounding box variables
            int minX = Math.min(lastPressedX_, x);
            int maxX = Math.max(lastPressedX_, x);
            int minY = Math.min(lastPressedY_, y);
            int maxY = Math.max(lastPressedY_, y);
            Vehicle.setMonitoredMixZoneVariables(minX, maxX, minY, maxY);
            Renderer.getInstance().setMonitoredBeaconZoneVariables(minX, maxX, minY, maxY);
            if(monitoredBeaconZoneShowCheckBox_.isSelected()) Renderer.getInstance().ReRender(false, false);
            lastPressedX_ = -1;
            lastPressedY_ = -1;
        }
    }


    /**
     * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
     * is clicked.
     *
     * @param e an <code>ActionEvent</code>
     */
    @Override
    public void actionPerformed(ActionEvent e) {


    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }
    /**
     * /////////////  事件觸發方法(end) //////////
     */

    /**
     * ///////////// getter & setter (start) ////////
     */
    /**
     * Sets if this panel is currently active
     *
     * @param active	<code>true</code> if it is active, else <code>false</code>
     */
    public void setActive(boolean active){
        active_ = active;
    }

    /**
     * ///////////// getter & setter (end) ////////
     */


}
