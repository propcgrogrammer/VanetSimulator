package vanetsim.gui.controlpanels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Random;
import java.util.ArrayDeque;


//import java16.util.ArrayDeque;

import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.gui.helpers.VehicleType;
import vanetsim.gui.helpers.VehicleTypeXML;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding random vehicles.
 */
public class EditVehicleControlPanel extends JPanel implements ActionListener, MouseListener{

    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */
    /** The necessary constant for serializing. */
    private static final long serialVersionUID = 1347869556374738481L;

    /** A JComboBox Label for vehicle type. */
    private JLabel chooseVehicleTypeLabel_;

    /** A JComboBox to switch between vehicles types. */
    private JComboBox chooseVehicleType_;

    /** The input field for the vehicle length (cm) */
    private final JFormattedTextField vehicleLength_;

    /** The input field for the minimum speed. */
    private final JFormattedTextField minSpeed_;

    /** The input field for the maximum speed. */
    private final JFormattedTextField maxSpeed_;

    /** The input field for the minimum communication distance. */
    private final JFormattedTextField minCommDist_;

    /** The input field for the maximum communication distance.. */
    private final JFormattedTextField maxCommDist_;

    /** The input field for the minimum wait in milliseconds. */
    private final JFormattedTextField minWait_;

    /** The input field for the maximum wait in milliseconds. */
    private final JFormattedTextField maxWait_;

    /** The input field for the minimum braking rate in cm/s^2. */
    private final JFormattedTextField minBraking_;

    /** The input field for the maximum braking rate in cm/s^2. */
    private final JFormattedTextField maxBraking_;

    /** The input field for the minimum acceleration rate in cm/s^2. */
    private final JFormattedTextField minAcceleration_;

    /** The input field for the maximum acceleration rate in cm/s^2. */
    private final JFormattedTextField maxAcceleration_;

    /** The input field for the minimum time distance in ms. */
    private final JFormattedTextField minTimeDistance_;

    /** The input field for the maximum time distance in ms. */
    private final JFormattedTextField maxTimeDistance_;

    /** The input field for the minimum politeness factor in %. */
    private final JFormattedTextField minPoliteness_;

    /** The input field for the maximum politeness factor in %. */
    private final JFormattedTextField maxPoliteness_;

    /** The input field for the percentage of vehicles with WiFi. */
    private final JFormattedTextField wiFi_;

    /** The input field for the percentage of emergency vehicles. */
    private final JFormattedTextField emergencyVehicle_;

    /** The input field for the amount of vehicles to be created. */
    private final JFormattedTextField amount_;

    /** The input field for a restriction that source an destination may only be on specific streets. */
    private final JFormattedTextField speedStreetRestriction_;


    /** The input field for the wait in milliseconds. */
    private final JPanel colorPreview_;


    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */
    /**
     * Constructor.
     */
    public EditVehicleControlPanel(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "EditVehicleControlPanel()", Debug.ISLOGGED);

        setLayout(new GridBagLayout());

        // global layout settings
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.insets = new Insets(5,5,5,5);

        //add vehicle types comboBox
        chooseVehicleTypeLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.selectVehicleType")); //$NON-NLS-1$
        ++c.gridy;
        add(chooseVehicleTypeLabel_,c);
        chooseVehicleType_ = new JComboBox();
        chooseVehicleType_.setName("chooseVehicleType");
        //load vehicle types from vehicleTypes.xml into JCombobox
        refreshVehicleTypes();

        chooseVehicleType_.addActionListener(this);
        c.gridx = 1;
        add(chooseVehicleType_, c);

        //add vehicle properties
        c.gridx = 0;
        JLabel jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minSpeed")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minSpeed_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minSpeed_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxSpeed")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxSpeed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxSpeed_.setPreferredSize(new Dimension(60,20));;
        c.gridx = 1;
        add(maxSpeed_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minCommDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minCommDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minCommDist_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minCommDist_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxCommDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxCommDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxCommDist_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxCommDist_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minWaittime")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minWait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minWait_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minWait_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxWaittime")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxWait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxWait_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxWait_,c);


        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minBraking_rate")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minBraking_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minBraking_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minBraking_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxBraking_rate")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxBraking_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxBraking_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxBraking_,c);


        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minAcceleration_rate")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minAcceleration_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minAcceleration_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minAcceleration_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxAcceleration_rate")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxAcceleration_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxAcceleration_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxAcceleration_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minTimeDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minTimeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minTimeDistance_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minTimeDistance_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxTimeDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxTimeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxTimeDistance_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxTimeDistance_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.minPoliteness")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        minPoliteness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        minPoliteness_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(minPoliteness_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.maxPoliteness")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        maxPoliteness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        maxPoliteness_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(maxPoliteness_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.vehicleLength")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        vehicleLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        vehicleLength_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(vehicleLength_,c);


        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.wiFiVehicles")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        wiFi_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        wiFi_.setPreferredSize(new Dimension(60,20));
        wiFi_.setValue(100);
        c.gridx = 1;
        add(wiFi_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.emergencyVehicles")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        emergencyVehicle_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        emergencyVehicle_.setPreferredSize(new Dimension(60,20));
        emergencyVehicle_.setValue(0);
        c.gridx = 1;
        add(emergencyVehicle_,c);

        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditVehicleControlPanel.amount")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        amount_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        amount_.setPreferredSize(new Dimension(60,20));
        amount_.setValue(100);
        c.gridx = 1;
        add(amount_,c);


        c.gridx = 0;
        jLabel1 = new JLabel(Messages.getString("EditOneVehicleControlPanel.color")); //$NON-NLS-1$
        ++c.gridy;
        add(jLabel1,c);
        colorPreview_ = new JPanel();
        colorPreview_.setBackground(Color.black);
        colorPreview_.setSize(10, 10);
        colorPreview_.addMouseListener(this);
        c.gridx = 1;
        add(colorPreview_,c);


        c.gridx = 0;
        jLabel1 = new JLabel("<html>" + Messages.getString("EditVehicleControlPanel.onlyOnLowerSpeedStreets")); //$NON-NLS-1$ //$NON-NLS-2$
        ++c.gridy;
        add(jLabel1,c);
        speedStreetRestriction_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        speedStreetRestriction_.setPreferredSize(new Dimension(60,20));
        speedStreetRestriction_.setValue(80);
        c.gridx = 1;
        add(speedStreetRestriction_,c);

        c.gridx = 0;
        c.gridwidth = 2;
        ++c.gridy;
        add(ButtonCreator.getJButton("randomVehicles.png", "createRandom", Messages.getString("EditVehicleControlPanel.createRandom"), this),c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        c.gridx = 0;
        c.gridwidth = 2;
        ++c.gridy;
        add(ButtonCreator.getJButton("deleteAll.png", "clearVehicles", Messages.getString("EditVehicleControlPanel.btnClearVehicles"), this),c);

        TextAreaLabel jlabel1 = new TextAreaLabel(Messages.getString("EditVehicleControlPanel.note")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        c.gridwidth = 2;
        add(jlabel1, c);

        //to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        add(new JPanel(), c);

        //updates the input fields to the first vehicle type
        actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
    }

    /**
     * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
     * is clicked.
     *
     * @param e	an <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
        /** 待新增 */
    }

    /**
     * Gets an integer in the range between <code>min</code> and <code>max</code> (including both!). If you don't put the bigger variable in
     * the <code>min</code>, the variables will be automatically swapped.
     *
     * @param min		the first integer (lower limit)
     * @param max		the second integer (upper limit)
     * @param random	the random number generator
     *
     * @return the random range
     */
    private int getRandomRange(int min, int max, Random random){
        if(min == max) return min;
        else {
            if(max < min){	//swap to make sure that smallest value is in min if wrong values were passed
                int tmp = max;
                max = min;
                min = tmp;
            }
            return (random.nextInt(max - min + 1) + min);
        }
    }

    /**
     * 	updates the vehicle types combobox
     */
    public void refreshVehicleTypes(){
        /** 待新增 */
    }

    /**
     * Mouse listener used to open JColorChooser dialog when colorPreview Panel is clicked
     */
    public void mouseClicked(MouseEvent e) {
        colorPreview_.setBackground(JColorChooser.showDialog(null, Messages.getString("EditOneVehicleControlPanel.color"), colorPreview_.getBackground()));
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}
}