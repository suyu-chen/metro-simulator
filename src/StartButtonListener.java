import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * StartButtonListener.java
 * Class that used to detect a button press
 * @author Mangat, Suyu
 * @version 1.0
 */
class StartButtonListener implements ActionListener {

  // the JFrame associated with this StartButtonListener
  JFrame parentFrame;

  /**
   * Creates a new StartButtonListener with an associated JFrame
   * @param parent the JFrame associated with this StartButtonListener
   */
  StartButtonListener(JFrame parent) {
    parentFrame = parent;
  }

  /**
   * Invoked when the button is pressed.
   * Disposes the parent frame and creates a new GameFrame to start the game
   * 
   * @param event the ActionEvent
   */
  public void actionPerformed(ActionEvent event) {
    System.out.println("Starting new Game");
    parentFrame.dispose();
    new GameFrame(); // create a new frame after removing the current one
  }
}