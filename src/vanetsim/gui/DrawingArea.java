package vanetsim.gui;

import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.localization.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class represents a <code>JComponent</code> on which all map elements are painted. It just creates
 * the basic system which is needed, rendering itself is delegated to the {@link Renderer}-class!
 *
 * @see vanetsim.gui.Renderer
 */
public final class DrawingArea extends JComponent implements MouseWheelListener, KeyListener, MouseListener {


    /**
     * ////////////////////////
     * // instance variable
     * ////////////////////////
     */
    /** If <code>true</code>, a temporary Image is used to create a manual DoubleBuffering. */
    private final boolean drawManualBuffered_;


    /**
     * ///////////////////////
     * // method
     * ///////////////////////
     */
    /**
     * Constructor.
     * 由VanetSimStart初始化MainFrame時呼叫
     * @param useDoubleBuffer		<code>true</code> to set DoubleBuffering on, <code>false</code> to set it off
     * @param drawManualBuffered	set to <code>true</code> to use a <code>BufferdImage</code> for drawing (manual DoubleBuffering)
     */
    public DrawingArea(boolean useDoubleBuffer, boolean drawManualBuffered){


        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "DrawingArea(boolean useDoubleBuffer, boolean drawManualBuffered)", Debug.ISLOGGED);

        drawManualBuffered_ = drawManualBuffered;
        setBackground(Color.white);
        setDoubleBuffered(useDoubleBuffer);
        setOpaque(true);
        setIgnoreRepaint(false);
        setFocusable(true);
        addMouseWheelListener(this);
        addKeyListener(this);
        addMouseListener(this);
        ErrorLog.log(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getAvailableAcceleratedMemory()/(1024*1024) + Messages.getString("DrawingArea.acceleratedVRAM"), 4, this.getName(), "init", null); //$NON-NLS-1$ //$NON-NLS-2$
    }




    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }
}
