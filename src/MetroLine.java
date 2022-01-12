import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

/**
 * MetroLine.java
 * A class representing a metro line with trains on the map
 * @author Suyu
 * @version 1.0
 */
public class MetroLine implements DrawableGameObject{

    // variables
    private static BasicStroke lineStroke;
    private Color lineColor;
    private LinkedList<Station> stations = new LinkedList<Station>();
    private LinkedList<int[]> points = new LinkedList<int[]>();
    private Path2D.Float path = new Path2D.Float();
    private ArrayList<Train> trains = new ArrayList<Train>(); 
    
    /**
     * Creates a new MetroLine with one train from a TempMetroLine, with all of the
     * stations, points, and color from the TempMetroLine.
     *  
     * @param tempLine the TempMetroLine to use to create a new MetroLine
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called yet
     */
    public MetroLine(TempMetroLine tempLine) throws ClassNotInitializedException{
        if(!isInitialized()){
            MetroLine.lineStroke = TempMetroLine.getStroke();
            if(!isInitialized()){
                throw new ClassNotInitializedException();
            }
        }
        this.stations = tempLine.getStations();
        this.path = tempLine.getPath();
        this.points = tempLine.getPoints();
        this.lineColor = tempLine.getColor();
        trains.add(new Train(this));
    }

    /**
     * Extends this MetroLine using the points and stations of a TempMetroLine
     * starting from one of the termini of this line.
     * 
     * @param tempLine the TempMetroLine to use to extend this MetroLine
     */
    public void extendLine(TempMetroLine tempLine){

        // ensure the tempLine is actually more than 1 station
        if(tempLine.hasMultipleStations()){
            LinkedList<int[]> pointsToAdd = tempLine.getPoints();
            LinkedList<Station> stationsToAdd = new LinkedList<Station> (tempLine.getStations());

            // Processing the tempLine - remove the first entries in the points and stations, 
            // since these are already part of this MetroLine
            pointsToAdd.removeFirst(); 
            stationsToAdd.removeFirst();

            // case where the line is being extended from the 'end' of the data structures
            if(this.stations.getLast()==tempLine.getStations().getFirst()){

                // add everything
                this.stations.addAll(stationsToAdd);
                this.points.addAll(pointsToAdd);
                this.path.append(tempLine.getPath(), false);

            // Case where line is being extended fron the 'beginning' of the data structures
            }else if(this.stations.getFirst()==tempLine.getStations().getFirst()){

                // need to reverse everything before adding
                Collections.reverse(pointsToAdd);
                Collections.reverse(stationsToAdd);
                this.stations.addAll(0, stationsToAdd);
                this.points.addAll(0, pointsToAdd);

                // remake path, since it's not possible to extend from the beginning of a Path2D
                path.reset();
                path = Path2DTools.makePathWithPoints(this.points);

                // Update train's data since the indexes all changed
                for(Train train:trains){
                    train.updateForLineBeginningExtension(pointsToAdd.size(), stationsToAdd.size());
                }
            }
            
        }
    }

    /**
     * Initializes class static variables with information from the game grid (screen 
     * size). Must be called before any MetroLine instances are created
     * @param grid the ScreenGrid object representing the game grid
     */
    public static void initializeClass(ScreenGrid grid){
        lineStroke = new BasicStroke((int) Math.round(grid.getGridSize() * 0.2), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
    }

    /**
     * Returns whether or not initializeClass() has been called using a valid game grid
     * @return true if this class has been initialized with a valid game grid, false otherwise
     */
    public static boolean isInitialized(){
        if(lineStroke == null){
            return false;
        }else{
            return true;
        }
    }

    /**
     * Gets a LinkedList of all stations on this line, in order
     * @return a LinkedList of all stations on this line, in order
     */
    public LinkedList<Station> getStations(){
        return stations;
    }

    /**
     * Gets the color of this line
     * @return A Color object; the color of this line
     */
    public Color getColor(){
        return lineColor;
    }

    /**
     * Gets a LinkedList of all points on this line, in order
     * @return a LinkedList of all points on this line, in order
     */
    public LinkedList<int[]> getPoints(){
        return points;
    }

    /**
     * Updates the state of all trains on this line
     */
    public void updateTrains() {
        for(Train train:trains){
            train.update();
        }
    }

    /**
     * Draws this MetroLine and associated trains on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics graphics, ScreenGrid grid) {

        // set graphics stuff, need to cast to Graphics2D to use g.draw()
        Graphics2D g = (Graphics2D) graphics;
        g.setStroke(lineStroke);
        g.setColor(lineColor);

        // draw the line
        g.draw(path);

        // draw all trains on the line
        for(Train train:trains){
            train.draw(g,grid);
        }

    }

}
