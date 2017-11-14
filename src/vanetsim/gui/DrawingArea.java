package vanetsim.gui;

import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.localization.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

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

    /** A reference to the singleton instance of the {@link Renderer} because we need this quite often and don't want to rely on compiler inlining. */
    private final Renderer renderer_ = Renderer.getInstance();

    /**
     * BufferedImage 類變數
     * 它是 Image 類的一個子類，它把圖像數據存儲在一個可以被訪問的緩衝區中。它還支持各種存儲像素數據的方法
     */
    /** The street map is static (only changed through zooming/panning) so it's efficient to just store its image representation in memory. */
    private BufferedImage streetsImage_ = null;

    /** A temporary <code>BufferedImage</code> which is used when <code>DrawManualBuffered=true</code>. */
    private BufferedImage temporaryImage_ = null;

    /** An image in which the current scale is drawn. */
    private BufferedImage scaleImage_ = null;




    /** A temporary <code>Graphics2D</code> based on <code>temporaryImage</code>. */
    private Graphics2D temporaryG2d_ = null;

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



    /**
     * Prepares all <code>BufferedImages</code> and notifies the {@link Renderer} of a new drawing area size.
     */
    public void prepareBufferedImages(){
        if(streetsImage_ == null || getWidth() != streetsImage_.getWidth() || getHeight() != streetsImage_.getHeight()){	//prepare new image for streets ("static objects")
            renderer_.setDrawHeight(getHeight());
            renderer_.setDrawWidth(getWidth());
            renderer_.updateParams();
            streetsImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
        }
        if(drawManualBuffered_ == true && (temporaryImage_ == null || getWidth() != temporaryImage_.getWidth() || getHeight() != temporaryImage_.getHeight())){	//create image for manual double buffering
            temporaryImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.OPAQUE);
            temporaryG2d_ = temporaryImage_.createGraphics();
            temporaryG2d_.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if(scaleImage_ == null){		//just needs to be created one single time!
            scaleImage_ = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(100, 30, Transparency.OPAQUE);
            Graphics2D tmpgraphics = scaleImage_.createGraphics();
            tmpgraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            tmpgraphics.setColor(Color.black);
            tmpgraphics.fillRect(0, 0, 100, 30);
        }
        renderer_.drawStaticObjects(streetsImage_);
        /** 繪製比例尺 */
        renderer_.drawScale(scaleImage_);
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
