package vanetsim.gui.controlpanels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import javax.swing.JPanel;

import vanetsim.debug.Debug;
import vanetsim.gui.Renderer;
import vanetsim.gui.helpers.ButtonCreator;
import vanetsim.gui.helpers.TextAreaLabel;
import vanetsim.localization.Messages;
import vanetsim.map.Map;
import vanetsim.scenario.RSU;

/**
 * This class represents the control panel for adding Road-Side-Units (RSUs).
 */
public class RSUPanel extends JPanel implements ActionListener{

    /**
     * ///////////////////////////////////
     * //  instance variable
     * ///////////////////////////////////
     */
    /** The necessary constant for serializing. */
    private static final long serialVersionUID = 6951925324502007245L;

    /** RadioButton to add RSUs. */
    JRadioButton addRSU_;

    /** RadioButton to delete RSUs. */
    JRadioButton deleteRSU_;

    /** The input field for the RSU radius */
    private final JFormattedTextField rsuRadius_;

    /** The label of the RSU radius textfield */
    private final JLabel rsuLabel_;

    /** Note to describe add rsu mode */
    TextAreaLabel addNote_;

    /** Note to describe delete rsu mode. */
    TextAreaLabel deleteNote_;

    /**
     * ///////////////////////////////////
     * //  method
     * ///////////////////////////////////
     */
    /**
     * Constructor. Creating GUI items.
     */
    public RSUPanel(){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "RSUPanel()", Debug.ISLOGGED);

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

        // Radio buttons to select mode
        ButtonGroup group = new ButtonGroup();
        addRSU_ = new JRadioButton(Messages.getString("RSUPanel.addRSU")); //$NON-NLS-1$
        addRSU_.setActionCommand("addRSU"); //$NON-NLS-1$;
        addRSU_.setSelected(true);
        group.add(addRSU_);
        ++c.gridy;
        add(addRSU_,c);
        addRSU_.addActionListener(this);

        deleteRSU_ = new JRadioButton(Messages.getString("RSUPanel.deleteRSU")); //$NON-NLS-1$
        deleteRSU_.setActionCommand("deleteRSU"); //$NON-NLS-1$
        group.add(deleteRSU_);
        ++c.gridy;
        add(deleteRSU_,c);
        deleteRSU_.addActionListener(this);

        c.gridwidth = 1;
        c.insets = new Insets(5,5,5,5);

        //textfields
        c.gridx = 0;
        rsuLabel_ = new JLabel(Messages.getString("RSUPanel.radius")); //$NON-NLS-1$
        ++c.gridy;
        add(rsuLabel_,c);
        rsuRadius_ = new JFormattedTextField(NumberFormat.getIntegerInstance());
        rsuRadius_.setValue(500);

        rsuRadius_.setPreferredSize(new Dimension(60,20));
        c.gridx = 1;
        add(rsuRadius_,c);

        c.gridx = 0;
        c.gridwidth = 2;
        ++c.gridy;
        add(ButtonCreator.getJButton("deleteAll.png", "clearRSUs", Messages.getString("RSUPanel.btnClearRSUs"), this),c);

        deleteNote_ = new TextAreaLabel(Messages.getString("RSUPanel.noteDelete")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        add(deleteNote_, c);
        deleteNote_.setVisible(false);

        addNote_ = new TextAreaLabel(Messages.getString("RSUPanel.noteAdd")); //$NON-NLS-1$
        ++c.gridy;
        c.gridx = 0;
        add(addNote_, c);
        addNote_.setVisible(true);


        //to consume the rest of the space
        c.weighty = 1.0;
        ++c.gridy;
        add(new JPanel(), c);
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
     * An implemented <code>ActionListener</code> which performs all needed actions when a <code>JRadioButton</code> or <code>JButton</code>
     * is clicked.
     *
     * @param e	an <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
        /** 待新增 */
    }
}