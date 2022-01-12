import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import java.util.ArrayList;

/**
 * LineEditingMouseListener.java
 * A MouseListener used to create and extend MetroLines using TempMetroLines.
 * @author Suyu
 * @version 1.0
 **/
public class LineEditingMouseListener extends MouseInputAdapter {

  // References to network stuff
  private Network network;
  private ScreenGrid grid;
  private ArrayList<Station> stations;
  
  // Variables
  private TempMetroLine tempLine = null;
  private Station previousSelected = null;
  private int multipleTerminusStationChoice = 0;
  private ArrayList<MetroLine> linesTerminatingAtSelected = new ArrayList<MetroLine>();
  
  /**
   * Creates a new LineEditingMouseListener for a Network and establishes local
   * references to Network objects
   * @param network the Network this LineEditingMouseListener is for
   */
  LineEditingMouseListener(Network network){
    this.network = network;
    this.stations = network.getStations();
    this.grid = network.getGrid();
  }

  /**
   * Invoked on mouse button presses.
   * 
   * Checks if the press is on a station, then establishes a new TempMetroLine in
   * the network as an extension if the station was a terminus of an existing
   * line, or as a new line if the station was not an existing line terminus. If
   * multiple lines end at the station where the mouse was pressed, repeated mouse
   * presses will cycle through extending the lines terminating at that station.
   * 
   * @param e the MouseEvent
   */
  public void mousePressed(MouseEvent e) {

    // check if the mouse was pressed on a station
    int pressX = e.getX();
    int pressY = e.getY();
    Station selected = stationSelected(pressX, pressY);

    if(selected != null){ //i.e. if mouse press was on a station

      // Figure out which lines (if any) this station is a terminus of
      linesTerminatingAtSelected = network.getLinesTerminatingAtStation(selected);

      // If this station is not a terminus of any line, add a tempLine as a new line, otherwise, 
      // add a tempLine as an extension of the line terminating at the selected station
      if(linesTerminatingAtSelected.isEmpty()){
        tempLine = network.newTempLine(selected);
      }else{
        // If multiple lines terminate at this station, cycle through them as the user
        // tries extending from the station multiple times
        multipleTerminusStationChoice ++;
        if(multipleTerminusStationChoice >= linesTerminatingAtSelected.size()){
          multipleTerminusStationChoice = 0;
        }
        tempLine = network.newTempLine(selected, linesTerminatingAtSelected.get(multipleTerminusStationChoice));
      }      

      // update this for reference later in mouseDragged
      previousSelected = selected;
    }    
  }

  /**
   * Invoked on mouse button releases.
   * 
   * Finalizes any TempMetroLine that has been created and resets variables to
   * prepare for another mouse press.
   * 
   * @param e the MouseEvent
   */
  public void mouseReleased(MouseEvent e) {
    if(tempLine != null){
      network.finalizeTempLine();
    }
    tempLine = null;
    previousSelected = null;
  }

  /**
   * Invoked when the mouse is dragged (moved while button held down)
   * 
   * Updates the TempMetroLine being drawn (if there is one): Updates the mouse
   * point for the TempMetroLine so that the line extends to the mouse cursor.
   * Adds a station to the TempMetroLine if the mouse is dragged onto a station
   * not already on the line. Removes a station from the TempMetroLine if the
   * mouse is dragged onto the last station that was added.
   * 
   * @param e the MouseEvent
   */
  public void mouseDragged(MouseEvent e) { 
    if(tempLine != null){
      int mouseX = e.getX();
      int mouseY = e.getY();

      tempLine.updateMousePoint(mouseX, mouseY); // method ensures tempMetroLine extends to the mouse cursor

      Station selected = stationSelected(mouseX, mouseY);
      if((selected!=null)&&(selected!=previousSelected)){
        tempLine.addRemoveStation(selected); // add or remove station depending on if it is already on the line; remove
                                             // if the station was the last one just added
        previousSelected = selected; // prevents the same station from being added multiple times while the mouse is
                                     // still on the same station
      }else if(selected == null){
        previousSelected = null;
      }
    }
  }

  /**
   * Checks if a point on the screen is on the circular icon of any station
   * @param x the x coordinate of the point on the screen
   * @param y the y coordinate of the point on the screen
   * @return the Station that the point lies on, or null if the point is not on a station
   */
  private Station stationSelected(int x, int y){
    // Check if the mouse press is on a station
    for (int i = 0; i < stations.size(); i++) {

      // check if mouse click is in a square bounding a station first - maybe save some resources 
      if ((Math.abs(x- grid.gridXToScreen(stations.get(i).getX())) < Station.getOuterWidth() / 2)
          && (Math.abs(y - grid.gridYToScreen(stations.get(i).getY())) < Station.getOuterWidth() / 2)) {

        // check if mouse click is in the station's circle
        if (Math.hypot(x - grid.gridXToScreen(stations.get(i).getX()),
            y - grid.gridYToScreen(stations.get(i).getY())) < Station.getOuterWidth() / 2) {
          return stations.get(i);
        }
      }
    }
    return null;
  }
  
}
