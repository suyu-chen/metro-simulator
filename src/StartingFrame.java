import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Toolkit;

/**
 * StartingFrame.java
 * The start menu for the game 
 * @author Mangat, Suyu
 * @version 1.0
 */
class StartingFrame extends JFrame { 

  JFrame thisFrame;
  
  /**
   * Creates a new StartingFrame and initializes stuff
   */
  StartingFrame() { 
    super("Metro Lines");
    this.thisFrame = this; //lol  
    
    //configure the window
    this.setSize((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4,
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() * 5 / 6);
    this.setLocationRelativeTo(null); //start the frame in the center of the screen
    this.setResizable (false);
    
    // Create a Panel for stuff
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    // Create a JButton for the centerPanel
    JButton startButton = new JButton("START");
    startButton.addActionListener(new StartButtonListener(this));
    
    // Create a JLabel for the screen
    JLabel startLabel = new JLabel("<HTML><H1>Metro Lines</H1></HTML>",JLabel.CENTER);
    
    //Add all panels to the mainPanel according to border layout
    mainPanel.add(startButton,BorderLayout.SOUTH);
    mainPanel.add(startLabel,BorderLayout.CENTER);
    
    //add the main panel to the frame
    this.add(mainPanel);
    this.setVisible(true);
    this.requestFocusInWindow();
  }
  

  /**
   * Main method - starts the application
   * @param args 
   */
  public static void main(String[] args) { 
    new StartingFrame();
  }
  
}