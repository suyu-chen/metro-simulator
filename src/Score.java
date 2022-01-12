import java.awt.Graphics;
import java.awt.Color;

/**
 * Score.java
 * A class storing the player's score and for drawing the score on the screen
 * @author Suyu
 * @version 1.0
 */
public class Score extends DisplayedText{
    private int score;
    
    /**
     * Creates a new Score object, with the score set to 0, black text, and using
     * the game grid to scale text
     * 
     * @param grid the ScreenGrid object representing the game grid
     */
    public Score(ScreenGrid grid){
        super("Score: 0", grid.getGridSize(), Color.BLACK);
        score = 0;
    }

    /**
     * Increments the score and updates the text displayed on screen
     */
    public void incrementScore(){
        score++;
        super.changeText("Score: " + score);
    }

    /**
     * Gets the current score
     * @return the current score
     */
    public int getScore(){
        return score;
    }

    /**
     * Draws the score on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics g, ScreenGrid grid) {
        super.draw(g, super.getWidth()/2 + grid.getGridSize()/2, grid.getGridSize()*3/4);
    }
}
