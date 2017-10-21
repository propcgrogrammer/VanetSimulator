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
import java.text.ParseException;
import java.util.ArrayDeque;

//import java16.util.ArrayDeque;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.gui.helpers.VehicleType;
import vanetsim.gui.helpers.VehicleTypeXML;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Region;
import vanetsim.routing.WayPoint;
import vanetsim.scenario.Vehicle;

/**
 * This class represents the control panel for adding vehicles by click.
 */

public class EditOneVehicleControlPanel extends JPanel implements ActionListener, MouseListener{

    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /** The necessary constant for serializing. */
    private static final long serialVersionUID = 8669978113870090221L;

    /** RadioButton to add vehicle. */
    JRadioButton addItem_;

    /** RadioButton to edit vehicle. */
    JRadioButton editItem_;

    /** RadioButton to delete vehicle. */
    JRadioButton deleteItem_;

    /** A Label for vehicle type ComboBox. */
    private JLabel chooseVehicleTypeLabel_;

    /** A JComboBox to switch between vehicles types. */
    private JComboBox chooseVehicleType_;

    /** A Label for the JComboBox to switch between vehicles that are near each other. */
    private JLabel chooseVehicleLabel_;

    /** A JComboBox to switch between vehicles that are near each other. */
    private JComboBox chooseVehicle_;

    /** The input field for the speed in km/h. */
    private final JFormattedTextField speed_;

    /** The input field for the communication distance in m. */
    private final JFormattedTextField commDist_;

    /** The input field for the wait in milliseconds. */
    private final JFormattedTextField wait_;

    /** The input field for the braking rate in cm/s^2. */
    private final JFormattedTextField brakingRate_;

    /** The input field for the acceleration rate in cm/s^2. */
    private final JFormattedTextField accelerationRate_;

    /** The input field for the time distance in ms. */
    private final JFormattedTextField timeDistance_;

    /** The input field for the politeness factor in %. */
    private final JFormattedTextField politeness_;

    /** The input field for the vehicleLength in cm. */
    private final JFormattedTextField vehicleLength_;

    /** The checkbox to activate and deactivate wiFi */
    private final JCheckBox wifi_;

    /** The checkbox to activate and deactivate emergency vehicle features */
    private final JCheckBox emergencyVehicle_;

    /** JPanel to preview the selected Vehicle color. */
    private final JPanel colorPreview_;

    /** The spinner to define the amount of waypoints. */
    private final JSpinner waypointAmount_;

    /** The label describing the waypointAmount_ Spinner */
    private final JLabel waypointAmountLabel_;

    /** The spinner to define the amount of vehicles. */
    private final JSpinner vehicleAmount_;

    /** The label describing the vehiceAmount_ Spinner */
    private final JLabel vehicleAmountLabel_;

    /** Collects waypoints, when adding new vehicle. */
    private ArrayDeque<WayPoint> destinations = null;

    /** Create/Save button for vehicles. */
    private JButton createVehicle_;

    /** Delete button to delete a selected vehicle. */
    private JButton deleteVehicle_;

    /** Delete button to delete all vehicles. */
    private JButton deleteAllVehicles_;

    /** Note to describe vehicle action button. */
    TextAreaLabel addNote_;

    /** Note to describe vehicle save button. */
    TextAreaLabel saveNote_;

    /** Note to describe vehicle delete button. */
    TextAreaLabel deleteNote_;

    /** JPanel to consume whitespace */
    JPanel space_;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */
    /**
     * Constructor.
     */
    public EditOneVehicleControlPanel(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "EditOneVehicleControlPanel()", Debug.ISLOGGED);

        setLayout(new GridBagLayout());

        // global layout settings
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = 2;

        // Radio buttons to select add, edit or delete mode
        ButtonGroup group = new ButtonGroup();
        addItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.add")); //$NON-NLS-1$
        addItem_.setActionCommand("add"); //$NON-NLS-1$
        addItem_.addActionListener(this);
        addItem_.setSelected(true);
        group.add(addItem_);
        ++c.gridy;
        add(addItem_,c);

        editItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.edit")); //$NON-NLS-1$
        editItem_.setActionCommand("edit"); //$NON-NLS-1$
        editItem_.addActionListener(this);
        group.add(editItem_);
        ++c.gridy;
        add(editItem_,c);

        deleteItem_ = new JRadioButton(Messages.getString("EditOneVehicleControlPanel.delete")); //$NON-NLS-1$
        deleteItem_.setActionCommand("delete"); //$NON-NLS-1$
        deleteItem_.setSelected(true);
        deleteItem_.addActionListener(this);
        group.add(deleteItem_);
        ++c.gridy;
        add(deleteItem_,c);

        //add comboBox to choose vehicle types and vehicles
        c.gridwidth = 1;
        c.insets = new Insets(5,5,5,5);


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


        c.gridx = 0;
        chooseVehicleLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.selectVehicle")); //$NON-NLS-1$
        ++c.gridy;
        add(chooseVehicleLabel_,c);
        chooseVehicle_ = new JComboBox();
        chooseVehicle_.setName("chooseVehicle");
        chooseVehicle_.addActionListener(this);
        c.gridx = 1;
        add(chooseVehicle_, c);
        chooseVehicle_.setVisible(false);
        chooseVehicleLabel_.setVisible(false);

        //add textfields and checkboxes to change vehicle properties
        c.gridx = 0;
        JLabel label = new JLabel(Messages.getString("EditOneVehicleControlPanel.speed")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        speed_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        speed_.setValue(100);
        getSpeed().setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(getSpeed(),c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.commDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        commDist_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        commDist_.setValue(100);
        getCommDist().setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(getCommDist(),c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.waittime")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        wait_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        wait_.setValue(10);
        getWait().setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(getWait(),c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.brakingRate")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        brakingRate_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        brakingRate_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(brakingRate_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.accelerationRate")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        accelerationRate_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        accelerationRate_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(accelerationRate_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.timeDistance")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        timeDistance_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        timeDistance_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(timeDistance_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.politeness")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        politeness_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        politeness_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(politeness_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.vehicleLength")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        vehicleLength_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        vehicleLength_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(vehicleLength_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.wifi")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        wifi_ = new JCheckBox();
        c.gridx = 1;
        add(wifi_,c);

        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.emergencyVehicle")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        emergencyVehicle_ = new JCheckBox();
        c.gridx = 1;
        add(emergencyVehicle_,c);


        c.gridx = 0;
        label = new JLabel(Messages.getString("EditOneVehicleControlPanel.color")); //$NON-NLS-1$
        ++c.gridy;
        add(label,c);
        colorPreview_ = new JPanel();
        getColorPreview().setBackground(Color.black);
        getColorPreview().setSize(10, 10);
        getColorPreview().addMouseListener(this);
        c.gridx = 1;
        add(getColorPreview(),c);

        //add spinner to choose the amount of waypoints
        c.gridx = 0;
        waypointAmountLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.waypointAmount")); //$NON-NLS-1$
        ++c.gridy;
        add(waypointAmountLabel_,c);
        waypointAmount_ = new JSpinner();
        waypointAmount_.setValue(2);
        c.gridx = 1;
        add(waypointAmount_,c);

        //add spinner to choose amount of vehicles
        c.gridx = 0;
        vehicleAmountLabel_ = new JLabel(Messages.getString("EditOneVehicleControlPanel.vehicleAmount")); //$NON-NLS-1$
        ++c.gridy;
        add(vehicleAmountLabel_,c);
        vehicleAmount_ = new JSpinner();
        vehicleAmount_.setValue(1);
        c.gridx = 1;
        add(vehicleAmount_,c);

        //add buttons
        c.gridx = 0;
        c.gridwidth = 2;
        ++c.gridy;
        createVehicle_ = ButtonCreator.getJButton("oneVehicle.png", "vehicleAction", Messages.getString("EditOneVehicleControlPanel.vehicleAction"), this);
        add(createVehicle_,c);

        c.gridx = 0;
        ++c.gridy;
        deleteVehicle_ = ButtonCreator.getJButton("deleteVehicles.png", "deleteVehicle", Messages.getString("EditOneVehicleControlPanel.deleteVehicle"), this);
        add(deleteVehicle_,c);
        deleteVehicle_.setVisible(false);

        c.gridx = 0;
        c.gridwidth = 2;
        ++c.gridy;
        deleteAllVehicles_ = ButtonCreator.getJButton("deleteAll.png", "clearVehicles", Messages.getString("EditOneVehicleControlPanel.btnClearVehicles"), this);
        add(deleteAllVehicles_,c);

        addNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteAdd")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        add(addNote_, c);

        saveNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteSave")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        add(saveNote_, c);
        saveNote_.setVisible(false);

        deleteNote_ = new TextAreaLabel(Messages.getString("EditOneVehicleControlPanel.noteDelete")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        add(deleteNote_, c);
        deleteNote_.setVisible(false);

        //to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        space_ = new JPanel();
        add(space_, c);


        //updates the input fields to the first vehicle type
        actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
    }

    /**
     * Receives a mouse event.
     *
     * @param x	the x coordinate (in map scale)
     * @param y	the y coordinate (in map scale)
     */
    public void receiveMouseEvent(int x, int y){

        /** 待新增 */
    }

    /**
     * Function to add vehicles. Called after enough waypoints where selected on map.
     */
    private void addVehicle(){
        /** 待新增 */
    }


    /**
     * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>,
     * or a <code>JComboBox</code>, or a <code>JRadioButton</code> is clicked.
     *
     * @param e	an <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        //action when the add RadioButton is selected
        if("add".equals(command)){
            destinations = null;
            Renderer.getInstance().setMarkedVehicle(null);
            setGuiElements("add");
            actionPerformed(new ActionEvent(chooseVehicleType_,0,"comboBoxChanged"));
            Renderer.getInstance().ReRender(false, false);
        }
        //action when the edit RadioButton is selected
        if("edit".equals(command)){
            Renderer.getInstance().setMarkedVehicle(null);
            setGuiElements("save");
            Renderer.getInstance().ReRender(false, false);

            //reset the text of the add vehicle note
            addNote_.setForeground(Color.black);
            addNote_.setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
        }
        //action when the delete RadioButton is selected
        if("delete".equals(command)){
            Renderer.getInstance().setMarkedVehicle(null);
            setGuiElements("delete");
            Renderer.getInstance().ReRender(false, false);

            //reset the text of the add vehicle note
            addNote_.setForeground(Color.black);
            addNote_.setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
        }
        //action when vehicleAction button is pressed and add item radiobutton is selected
        if("vehicleAction".equals(command) && addItem_.isSelected()){ //$NON-NLS-1$	;
            //show add vehicle information and create destinations ArrayDeque (now all mouse events placed on the map will create Waypoints)
            if(((Number)waypointAmount_.getValue()).intValue() > 1){
                addNote_.setForeground(Color.red);
                addNote_.setText(Messages.getString("EditOneVehicleControlPanel.MsgCreateVehicle"));
                destinations = new ArrayDeque<WayPoint>(((Number)waypointAmount_.getValue()).intValue());
            }
            else{
                JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxCreateVehicleWaypointAmountError"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        //action when vehicleAction button is pressed and edit item radiobutton is selected
        else if("vehicleAction".equals(command) && editItem_.isSelected()){ //$NON-NLS-1$
            Renderer.getInstance().setShowVehicles(true);
            //save the vehicle
            Vehicle tmpVehicle = Renderer.getInstance().getMarkedVehicle();

            if(tmpVehicle != null && !getSpeed().getValue().equals("") && !getCommDist().getValue().equals("") && !getWait().getValue().equals("")){
                tmpVehicle.setMaxSpeed((int)Math.round(((Number)getSpeed().getValue()).intValue() * 100000.0/3600));
                tmpVehicle.setVehicleLength(((Number)vehicleLength_.getValue()).intValue());
                tmpVehicle.setMaxCommDistance(((Number)getCommDist().getValue()).intValue()*100);
                tmpVehicle.setCurWaitTime(((Number)getWait().getValue()).intValue());
                tmpVehicle.setAccelerationRate(((Number)getAccelerationRate().getValue()).intValue());
                tmpVehicle.setTimeDistance(((Number)timeDistance_.getValue()).intValue());
                tmpVehicle.setPoliteness(((Number)politeness_.getValue()).intValue());
                tmpVehicle.setBrakingRate(((Number)getBrakingRate().getValue()).intValue());
                tmpVehicle.setWiFiEnabled(wifi_.isSelected());
                tmpVehicle.setColor(colorPreview_.getBackground());
                tmpVehicle.setEmergencyVehicle(emergencyVehicle_.isSelected());

                JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxSavedText"), "Information", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(null, Messages.getString("EditOneVehicleControlPanel.MsgBoxNOTSavedText"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        //action when the deleteVehicle Button is pressed
        if("deleteVehicle".equals(command)){
            /** 待新增 */
        }
        else if ("comboBoxChanged".equals(command)){
            //action when a vehicle in the chooseVehicle Combobox is selected update GUI
            if(((Component) e.getSource()).getName().equals("chooseVehicle")){
                Vehicle tmpVehicle = (Vehicle) chooseVehicle_.getSelectedItem();
                Renderer.getInstance().setMarkedVehicle(tmpVehicle);

                if(tmpVehicle != null){
                    getSpeed().setValue((int)Math.round(tmpVehicle.getMaxSpeed() / (100000.0/3600)));
                    vehicleLength_.setValue(tmpVehicle.getVehicleLength());
                    commDist_.setValue((int)Math.round(tmpVehicle.getMaxCommDistance() / 100));
                    wait_.setValue((int)tmpVehicle.getWaittime());
                    brakingRate_.setValue(tmpVehicle.getBrakingRate());
                    accelerationRate_.setValue(tmpVehicle.getAccelerationRate());
                    timeDistance_.setValue(tmpVehicle.getTimeDistance());
                    politeness_.setValue(tmpVehicle.getPoliteness());
                    wifi_.setSelected(tmpVehicle.isWiFiEnabled());
                    emergencyVehicle_.setSelected(tmpVehicle.isEmergencyVehicle());
                    colorPreview_.setBackground(tmpVehicle.getColor());
                    Renderer.getInstance().ReRender(false, false);
                }
            }
            //action when a vehicle  type in the chooseVehicleType Combobox is selected update GUI
            else if(((Component) e.getSource()).getName().equals("chooseVehicleType")){
                VehicleType tmpVehicleType = (VehicleType) chooseVehicleType_.getSelectedItem();

                /** 待新增 */

            }
        }
        //delete all vehicles
        else if("clearVehicles".equals(command)){
            if(JOptionPane.showConfirmDialog(null, Messages.getString("EditOneVehicleControlPanel.msgBoxClearAll"), "", JOptionPane.YES_NO_OPTION) == 0){
                /** 待新增 */
                Renderer.getInstance().ReRender(true, false);
            }
        }
    }

    /**
     * updates the vehicle type combobox
     */
    public void refreshVehicleTypes(){
        /** 待新增 */
    }

    /**
     * Sets the visibility of GUI elements for adding vehicles
     */
    public void setGuiElements(String command){
        for(Object o:this.getComponents()){
            ((Component) o).setVisible(true);
        }
        if(command.equals("add")){

            if(chooseVehicleType_.getItemCount() < 1){
                chooseVehicleType_.setVisible(true);
                chooseVehicleTypeLabel_.setVisible(true);
            }
            chooseVehicle_.setVisible(true);
            chooseVehicleLabel_.setVisible(true);
            saveNote_.setVisible(true);
            deleteNote_.setVisible(true);
            deleteVehicle_.setVisible(true);

        }
        else if(command.equals("save")){

            chooseVehicle_.setVisible(true);
            chooseVehicleLabel_.setVisible(true);
            chooseVehicleType_.setVisible(true);
            chooseVehicleTypeLabel_.setVisible(true);
            waypointAmount_.setVisible(true);
            waypointAmountLabel_.setVisible(true);
            vehicleAmount_.setVisible(true);
            vehicleAmountLabel_.setVisible(true);
            addNote_.setVisible(true);
            deleteNote_.setVisible(true);

        }
        else if(command.equals("delete")){
            for(Object o:this.getComponents()){
                ((Component) o).setVisible(false);
            }
            addItem_.setVisible(true);
            editItem_.setVisible(true);
            deleteItem_.setVisible(true);
            deleteNote_.setVisible(true);
            deleteAllVehicles_.setVisible(true);
            space_.setVisible(true);
        }

    }

    /**
     * Gets the vehicle wait time TextField
     *
     * @return the wait_ TextField
     */
    public JFormattedTextField getWait() {
        return wait_;
    }

    /**
     * Gets the vehicle speed TextField
     *
     * @return the speed_ TextField
     */
    public JFormattedTextField getSpeed() {
        return speed_;
    }

    /**
     * Gets the vehicle communications distance TextField
     *
     * @return the commDist_ TextField
     */
    public JFormattedTextField getCommDist() {
        return commDist_;
    }

    /**
     * Gets the vehicle color Panel
     *
     * @return the colorPreview_ Panel
     */
    public JPanel getColorPreview() {
        return colorPreview_;
    }

    /**
     * Gets the vehicle braking rate TextField
     *
     * @return the brakingRate_ TextField
     */
    public JFormattedTextField getBrakingRate() {
        return brakingRate_;
    }

    /**
     * Gets the vehicle acceleration rate TextField
     *
     * @return the accelerationRate_ TextField
     */
    public JFormattedTextField getAccelerationRate() {
        return accelerationRate_;
    }

    /**
     * Gets the TextAreaLabel in the add vehicle menu
     *
     * @return addNote_		the add note
     */
    public TextAreaLabel getAddNote() {
        return addNote_;
    }

    //Mouse Listener to open the JColorChooser to choose the vehicle color, when the colorPreview Panel is clicked
    public void mouseClicked(MouseEvent e) {
        getColorPreview().setBackground(JColorChooser.showDialog(null, Messages.getString("EditOneVehicleControlPanel.color"), getColorPreview().getBackground()));
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

}