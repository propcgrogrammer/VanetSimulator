package vanetsim.gui.controlpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.CyclicBarrier;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import vanetsim.ErrorLog;
import vanetsim.VanetSimStart;
import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.map.Node;
import vanetsim.map.Region;
import vanetsim.map.OSM.OSMLoader;
import vanetsim.scenario.Scenario;
import vanetsim.scenario.Vehicle;

/**
 * This class creates all control elements used in the edit tab.
 */
public final class EditControlPanel extends JPanel implements ActionListener {


    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */

    /** The necessary constant for serializing. */
    private static final long serialVersionUID = -7019659218394560856L;

    /** The button to enable edit mode. */
    private final JRadioButton enableEdit_;

    /** The button to disable edit mode. */
    private final JRadioButton disableEdit_;

    /** A <code>JComboBox</code> to switch between the different editing tasks. */
    private final JComboBox editChoice_;

    /** The panel containing all controls (only visible if in edit mode). */
    private final JPanel editPanel_;

    /** A <code>JPanel</code> with <code>CardLayout</code> to switch between editing streets, vehicles and events. */
    private final JPanel editCardPanel_;

    /** The tabbed Panel for the edit vehicle tabs */
    private final JTabbedPane tabbedPane_;

    /** The tabbed Panel for the privacy tabs */
    private final JTabbedPane privacyTabbedPane_;

    /** The control panel to edit streets. */
    private final EditStreetControlPanel editStreetPanel_ = new EditStreetControlPanel();

    /** The control panel to edit vehicles. */
    private final EditVehicleControlPanel editVehiclePanel_ = new EditVehicleControlPanel();

    /** The control panel to create,edit or delete one vehicle. */
    private final EditOneVehicleControlPanel editOneVehiclePanel_ = new EditOneVehicleControlPanel();

    /** The control panel to edit mix zones. */
    private final MixZonePanel editMixZonePanel_ = new MixZonePanel();

    /** The control panel to edit silent periods. */
    private final SilentPeriodPanel editSilentPeriodPanel_ = new SilentPeriodPanel();

    /** The control panel to edit RSUs. */
    private final RSUPanel editRSUPanel_ = new RSUPanel();

    /** The control panel to edit Attacker Settings. */
    private final AttackerPanel editAttackerPanel_ = new AttackerPanel();

    /** The control panel to edit events. */
    private final EditEventControlPanel editEventPanel_ = new EditEventControlPanel();

    /** The control panel to edit events. */
    private final EditSettingsControlPanel editSettingsPanel_ = new EditSettingsControlPanel();

    /** The control panel to create,edit or delete traffic lights. */
    private final EditTrafficLightsControlPanel editTrafficLightsPanel_ = new EditTrafficLightsControlPanel();

    /** The control panel to edit log configuration. */
    private final EditLogControlPanel editLogControlPanel_ = new EditLogControlPanel();

    /** If edit mode is currently enabled or not. */
    private boolean editMode_ = true;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    /**
     * Constructor for this ControlPanel.
     */
    public EditControlPanel(){

        /** 進行版面配置（使用GridBagConstraints） */
        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "EditControlPanel()", Debug.ISLOGGED);

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

        /** 載入按鈕圖檔 */
        Debug.detailedInfo("載入按鈕圖檔中.....",Debug.ISLOGGED);
        //the save and load buttons
        add(ButtonCreator.getJButton("newmap.png", "newmap", Messages.getString("EditControlPanel.newMap"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 1;
        add(ButtonCreator.getJButton("newscenario.png", "newscenario", Messages.getString("EditControlPanel.newScenario"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 0;
        ++c.gridy;
        add(ButtonCreator.getJButton("savemap.png", "savemap", Messages.getString("EditControlPanel.saveMap"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 1;
        add(ButtonCreator.getJButton("savescenario.png", "savescenario", Messages.getString("EditControlPanel.saveScenario"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 0;
        ++c.gridy;
        add(ButtonCreator.getJButton("importOSM.png", "importOSM", Messages.getString("EditControlPanel.importOSM"), this) , c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 1;
        add(ButtonCreator.getJButton("openTypeDialog.png", "openTypeDialog", Messages.getString("EditControlPanel.openTypeDialog"), this), c); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridx = 0;
        ++c.gridy;
        Debug.detailedInfo("圖檔載入完成",Debug.ISLOGGED);

        // Radio buttons to enable/disable editing
        JLabel jLabel1 = new JLabel("<html><u><b>" + Messages.getString("EditControlPanel.editMode") +"</u></b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.gridwidth = 2;
        ++c.gridy;
        add(jLabel1,c);
        c.gridwidth = 1;
        ButtonGroup group = new ButtonGroup();
        enableEdit_ = new JRadioButton(Messages.getString("EditControlPanel.enable")); //$NON-NLS-1$
        enableEdit_.setActionCommand("enableEdit"); //$NON-NLS-1$
        enableEdit_.setSelected(true);
        enableEdit_.addActionListener(this);
        group.add(enableEdit_);
        ++c.gridy;
        add(enableEdit_,c);
        disableEdit_ = new JRadioButton(Messages.getString("EditControlPanel.disable")); //$NON-NLS-1$
        disableEdit_.setActionCommand("disableEdit"); //$NON-NLS-1$
        disableEdit_.addActionListener(this);
        group.add(disableEdit_);
        c.gridx = 1;
        add(disableEdit_,c);
        c.gridx = 0;

        // A Panel containing all edit controls. The controls for editing streets, vehicles and events are outsourced into separate classes
        editPanel_ = new JPanel();
        editPanel_.setLayout(new BorderLayout(0,5));
        String[] choices = { Messages.getString("EditControlPanel.settings"), Messages.getString("EditControlPanel.street"), Messages.getString("EditControlPanel.trafficLights"), Messages.getString("EditControlPanel.vehicles"),  Messages.getString("EditControlPanel.privacy"), Messages.getString("EditControlPanel.rsus"), Messages.getString("EditControlPanel.attackers"), Messages.getString("EditControlPanel.event"), Messages.getString("EditControlPanel.logs")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        editChoice_ = new JComboBox(choices);
        editChoice_.setSelectedIndex(0);
        editChoice_.setMaximumRowCount(100);
        editChoice_.addActionListener(this);
        editPanel_.add(editChoice_, BorderLayout.PAGE_START);
        editCardPanel_ = new JPanel(new CardLayout());

        /** 暫時關閉此功能 */
    //    editCardPanel_.add(editSettingsPanel_, "settings"); //$NON-NLS-1$
    //    editCardPanel_.add(editStreetPanel_, "street"); //$NON-NLS-1$
        editCardPanel_.add(editTrafficLightsPanel_, "trafficLights"); //$NON-NLS-1$
        editCardPanel_.add(editEventPanel_, "event"); //$NON-NLS-1$

        // A tabbed panel for privacy functions
        privacyTabbedPane_ = new JTabbedPane();
        privacyTabbedPane_.add(Messages.getString("EditControlPanel.mixZones"), editMixZonePanel_);
        privacyTabbedPane_.add(Messages.getString("EditControlPanel.silentPeriods"), editSilentPeriodPanel_);
        editCardPanel_.add(privacyTabbedPane_, "privacy"); //$NON-NLS-1$

        editCardPanel_.add(editRSUPanel_, "rsus"); //$NON-NLS-1$
        // A tabbed panel to expand the vehicle functions
        tabbedPane_ = new JTabbedPane();
        tabbedPane_.add(Messages.getString("EditVehiclesControlPanel.vehicle1"), editVehiclePanel_);
        tabbedPane_.add(Messages.getString("EditVehiclesControlPanel.vehicle2"), editOneVehiclePanel_);
        /** 暫時關閉此功能 */
    //    editCardPanel_.add(editAttackerPanel_, "attackers"); //$NON-NLS-1$

        editCardPanel_.add(tabbedPane_, "vehicles"); //$NON-NLS-1$
        editCardPanel_.add(editLogControlPanel_, "logs"); //$NON-NLS-1$
        editPanel_.add(editCardPanel_, BorderLayout.PAGE_END);


        c.gridwidth = 2;
        ++c.gridy;
        add(editPanel_,c);

        //to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        add(new JPanel(), c);

    }

    /**
     * Receives a mouse event and forwards it to the correct control panel.
     *
     * @param x	the x coordinate
     * @param y	the y coordinate
     */
    public void receiveMouseEvent(int x, int y){
        String item = (String)editChoice_.getSelectedItem();
        if(item.equals(Messages.getString("EditControlPanel.street"))){ //$NON-NLS-1$
            editStreetPanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle2"))){ //$NON-NLS-1$
            editOneVehiclePanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.event"))){ //$NON-NLS-1$
            editEventPanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.privacy"))){ //$NON-NLS-1$
            editMixZonePanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.rsus"))){ //$NON-NLS-1$
            editRSUPanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.attackers"))){ //$NON-NLS-1$
            editAttackerPanel_.receiveMouseEvent(x, y);
        } else if(item.equals(Messages.getString("EditControlPanel.trafficLights"))){ //$NON-NLS-1$
            editTrafficLightsPanel_.receiveMouseEvent(x, y);
        }
    }

    /**
     * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JButton</code>
     * is clicked.
     *
     * @param e	an <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e){
        /** 待新增 */
        /** 2017/10/24_1636 新增
         *  2017/11/15_2131 全部新增完成
         * */
        String command = e.getActionCommand();
        if ("savemap".equals(command)){ //$NON-NLS-1$
            VanetSimStart.getMainControlPanel().changeFileChooser(false, true, false);
            final JFileChooser filechooser = VanetSimStart.getMainControlPanel().getFileChooser();
            int returnVal = filechooser.showSaveDialog(VanetSimStart.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Runnable job = new Runnable() {
                    public void run() {
                        File file = filechooser.getSelectedFile();
                        if(filechooser.getAcceptAllFileFilter() != filechooser.getFileFilter() && !file.getName().toLowerCase().endsWith(".xml")) file = new File(file.getAbsolutePath() + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
                        Map.getInstance().save(file, false);
                    }
                };
                new Thread(job).start();
            }
        } else if ("savescenario".equals(command)){ //$NON-NLS-1$
            VanetSimStart.getMainControlPanel().changeFileChooser(false, true, false);
            final JFileChooser filechooser = VanetSimStart.getMainControlPanel().getFileChooser();
            int returnVal = filechooser.showSaveDialog(VanetSimStart.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Runnable job = new Runnable() {
                    public void run() {
                        File file = filechooser.getSelectedFile();
                        if(filechooser.getAcceptAllFileFilter() != filechooser.getFileFilter() && !file.getName().toLowerCase().endsWith(".xml")) file = new File(file.getAbsolutePath() + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
                        Scenario.getInstance().save(file, false);
                    }
                };
                new Thread(job).start();
            }
        } else if ("importOSM".equals(command)) { //$NON-NLS-1$
            VanetSimStart.getMainControlPanel().changeFileChooser(true, true, true);
            int returnVal = VanetSimStart.getMainControlPanel().getFileChooser().showOpenDialog(VanetSimStart.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION){
                Runnable job = new Runnable() {
                    public void run() {
                        OSMLoader.getInstance().loadOSM(VanetSimStart.getMainControlPanel().getFileChooser().getSelectedFile());
                    }
                };
                new Thread(job).start();
            }
        }else if ("newmap".equals(command)){ //$NON-NLS-1$
            CyclicBarrier barrier = new CyclicBarrier(2);
            new MapSizeDialog(100000, 100000, 50000, 50000, barrier);
            try {
                barrier.await();
            } catch (Exception e2) {}
            enableEdit_.setSelected(false);
            disableEdit_.setSelected(true);
            Map.getInstance().signalMapLoaded();
        } else if ("newscenario".equals(command)){ //$NON-NLS-1$
            Scenario.getInstance().initNewScenario();
            Scenario.getInstance().setReadyState(true);
            VanetSimStart.getMainControlPanel().getEditPanel().getEditEventPanel().updateList();
            Renderer.getInstance().ReRender(false, false);
        } else if ("openTypeDialog".equals(command)){ //$NON-NLS-1$
           // new VehicleTypeDialog();
        } else if ("enableEdit".equals(command)){ //$NON-NLS-1$
            if(Renderer.getInstance().getTimePassed() > 0){
                enableEdit_.setSelected(false);
                disableEdit_.setSelected(true);
                Renderer.getInstance().setAutoAddMixZones(editMixZonePanel_.getAutoAddMixZones().isSelected());
                ErrorLog.log(Messages.getString("EditControlPanel.editingOnlyOnCleanMap"), 6, this.getName(), "enableEdit", null); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                editMode_ = true;
                if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.street"))){ //$NON-NLS-1$
                    Renderer.getInstance().setHighlightNodes(true);
                    Renderer.getInstance().ReRender(true, false);
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.event"))){ //$NON-NLS-1$
                    Renderer.getInstance().setShowAllBlockings(true);
                    Renderer.getInstance().ReRender(true, false);
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.privacy"))){ //$NON-NLS-1$
                    Renderer.getInstance().setShowMixZones(true);
                    Renderer.getInstance().setHighlightNodes(true);
                    Renderer.getInstance().ReRender(true, false);
                    editSilentPeriodPanel_.loadAttributes();
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.rsus"))){ //$NON-NLS-1$
                    Renderer.getInstance().setShowRSUs(true);
                    Renderer.getInstance().setHighlightCommunication(true);
                    Renderer.getInstance().ReRender(true, false);
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.attackers"))){ //$NON-NLS-1$
                    Renderer.getInstance().setShowVehicles(true);
                    Renderer.getInstance().setShowAttackers(true);
                    Renderer.getInstance().setShowMixZones(true);
                    Renderer.getInstance().setHighlightCommunication(true);
                    Renderer.getInstance().ReRender(true, false);
                    editLogControlPanel_.refreshGUI();
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.vehicles"))){ //$NON-NLS-1$
                    Renderer.getInstance().setShowVehicles(true);
                    Renderer.getInstance().ReRender(true, false);
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.trafficLights"))){ //$NON-NLS-1$
                    Renderer.getInstance().setHighlightNodes(true);
                    Renderer.getInstance().ReRender(false, false);
                } else if(((String)editChoice_.getSelectedItem()).equals(Messages.getString("EditControlPanel.logs"))){ //$NON-NLS-1$

                    Renderer.getInstance().ReRender(false, false);
                }
                editPanel_.setVisible(true);
            }
        } else if ("disableEdit".equals(command)){ //$NON-NLS-1$
            editMode_ = false;
            editPanel_.setVisible(false);
            Renderer.getInstance().setHighlightNodes(false);
            Renderer.getInstance().setShowAllBlockings(false);
            //	Renderer.getInstance().setShowVehicles(false);
            Renderer.getInstance().setShowVehicles(true);
            Renderer.getInstance().setShowMixZones(false);
            Renderer.getInstance().setShowRSUs(false);
            Renderer.getInstance().setShowAttackers(false);
            Renderer.getInstance().setHighlightCommunication(false);
            //Renderer.getInstance().setShowAttackers(false);
            Vehicle markedVehicle = Renderer.getInstance().getMarkedVehicle();
            Renderer.getInstance().setMarkedVehicle(markedVehicle);
            Renderer.getInstance().setMarkedJunction_(null);
            Renderer.getInstance().ReRender(true, false);
            setMaxMixZoneRadius();
            editSilentPeriodPanel_.saveAttributes();
        } else if ("comboBoxChanged".equals(command)){ //$NON-NLS-1$
            String item = (String)editChoice_.getSelectedItem();
            CardLayout cl = (CardLayout)(editCardPanel_.getLayout());
            Renderer.getInstance().setHighlightNodes(false);
            Renderer.getInstance().setShowAllBlockings(false);
            Renderer.getInstance().setShowVehicles(false);
            Renderer.getInstance().setShowMixZones(false);
            Renderer.getInstance().setShowRSUs(false);
            Renderer.getInstance().setHighlightCommunication(false);
            Renderer.getInstance().setShowAttackers(false);
            Renderer.getInstance().setMarkedJunction_(null);
            //Renderer.getInstance().setShowAttackers(false);
            Renderer.getInstance().setMarkedVehicle(null);
            Renderer.getInstance().ReRender(true, false);
            if(Messages.getString("EditControlPanel.street").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "street"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(true);
                Renderer.getInstance().setShowAllBlockings(false);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.vehicles").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "vehicles"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(false);
                Renderer.getInstance().setShowAllBlockings(false);
                Renderer.getInstance().setShowVehicles(true);
                Renderer.getInstance().ReRender(true, false);

                //reset the text of the add vehicle note
                editOneVehiclePanel_.getAddNote().setForeground(Color.black);
                editOneVehiclePanel_.getAddNote().setText(Messages.getString("EditOneVehicleControlPanel.noteAdd"));
                stateChanged(null);
            } else if(Messages.getString("EditControlPanel.privacy").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "privacy"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(true);
                Renderer.getInstance().setShowAllBlockings(false);
                Renderer.getInstance().setShowMixZones(true);
                Renderer.getInstance().ReRender(true, false);
                editSilentPeriodPanel_.loadAttributes();
            } else if(Messages.getString("EditControlPanel.rsus").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "rsus"); //$NON-NLS-1$
                Renderer.getInstance().setShowRSUs(true);
                Renderer.getInstance().setHighlightCommunication(true);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.attackers").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "attackers"); //$NON-NLS-1$
                Renderer.getInstance().setShowMixZones(true);
                Renderer.getInstance().setShowVehicles(true);
                Renderer.getInstance().setShowAttackers(true);
                Renderer.getInstance().setHighlightCommunication(true);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.event").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "event"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(false);
                Renderer.getInstance().setShowAllBlockings(true);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.settings").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "settings"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(false);
                Renderer.getInstance().setShowAllBlockings(false);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.trafficLights").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "trafficLights"); //$NON-NLS-1$
                Renderer.getInstance().setHighlightNodes(true);
                Renderer.getInstance().ReRender(true, false);
            } else if(Messages.getString("EditControlPanel.logs").equals(item)){	//$NON-NLS-1$
                cl.show(editCardPanel_, "logs"); //$NON-NLS-1$
                editLogControlPanel_.refreshGUI();
                Renderer.getInstance().ReRender(true, false);
            }
        }

    }

    /**
     * Controls the tabbed pane (tabbedPane_) to switch between edit vehicle modes
     */
    public void stateChanged(ChangeEvent arg0) {
        Renderer.getInstance().setMarkedVehicle(null);
        if(editChoice_.getSelectedItem().toString().equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle1"))){
        }
        else if(editChoice_.getSelectedItem().toString().equals(Messages.getString("EditControlPanel.vehicles")) && tabbedPane_.getTitleAt(tabbedPane_.getSelectedIndex()).equals(Messages.getString("EditVehiclesControlPanel.vehicle2"))){
            Renderer.getInstance().setMarkedVehicle(null);
            Renderer.getInstance().setShowVehicles(true);
        }

        Renderer.getInstance().ReRender(false, false);
    }

    /**
     * 地圖載入前由 VanetSimStart 呼叫（state = false)
     * @param state
     */
    public void setEditMode(boolean state){

        Debug.callFunctionInfo(this.getClass().getName(),"setEditMode(boolean state)",Debug.ISLOGGED);


        if(state) enableEdit_.setSelected(true);
        else disableEdit_.setSelected(true);
        editPanel_.setVisible(state);
        editMode_ = state;
    }

    /**
     * Gets the maximal Mix-Zone radius used in the actual scenario and sets the variable in Vehicle.java
     */
    public void setMaxMixZoneRadius(){
        Region[][] tmpRegions = Map.getInstance().getRegions();

        int regionCountX = Map.getInstance().getRegionCountX();
        int regionCountY = Map.getInstance().getRegionCountY();
        int maxMixRadius = 0;

        for(int i = 0; i <= regionCountX-1;i++){
            for(int j = 0; j <= regionCountY-1;j++){
                Region tmpRegion = tmpRegions[i][j];

                Node[] mixZones = tmpRegion.getMixZoneNodes();

                for(int k = 0;k < mixZones.length; k++){
                    if(maxMixRadius < mixZones[k].getMixZoneRadius()) maxMixRadius = mixZones[k].getMixZoneRadius();
                }

            }
        }

        Vehicle.setMaxMixZoneRadius(maxMixRadius);
    }

    /**
     * Gets the current edit mode.
     *
     * @return <code>true</code> if editing is currently enabled, <code>false</code> if it's disabled
     */
    public boolean getEditMode(){
        return editMode_;
    }

    /**
     * Gets the control panel to edit streets.
     *
     * @return the control panel
     */
    public EditStreetControlPanel getEditStreetPanel(){
        return editStreetPanel_;
    }

    /**
     * Gets the control panel to edit vehicles.
     *
     * @return the control panel
     */
    public EditVehicleControlPanel getEditVehiclePanel(){
        return editVehiclePanel_;
    }

    /**
     * Gets the control panel to edit events.
     *
     * @return the control panel
     */
    public EditEventControlPanel getEditEventPanel(){

        Debug.callFunctionInfo(this.getClass().getName(),"getEditEventPanel()",Debug.ISLOGGED);

        return editEventPanel_;
    }

    /**
     * Gets the control panel to edit settings.
     *
     * @return the control panel
     */
    public EditSettingsControlPanel getEditSettingsPanel(){
        return editSettingsPanel_;
    }

    /**
     * Gets the tabbed panel for vehicle editing
     *
     * @return the tabbed panel
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane_;
    }

    /**
     * Gets the editOneVehiclePanel to edit one vehicle.
     *
     * @return the editOneVehiclePanel
     */
    public EditOneVehicleControlPanel getEditOneVehiclePanel() {
        return editOneVehiclePanel_;
    }

    public MixZonePanel getEditMixZonePanel_() {
        return editMixZonePanel_;
    }

    public AttackerPanel getEditAttackerPanel_() {
        return editAttackerPanel_;
    }

    public EditLogControlPanel getEditLogControlPanel_() {
        return editLogControlPanel_;
    }
}