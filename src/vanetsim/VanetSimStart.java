package vanetsim;


import vanetsim.debug.Debug;
import vanetsim.gui.controlpanels.MainControlPanel;
import vanetsim.localization.Messages;

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

    /** The <code>JFrame</code> which is the base of the application. */
    private static JFrame mainFrame_;

    /** The controlpanel on the right side. */
    private static MainControlPanel controlPanel_;


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

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
//
//        DrawingArea drawarea = addComponentsToPane(mainFrame_.getContentPane());
//        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        ///////////////////////////////////////

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



    }
}
