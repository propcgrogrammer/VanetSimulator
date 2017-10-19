package vanetsim.gui.controlpanels;

import vanetsim.debug.Debug;
import vanetsim.localization.Messages;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
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

    /** The reporting control panel. */
    private final ReportingControlPanel reportingPanel_ = new ReportingControlPanel();

    /** The pane for the tabs. */
    private final JTabbedPane tabbedPane_ = new JTabbedPane();


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


    }


    /**
     * /////////////  檔案選擇方法 (start) /////////////////
     */

    /**
     * Gets the central <code>JFileChooser</code>. If it does not exist, it waits until it's created
     * in the separate thread started from the constructor.
     * 獨立執行緒，於建構子時呼叫
     * @return the file chooser
     */
    public JFileChooser getFileChooser(){
        if(fileChooser_ == null){	// wait until it's ready
            do{
                try{
                    Thread.sleep(1);
                } catch (Exception e){};
            } while (fileChooser_ == null);
        }
        return fileChooser_;
    }

    /**
     * Changes the available file choosers of the central <code>JFileChooser</code>.
     *
     * @param acceptAll	adds a file chooser to select all files
     * @param acceptXML	adds a file chooser to select XML files
     * @param acceptOSM	adds a file chooser to select OpenStreetMap-files (*.osm)
     */
    public void changeFileChooser(boolean acceptAll, boolean acceptXML, boolean acceptOSM){
        if(fileChooser_ == null){	// wait until it's ready
            do{
                try{
                    Thread.sleep(1);
                } catch (Exception e){};
            } while (fileChooser_ == null);
        }
        fileChooser_.resetChoosableFileFilters();
        if(acceptAll) fileChooser_.setAcceptAllFileFilterUsed(true);
        else fileChooser_.setAcceptAllFileFilterUsed(true);
        if(acceptOSM){
            fileChooser_.addChoosableFileFilter(osmFileFilter_);
            fileChooser_.setFileFilter(osmFileFilter_);
        }
        if(acceptXML){
            fileChooser_.addChoosableFileFilter(xmlFileFilter_);
            fileChooser_.setFileFilter(xmlFileFilter_);
        }
    }

    /**
     * /////////////  檔案選擇方法 (end) /////////////////
     */

    /**
     * ///////////// setter & getter (start) //////////////
     */

    /**
     * Gets the control panel on the simulation tab.
     *
     * @return the control panel
     */
    public SimulateControlPanel getSimulatePanel(){
        return simulatePanel_;
    }

    /**
     * Gets the control panel on the edit tab.
     * 由 VanetSimStart 呼叫
     * @return the control panel
     */
    public EditControlPanel getEditPanel(){
        return editPanel_;
    }

    /**
     * Gets the control panel on the reporting tab.
     *
     * @return the control panel
     */
    public ReportingControlPanel getReportingPanel(){
        return reportingPanel_;
    }


    /**
     * ///////////// setter & getter (end) //////////////
     */

    /**
     * //////////// 頁籤選擇方法 （start) /////////////
     */
    /**
     * Gets the currently selected tab component.
     *
     * @return	the currently selected tab component
     */
    public Component getSelectedTabComponent(){
        return tabbedPane_.getSelectedComponent();
    }

    /**
     * An implemented <code>ChangeListener</code> for the tabs.
     *
     * @param e a <code>ChangeEvent</code>
     */
    public void stateChanged(ChangeEvent e){
        if(tabbedPane_.getSelectedComponent() instanceof ReportingControlPanel){
            reportingPanel_.setActive(true);
        } else {
            reportingPanel_.setActive(false);
        }
    }
    /**
     * //////////// 頁籤選擇方法 （end) /////////////
     */

}
