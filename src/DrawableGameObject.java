import java.awt.Graphics;

/**
 * DrawableGameObject.java 
 * Interface for game objects that can be drawn on the game grid.
 * @author Suyu
 * @version 1.0
 */
public interface DrawableGameObject{
    abstract void draw(Graphics g, ScreenGrid grid);
}