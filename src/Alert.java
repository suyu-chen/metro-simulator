import java.awt.Graphics;
import java.awt.Color;

/**
 * Alert.java
 * Class used for displaying alerts to the player
 * @author Suyu
 * @version 1.0
 */
public class Alert extends DisplayedText {

    // Constants
    private final Color BG_COLOR = new Color(0,0,0, (float) 0.6);
    private final int BG_BUFFER = 15;

    // Variables
    private int frameDuration = -1;
    private int framesPassed = 0;

    /**
     * Creates a new permanent alert with specified text and font size
     * @param text     the text of the alert
     * @param fontSize the alert's font size
     */
    public Alert(String text, int fontSize){
        super(text, fontSize, Color.WHITE);
    }

    /**
     * Creates a new alert with a specified duration, text, and font size
     * @param text          the text of the alert
     * @param fontSize      the alert's font size
     * @param frameDuration the alert's duration in frames
     */
    public Alert(String text, int fontSize, int frameDuration){
        this(text, fontSize);
        this.frameDuration = frameDuration;
    }

    /**
     * Returns whether or not the alert is over (the duration has passed)
     * @return true if the alert is over, false otherwise
     */
    public boolean isOver(){
        if((framesPassed>=frameDuration) && (frameDuration != -1)){
            return true;
        }
        return false;
    }

    /**
     * Updates this alert. Should be run every frame to ensure that the duration of
     * this alert is correct
     */
    public void update(){
        framesPassed ++;
    }

    /**
     * Draws this alert on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics g, ScreenGrid grid){
        g.setColor(BG_COLOR);
        g.fillRect((grid.getScreenW() - super.getWidth()) / 2 - BG_BUFFER, (grid.getScreenH() - super.getHeight()) / 2 - BG_BUFFER,
                super.getWidth() + BG_BUFFER * 2, super.getHeight() + BG_BUFFER * 2);
        super.draw(g, grid.getScreenW()/2, grid.getScreenH()/2);
    }

}
