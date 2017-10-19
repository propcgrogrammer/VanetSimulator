package vanetsim.gui.controlpanels;

import vanetsim.debug.Debug;
import vanetsim.localization.Messages;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

/**
 * This class creates the control elements on the right side of the DrawingArea. The control panels itself are
 * packed into a <code>JTabbedPane</code> and can be found in separate classes.
 */
public class MainControlPanel extends JPanel implements ChangeListener {

    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */

    /**
     * 檔案類變數
     */
    /** A central <code>JFileChooser</code> so that the directory stays saved. */
    private JFileChooser fileChooser_ = null;

    /** The file filter for XML files. Used in the central file chooser. */
    private final FileFilter xmlFileFilter_;

    /** The file filter for OpenStreetMap files. Used in the central file chooser. */
    private final FileFilter osmFileFilter_;


    /**
     * Panel類變數
     */

    /** The simulate control panel. */
    private final SimulateControlPanel simulatePanel_ = new SimulateControlPanel();

    /** The edit control panel. */
    private final EditControlPanel editPanel_ = new EditControlPanel();


    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    /**
     * Constructor for the main control panel.
     */
    public MainControlPanel(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "MainControlPanel()", Debug.ISLOGGED);

        /** Create the file chooser in a separate thread. This may take some seconds on Windows... */
        Debug.detailedInfo("Create the file chooser in a separate thread. This may take some seconds on Windows...", Debug.ISLOGGED);

        Runnable job = new Runnable(){
            public void run() {
                JFileChooser tmpChooser = new JFileChooser();
                tmpChooser.setMultiSelectionEnabled(false);
                fileChooser_ = tmpChooser;	//now it's ready and we can set the global filechooser
            }
        };
        new Thread(job).start();

        /**
         * 檢查xml檔案物件
         */
        xmlFileFilter_ = new FileFilter(){
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
            }
            public String getDescription () {
                return Messages.getString("MainControlPanel.xmlFiles") + " (*.xml)"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };

        /**
         * 檢查osm檔案物件
         */
        osmFileFilter_ = new FileFilter(){
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                return f.getName().toLowerCase().endsWith(".osm"); //$NON-NLS-1$
            }
            public String getDescription () {
                return Messages.getString("MainControlPanel.openStreetMapFiles") + " (*.osm)"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        };


        /** set some layout settings and a fixed size */
        setLayout(new GridBagLayout());
        Dimension size = simulatePanel_.getPreferredSize();
        /** 調整視窗大小 */
        size.setSize(size.width + 210, size.height < 800? 800: size.height);

        setMinimumSize(new Dimension(size.width,400));
        editPanel_.setMinimumSize(size);



        /**
         * Gets the control panel on the edit tab.
         *
         * @return the control panel
         */
        public EditControlPanel getEditPanel(){
            return editPanel_;
        }




    }



}
