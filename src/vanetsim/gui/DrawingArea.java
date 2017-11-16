package vanetsim.gui;

import vanetsim.ErrorLog;
import vanetsim.debug.Debug;
import vanetsim.localization.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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
    /** The constant for serializing. */
    private static final long serialVersionUID = -5210801710805449919L;

    /** The zoom value. Should be higher than 1. Setting this lower means faster zooming with mouse wheel, setting it higher means slower zooming. */
    private static final double ZOOM_VALUE = 5.0;

    /** If <code>true</code>, a temporary Image is used to create a manual DoubleBuffering. */
    private final boolean drawManualBuffered_;

    /** A reference to the singleton instance of the {@link Renderer} because we need this quite often and don't want to rely on compiler inlining. */
    private final Renderer renderer_ = Renderer.getInstance();

    /** An <code>AffineTransform</code> which does not transform any coordinates. */
    private final AffineTransform nullTransform_ = new AffineTransform();

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
     * This method gets automatically called on a <code>repaint()</code>. Therefore, rendering is delegated from here to
     * the renderer.
     * 此 paintComponent(Graphics g) 由系統(jvm)自行決定呼叫時機
     * 呼叫時機如下：
     * 呼叫 repaint() 方法
     * 視窗位置/大小 變動
     *
     * @param g	the <code>Graphics</code> object to paint on
     */
    public void paintComponent(Graphics g){

        Debug.callFunctionInfo(this.getClass().getName(), "paintComponent(Graphics g)", Debug.ISLOGGED);


        // Note: Normally, "super.paintComponent(_g)" should be called here to prevent garbage on the screen.
        // However, this can induce a huge performance hit and as the buffered streetimage is as large as the area,
        // it shouldn't give problems with garbage shining through.
        //to prevent this function from overwriting anything while rendering is in progress!
        synchronized(renderer_){
            if(streetsImage_ == null || getWidth() != streetsImage_.getWidth() || getHeight() != streetsImage_.getHeight()){
                prepareBufferedImages();
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if(drawManualBuffered_ == true){
                temporaryG2d_.setTransform(nullTransform_);
                temporaryG2d_.drawImage(streetsImage_, 0, 0, null, this);	// draw the prerendered static objects
                renderer_.drawMovingObjects(temporaryG2d_);	// draw moving objects
                temporaryG2d_.drawImage(scaleImage_, getWidth()-120, getHeight()-40, null, this);		//draw the measure
                g2d.drawImage(temporaryImage_, 0, 0, null, this);	// output temporary image
            } else {
                g2d.drawImage(streetsImage_, 0, 0, null, this); // draw the prerendered static objects
                renderer_.drawMovingObjects(g2d);	// draw moving objects
                g2d.drawImage(scaleImage_, getWidth()-120, getHeight()-40, null, this);		//draw the measure
            }

            g2d.dispose();	// should be disposed to aid garbage collector
        }
    }


    /**
     * Prepares all <code>BufferedImages</code> and notifies the {@link Renderer} of a new drawing area size.
     */
    public void prepareBufferedImages(){

        Debug.callFunctionInfo(this.getClass().getName(),"prepareBufferedImages()",Debug.ISLOGGED);

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
