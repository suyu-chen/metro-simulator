import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Polygon;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * Train.java
 * A class representing a train on the network that is able to move along lines
 * @author Suyu
 * @version 1.0
 */
public class Train implements DrawableGameObject{

    // constants
    private static final double COS_45 = 1/Math.sqrt(2);
    private static final int STOP_FRAMES = GameAreaPanel.FPS*3/4;
    private static final double SPEED_GRID_PER_SEC = 3.0;
    static final int MAX_CAPACITY = 9;

    // static variables
    private static double speed;
    private static ScreenGrid grid = null;
    private static int halfTrainLength, halfTrainWidth;
    private static int numberDisplayFontSize;

    // associated line
    private MetroLine line;

    // movement and positioning variables
    private LinkedList<int[]> linePoints;
    private double x, y;
    private double moveX, moveY;
    private int direction; // 1 for moving in the direction of the order of the line's data, -1 for the opposite direction
    private int[] lastPoint;
    private int[] nextPoint;
    private int nextPointIndex;
    private int nextStationIndex;
    private boolean hitPointAlready = false;
    private int stopFrameCounter = 0;
    private int rotation;
    private Shape graphic;
    
    // variables for passengers
    private ArrayList<Passenger> passengers = new ArrayList<Passenger>();
    private DisplayedNumber paxNumberDisplay;
    
    /**
     * Creates a new train on a specified line at a specified station (given by the
     * index of the station in the line's list of stations) and a specified
     * direction, with 1 being the ascending direction in the line's point and
     * station data, and -1 being the other direction
     * 
     * @param line the MetroLine that this train will run on
     * @param startingStationIndex the index of the station where the train starts
     *                             in the line's list of stations
     * @param direction the direction in which the train will travel, with 1 being 
     *                  the ascending direction in the line's point and station data,
     *                  and -1 being the other direction. If this is not set to 1 or 
     *                  -1, it will default to 1.
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called with a valid grid
     */
    public Train(MetroLine line, int startingStationIndex, int direction) throws ClassNotInitializedException{
        if(!isInitialized()){
            throw new ClassNotInitializedException();
        }

        // Convert speed in grid sizes per second to pixels per frame
        Train.speed = SPEED_GRID_PER_SEC*grid.getGridSize()/GameAreaPanel.FPS; 

        // copy over data
        this.line = line;
        this.linePoints = line.getPoints();
        this.x = grid.gridXToScreen(line.getStations().get(startingStationIndex).getX());
        this.y = grid.gridYToScreen(line.getStations().get(startingStationIndex).getY());
        if(Math.abs(direction)==1){
            this.direction = direction;
        }else{
            this.direction = 1;
        }
        this.nextPoint = grid.gridXYToScreen(line.getStations().get(startingStationIndex).getXY());
        this.nextPointIndex = linePoints.indexOf(nextPoint);
        this.nextStationIndex = startingStationIndex;

        // create the display of number of passengers on the train
        this.paxNumberDisplay = new DisplayedNumber(0, numberDisplayFontSize, Color.BLACK,
                grid.gridXToScreen((int) x) + grid.getGridSize() / 2, grid.gridYToScreen((int) y));

        // update method finishes initialization
        updateForNewPoint();

        // initialize graphic so it doesn't result in null pointer exception when drawing the first frame
        graphic = getHorizontalTrain((int) Math.round(x), (int) Math.round(y));

    }

    /**
     * Creates a new train on a specified line, at the starting terminus of the line
     * (the station with index 0 in the line's data) travelling towards the other terminus
     * @param line the MetroLine that this train will run on
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called with a valid grid
     */
    public Train(MetroLine line) throws ClassNotInitializedException{
        this(line, 0, 1);
    }

    /**
     * Initializes class static variables with information from the game grid (screen 
     * size). Must be called before any Train instances are created
     * @param grid the ScreenGrid object representing the game grid
     */
    public static void initializeClass(ScreenGrid grid){
        halfTrainLength = (int) Math.round(grid.getGridSize()*0.45);
        halfTrainWidth = (int) Math.round(grid.getGridSize()*0.3);
        numberDisplayFontSize = (int) Math.round(grid.getGridSize()*0.4);
        Train.grid = grid;
    }

    /**
     * Returns whether or not initializeClass() has been called using a valid game grid
     * @return true if this class has been initialized with a valid game grid, false otherwise
     */
    public static boolean isInitialized(){
        if(grid == null){
            return false;
        }else{
            return true;
        }
    }
    
    /**
     * Determines if a point's x and y coordinates are within 1 unit of another point's coordinates
     * @param point1 the first point, in a size 2 double array 
     * @param point2 the second point, in a size 2 int array 
     * @return true if both the x and y coordinates of the second point are within 1 unit of the 
     *         x and y coordinates of the other, false otherwise
     */
    private static boolean nearPoint(double[] point1, int[] point2){
        if((Math.abs(point1[0] - point2[0])<speed/2) &&(Math.abs(point1[1] - point2[1])<speed/2)){
            return true;
        }
        return false;
    }

    /**
     * Determines if a point's x and y coordinates are within 1 unit of another point's coordinates
     * @param point1 the first point, in a size 2 int array 
     * @param point2 the second point, in a size 2 int array 
     * @return true if both the x and y coordinates of the second point are within 1 unit of the 
     *         x and y coordinates of the other, false otherwise
     */
    private static boolean nearPoint(int[] point1, int[] point2){
        if((Math.abs(point1[0] - point2[0])<speed/2) &&(Math.abs(point1[1] - point2[1])<speed/2)){
            return true;
        }
        return false;
    }

    /**
     * Get a Rectangle object depicting a train at a specific coordinate, rotated horizontally
     * @param centerX the x coordinate of the center of the train
     * @param centerY the y coordinate of the center of the train
     * @return a Rectangle object representing a train at the specified coordinate, rotated horizontally
     */
    private static Rectangle getHorizontalTrain(int centerX,int centerY){
        return new Rectangle(centerX-halfTrainLength, centerY-halfTrainWidth, halfTrainLength*2, halfTrainWidth*2);
    }

    /**
     * Get a Rectangle object depicting a train at a specific coordinate, rotated vertically
     * @param centerX the x coordinate of the center of the train
     * @param centerY the y coordinate of the center of the train
     * @return a Rectangle object representing a train at the specified coordinate, rotated vertically
     */
    private static Rectangle getVerticalTrain(int centerX,int centerY){
        return new Rectangle(centerX-halfTrainWidth, centerY-halfTrainLength, halfTrainWidth*2, halfTrainLength*2);
    }

    /**
     * Get a Polygon object depicting a train at a specific coordinate, rotated diagonally
     * at a 45 degree angle counterclockwise from the positive x axis
     * @param centerX the x coordinate of the center of the train
     * @param centerY the y coordinate of the center of the train
     * @return a Polygon object representing a train at the specified coordinate, rotated diagonally
     * at a 45 degree angle counterclockwise from the positive x axis
     */
    private static Polygon getRightDiagTrain(int centerX,int centerY){
        return new Polygon(
                new int[] { (int) Math.round(centerX + (halfTrainLength + halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (-halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (-halfTrainLength + halfTrainWidth) * COS_45) },
                new int[] { (int) Math.round(centerY + (-halfTrainLength + halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (-halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (halfTrainLength + halfTrainWidth) * COS_45) },
                4);
    }

    /**
     * Get a Polygon object depicting a train at a specific coordinate, rotated diagonally
     * at a 135 degree angle counterclockwise from the positive x axis
     * @param centerX the x coordinate of the center of the train
     * @param centerY the y coordinate of the center of the train
     * @return a Polygon object representing a train at the specified coordinate, rotated diagonally
     * at a 135 degree angle counterclockwise from the positive x axis
     */
    private static Polygon getLeftDiagTrain(int centerX,int centerY){
        return new Polygon(
                new int[] { (int) Math.round(centerX + (halfTrainLength + halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (-halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerX + (-halfTrainLength + halfTrainWidth) * COS_45) },
                new int[] { (int) Math.round(centerY + (halfTrainLength - halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (halfTrainLength + halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (-halfTrainLength + halfTrainWidth) * COS_45),
                            (int) Math.round(centerY + (-halfTrainLength - halfTrainWidth) * COS_45) },
                4);
    }

    /**
     * Gets the next station this train is headed to. When a train is stopped at a
     * station, this will still return the next station, not the current one.
     * 
     * @return the train's next stop on the line
     */
    public Station getNextStation(){
        return this.line.getStations().get(nextStationIndex);
    }

    /**
     * Gets the ArrayList of all passengers on this train
     * @return the ArrayList of all passengers on this train
     */
    public ArrayList<Passenger> getPassengers(){
        return this.passengers;
    }

    /**
     * Adds a passenger onto this train
     * @param passenger the passenger boarding the train
     */
    public void boardPassenger(Passenger passenger){
        this.passengers.add(passenger);
    }

    /**
     * Updates the display of the number of passengers on this train
     */
    public void updatePaxDisplay(){
        paxNumberDisplay.changeNumber(passengers.size());
    }

    /**
     * Returns whether or not this train is full
     * @return true if the train is at maximum capacity, false otherwise.
     */
    public boolean full(){
        if(passengers.size()>=MAX_CAPACITY){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Updates this train's data if the line this train is on is extended from the
     * beginning of the train's data, since extending from the beginning messes up
     * the indices of where the train currently is
     * @param pointsAdded the number of points added to the beginning of the line
     */
    public void updateForLineBeginningExtension(int pointsAdded, int stationsAdded){
        nextPointIndex += pointsAdded;
        nextStationIndex += stationsAdded;
    }

    /**
     * Updates everything that needs to be updated when a train hits a point on the
     * line, including orientation, movement, roatation, and direction. Also updates
     * passengers and stops the train at stations.
     */
    private void updateForNewPoint(){
        // since train just arrived at next point, set last point to next point
        lastPoint = nextPoint;

        // change direction if arrived at terminus
        if(nextPoint.equals(linePoints.getFirst())){
            direction = 1;
        }else if(nextPoint.equals(linePoints.getLast())){
            direction = -1;
        }

        // set next point to the point the train should be travelling towards
        nextPointIndex+= direction;
        nextPoint = linePoints.get(nextPointIndex);

        // set x and y to be the coordinates of the point - prevents compoounding floating point error
        x = lastPoint[0];
        y = lastPoint[1];

        // figure out new orientation and speed
        int dx = nextPoint[0]-lastPoint[0];
        int dy = nextPoint[1]-lastPoint[1];
        if(dy == 0){  // horizontal
            rotation = 0;
            moveX = speed*Math.signum(dx);
            moveY = 0;
        }else if(dx == 0){  // vertical
            rotation = 90;
            moveX = 0;
            moveY = speed*Math.signum(dy);
        }else{  // diagonal
            if(dx*dy < 0){  // pointing to top left or bottom right
                rotation = 45;   
            }else if(dx*dy>0){   // pointing to top right or bottom left
                rotation = 135;
            }
            moveX = speed*Math.signum(dx)*COS_45;
            moveY = speed*Math.signum(dy)*COS_45;       
        }

        // if train just arrived at the next station
        if(nearPoint(lastPoint, grid.gridXYToScreen(line.getStations().get(nextStationIndex).getXY()))){
            
            stopFrameCounter = STOP_FRAMES; // stop the train
            nextStationIndex += direction; // update next station
            
            // update all the passengers
            for(Passenger passenger:passengers){
                passenger.updateAtNextStation();
            }

            line.getStations().get(nextStationIndex-direction).boardAndAlight(this);

        }
    }

    /**
     * Updates this train. Updates location every frame, updates orientation,
     * movement, and direction when hitting a point on the line, and updates
     * passengers and stops when hitting a station
     */
    public void update() {
        // If train is near a point on the line, run updateForNewPoint to update direction, orientation, speed
        if((!hitPointAlready) && (nearPoint(new double[]{x,y}, nextPoint))){
            updateForNewPoint();
            hitPointAlready = true; // prevent updateForNewPoint from running multiple times for the same point

        // reset hitPointAlready when train has passed last point
        }else if((hitPointAlready) && (!nearPoint(new double[]{x,y}, lastPoint))){
            hitPointAlready = false;
        }

        // decrement the stop frame counter if the train is stopped at station, otherwise move the train
        if(stopFrameCounter != 0){
            stopFrameCounter --; 
        }else{
            x += moveX;
            y += moveY;
            paxNumberDisplay.updatePosition((int) x, (int) y);
        }
        
        // update the Shape object of the train drawn on screen with new orientation/location
        if(rotation==0){
            graphic = getHorizontalTrain((int) Math.round(x), (int) Math.round(y));
        }else if(rotation == 90){
            graphic = getVerticalTrain((int) Math.round(x), (int) Math.round(y));
        }else if(rotation == 45){
            graphic = getRightDiagTrain((int) Math.round(x), (int) Math.round(y));
        }else if(rotation == 135){
            graphic = getLeftDiagTrain((int) Math.round(x), (int) Math.round(y));
        }

    }

    /**
     * Draws this train and the number of passengers on the train on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics graphics, ScreenGrid grid) {
        Graphics2D g = (Graphics2D) graphics;

        // draw the train graphic
        g.setColor(line.getColor());
        g.fill(graphic);

        // draw the display of the number of passengers
        paxNumberDisplay.draw(g);
    }
}
