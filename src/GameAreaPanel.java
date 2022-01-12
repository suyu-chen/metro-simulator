import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JPanel;

/**
 * GameAreaPanel.java
 * Class for the the game area - This is where all the drawing of the screen occurs
 * @author Mangat, Suyu
 * @version 1.0
 */
class GameAreaPanel extends JPanel {

  // main game network
  private Network network;

  // frame stuff
  public static final int FPS = 50;
  public static final int FRAME_TIME = 1000/FPS;

  /**
   * Creates a new GameAreaPanel with an associated GameFrame
   * @param gameFrame the GameFrame associated with this GameAreaPanel
   */
  GameAreaPanel(GameFrame gameFrame){
    // Game Object Initialization
    network = new Network(gameFrame.getSize().width, gameFrame.getSize().height);

    // Listener - Esc key to quit
    EscapeKeyListener escapeKeyListener = new EscapeKeyListener(gameFrame);
    this.addKeyListener(escapeKeyListener);

    // Listener for editing lines
    LineEditingMouseListener lineEditor = new LineEditingMouseListener(network);
    this.addMouseListener(lineEditor);
    this.addMouseMotionListener(lineEditor);

    // JPanel Stuff
    this.setFocusable(true);
    this.setBackground(Color.WHITE);
    this.requestFocusInWindow();

    // Start the game in a separate thread (allows simple frame rate control)
    // the alternate is to delete this and just call repaint() at the end of paintComponent()
    Thread t = new Thread(new Runnable() {public void run(){ animate(); }}); // start the game
    t.start();

  }

  /**
   * Updates the game state for each frame
   */
  public void animate(){

    while (true) {

      // update game content
      network.update();

      // delay
      try {
        Thread.sleep(FRAME_TIME);
      } catch (Exception exc) {
        System.out.println("Thread Error");
      }

      // repaint request
      this.repaint();
    }
  }

  /**
   * Runs every time the screen is refreshed. Draws all game content on the screen.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g); // required
    setDoubleBuffered(true);

    // screen is being refreshed - draw all objects
    network.draw(g);

  }
}
