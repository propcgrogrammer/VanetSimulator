package vanetsim.gui.controlpanels;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This class contains the control elements for display of statistics and mix zone information
 */
public class ReportingControlPanel extends JPanel implements ActionListener, ItemListener {


    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */
    /** If this panel is currently active. */
    private boolean active_ = false;





    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */
    /**
     * /////////////  事件觸發方法(start) //////////
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
