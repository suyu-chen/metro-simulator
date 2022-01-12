import javax.swing.JFrame;
import java.awt.Toolkit;

/**
 * GameFrame.java
 * The JFrame for this game.
 * @author Mangat, Suyu
 * @version 1.0
 **/
class GameFrame extends JFrame {

  // Game Screen
  static GameAreaPanel gamePanel;

  /**
   * Creates a new full screen game frame.
   */
  GameFrame() {
    super("Metro Simulator");

    // Set the frame to full screen
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Set the size and properties of the game frame
    this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    this.setUndecorated(true); // Set to true to remove title bar
    this.setResizable(false);

    // Set up the game panel (where we put our graphics)
    gamePanel = new GameAreaPanel(this);
    this.add(gamePanel);

    this.setFocusable(false); // we will focus on the JPanel
    this.setVisible(true);
    
  }

}
