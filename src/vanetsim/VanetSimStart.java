package vanetsim;


import vanetsim.debug.Debug;
import vanetsim.gui.DrawingArea;
import vanetsim.gui.Renderer;
import vanetsim.gui.controlpanels.MainControlPanel;
import vanetsim.localization.Messages;
import vanetsim.simulation.SimulationMaster;

import javax.swing.*;
import java.awt.*;

/**
 * This is the main class for the VANet-Simulator which starts the GUI and all other components.
 */
public class VanetSimStart implements Runnable{


    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */
    /**
     *  java swing gui類變數
     */
    /** The <code>JFrame</code> which is the base of the application. 程式主畫面 */

    private static JFrame mainFrame_;

    /** The controlpanel on the right side. 右側控制面板 */
    private static MainControlPanel controlPanel_;


    /**
     *  java thread 類變數
     */
    /** The master thread for simulation delegation. Stored here if any other class needs control over it. */
    private static SimulationMaster simulationMaster_;


    /**
     * buffering 類變數
     */
    /** <code>true</code> if double buffering shall be used on the drawing area. */
    private static boolean useDoubleBuffering_;

    /** <code>true</code> if a manual buffering shall be used on the drawing area. */
    private static boolean drawManualBuffered_;

    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    public VanetSimStart(){

        Debug.whereru("VanetSimStart", Debug.ISLOGGED);
        Debug.callFunctionInfo("VanetSimStart", "VanetSimStart()", Debug.ISLOGGED);

        //	new Statistics("行車速率表").createResultFrame("time vs velocity");

        //	readconfig("./config.txt"); //$NON-NLS-1$

    }


    /**
     * Thread which creates the GUI.
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        Debug.callFunctionInfo("VanetSimStart", "run()", Debug.ISLOGGED);

        Debug.detailedInfo("Thread which creates the GUI.", Debug.ISLOGGED);

        Debug.detailedInfo("creating MainFrame ...", Debug.ISLOGGED);
        mainFrame_ = new JFrame();
        mainFrame_.setTitle(Messages.getString("StartGUI.applicationtitle")); //$NON-NLS-1$
        mainFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Debug.detailedInfo("create MainFrame finish", Debug.ISLOGGED);

        /**
         * UI 控制部分
         */
        ///////////////////////////////////////
//        progressBar_ = new ProgressOverlay();
//        if(Runtime.getRuntime().maxMemory() < 120000000) ErrorLog.log(Messages.getString("StartGUI.detectedLowMemory"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$
//        URL appicon = ClassLoader.getSystemResource("vanetsim/images/appicon.gif"); //$NON-NLS-1$
//        if (appicon != null){
//            mainFrame_.setIconImage(Toolkit.getDefaultToolkit().getImage(appicon));
//        } else ErrorLog.log(Messages.getString("StartGUI.noAppIcon"), 6, VanetSimStart.class.getName(), "run", null); //$NON-NLS-1$ //$NON-NLS-2$
        ///////////////////////////////////////

        /** 設置MainFrame相關的版面配置 */
        DrawingArea drawarea = addComponentsToPane(mainFrame_.getContentPane());

        /** 取得系統螢幕大小 */
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        mainFrame_.pack();
        mainFrame_.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
        mainFrame_.setLocationRelativeTo(null); // center on screen
        mainFrame_.setResizable(true);
        mainFrame_.setVisible(true);

        /** 在正式載入地圖前，初始化各種設定面板  */
        controlPanel_.getEditPanel().setEditMode(false);
        Debug.detailedInfo("initiailze JFrame, JAVA Swing finish", Debug.ISLOGGED);

        /** 正式進行模擬執行緒 */
        simulationMaster_ = new SimulationMaster();
        simulationMaster_.start();


    }

    /**
     * Function to add the control elements to a container.
     *
     * @param container	the container on which to add the elements
     * 此處的 container 為 MainFrame的
     *
     * @return the constructed <code>DrawingArea</code>
     */
    public static DrawingArea addComponentsToPane(Container container) {

        Debug.callFunctionInfo("VanetSimStart", "addComponentsToPane(Container container)", Debug.ISLOGGED);

        container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        DrawingArea drawarea = new DrawingArea(useDoubleBuffering_, drawManualBuffered_);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        container.add(drawarea, c);
        Renderer.getInstance().setDrawArea(drawarea);

        /** 初始化 MainControlPanel 控制面板*/
        controlPanel_ = new MainControlPanel();
        controlPanel_.setPreferredSize(new Dimension(200, 100000));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.gridx = 1;
        c.gridy = 0;
        container.add(controlPanel_, c);

        return drawarea;
    }

    /**
     * Gets the control panel on the right side.
     *
     * @return the control panel
     */
    public static MainControlPanel getMainControlPanel(){
        return controlPanel_;
    }

    /**
     * Gets the initial <code>JFrame</code> of the application.
     *
     * @return the <code>JFrame</code>
     */
    public static JFrame getMainFrame(){
        return mainFrame_;
    }

    /**
     * Returns the simulation master (for example in order to stop or start simulation).
     *
     * @return the simulation master
     */
    public static SimulationMaster getSimulationMaster(){
        return simulationMaster_;
    }
}
