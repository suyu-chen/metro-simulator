import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.HashMap;

/**
 * Station.java
 * A class representing a station on the network
 * @author Suyu
 * @version 1.0
 */
class Station implements DrawableGameObject{

    // Static variables - define size of stations on screen
    private static int stationCircleInnerWidth = 0;
    private static int stationCircleOuterWidth = 0;
    private static int numberDisplayFontSize;
    private static Network network;

    // Random
    private Random random = new Random();

    // variables
    private int x, y;
    private ArrayList<Passenger> passengers = new ArrayList<Passenger>();
    private DisplayedNumber paxNumberDisplay;

    // Connections - for path finding
    private HashMap<Station, Double> connectedStations = new HashMap<Station, Double>();

    // Overcrowding
    static final int OVERCROWDING_WARNING = 15;
    static final int OVERCROWDING_LIMIT = 20;
    static final Color WARNING_COLOR = new Color(200,0,0);

    /**
     * Creates a new station at the location specified by two integer coordinates
     * @param gridX the x coordinate on the game grid of the new station
     * @param gridY the y coordinate on the game grid of the new station
     * @param grid the ScreenGrid object representing the game grid
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called with a valid grid
     */
    public Station(int gridX, int gridY, ScreenGrid grid) throws ClassNotInitializedException{
        if(!isInitialized()){
            throw new ClassNotInitializedException();
        }
        this.x = gridX;
        this.y = gridY;
        this.paxNumberDisplay = new DisplayedNumber(0, numberDisplayFontSize, Color.BLACK,
                grid.gridXToScreen(x) + grid.getGridSize() * 3 / 4, grid.gridYToScreen(y));
    }

    /**
     * Creates a new station at the coordinates specified by an length 2 integer array
     * @param gridXY A length 2 integer array containing the coordinates of the new
     *               station on the game grid
     * @param grid the ScreenGrid object representing the game grid
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called with a valid grid
     */
    public Station(int[] gridXY, ScreenGrid grid) throws ClassNotInitializedException{
        this(gridXY[0], gridXY[1], grid);
    }

    /**
     * Creates a new station at the center of the game grid
     * @param grid ScreenGrid object representing the game grid
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called with a valid grid
     */
    public Station(ScreenGrid grid) throws ClassNotInitializedException{
        this(grid.getW()/2, grid.getH()/2, grid);
    }

    /**
     * Initializes class static variables with information from the game grid (screen 
     * size). Must be called before any Station instances are created
     * @param grid the ScreenGrid object representing the game grid
     */
    public static void initializeClass(ScreenGrid grid, Network network){
        stationCircleInnerWidth = (int) Math.round(grid.getGridSize()*0.5);
        stationCircleOuterWidth = (int) Math.round(grid.getGridSize()*0.8);
        numberDisplayFontSize = (int) Math.round(grid.getGridSize()*0.6);
        Station.network = network;
    }

    /**
     * Returns whether or not initializeClass() has been called using a valid game grid
     * @return true if this class has been initialized with a valid game grid, false otherwise
     */
    public static boolean isInitialized(){
        if(stationCircleOuterWidth == 0){
            return false;
        }else{
            return true;
        }
    }

    /**
     * Gets the x coordinate of this station
     * @return the x coordinate of this station on the game grid
     */
    public int getX(){
        return x;
    }

    /**
     * Gets the y coordinate of this station
     * @return the y coordinate of this station on the game grid
     */
    public int getY(){
        return y;
    }

    /**
     * Gets the coordinates of this station
     * @return an integer array, the x and y of this station on the game grid
     */
    public int[] getXY(){
        return new int[]{x,y};
    }
    
    /**
     * Gets the diameter of all stations
     * @return the total diameter of a station, in units of pixels on the screen
     */
    public static int getOuterWidth(){
        return stationCircleOuterWidth;
    }

    /**
     * Adds a connection in this station's adjacency list of connected stations.
     * Also automatically calculates the distance to that station.
     * 
     * @param station the station to connect to this station
     */
    public void addConnection(Station station){
        connectedStations.put(station, getMetroMapDistance(station));
    }

    /**
     * Gets the HashMap adjacency list storing all stations connected to this one
     * and the distance to each one in grid units
     * 
     * @return the HashMap adjacency list storing all stations connected to this one
     * as keys and the distance to each one in grid units as the values
     */
    public HashMap<Station, Double> getConnections(){
        return connectedStations;
    }

    /**
     * Attempts to find paths for all passengers at this station that could not
     * previously find a path to their destination
     */
    public void findPathsForPassengersWithNoPath(){
        for(Passenger passenger:passengers){
            if(!passenger.hasPath()){
                passenger.findPath();
            }
        }
    }

    /**
     * Adds a passenger to this station. Intended for use when train drops off a
     * passenger at this station to transfer to another line.
     * 
     * @param passenger the Passenger that is transferring.
     */
    private void addTransferringPassenger(Passenger passenger){
        passengers.add(passenger);
    }

    /**
     * Checks passengers at this station and on a train that has stopped at this
     * station and determines whether any passengers should board or alight the
     * train at this station, then moves or removes passengers from the train and/or
     * this station accordingly. Also increments the player's score if a passenger
     * is delivered to their destination.
     * 
     * @param train the train that has stopped at this station.
     */
    public void boardAndAlight(Train train){

        // variables
        ListIterator<Passenger> paxListIt = train.getPassengers().listIterator(train.getPassengers().size());
        Passenger passenger;

        // for each passenger on the train
        while(paxListIt.hasPrevious()){
            passenger = paxListIt.previous();

            // if this station is the passenger's destination
            if(passenger.getNextOnPath() == null){
                paxListIt.remove(); // remove from train
                network.incrementScore(); // increment player's score

            // if the passenger wants to transfer
            }else if(passenger.getNextOnPath() != train.getNextStation()){
                paxListIt.remove(); // remove from train
                this.addTransferringPassenger(passenger); // add to station
            }
        }

        paxListIt = passengers.listIterator(passengers.size());
        // for each passenger waiting at this station and while train is not full
        while(paxListIt.hasPrevious() && !train.full()){
            passenger = paxListIt.previous();

            // board the train if the station it is heading to is the same as the passenger's intended next station
            if(passenger.getNextOnPath() == train.getNextStation()){
                paxListIt.remove(); // remove from station
                train.boardPassenger(passenger); // add to train
            }
        }

        // update the number of passengers displayed on both the train and station
        train.updatePaxDisplay(); 
        this.updatePaxDisplay();
    }

    /**
     * Gets the distance between this station and another station on the map in
     * units of game grid sizes along only orthogonal or 45 degree diagonal lines
     *
     * @param s the other station
     * @return the distance between this staiton and another station along only
     *         orthogonal or 45 degree diagonal lines
     */
    private double getMetroMapDistance(Station s){
        int longerDist = Math.max(Math.abs(s.getX()-this.x), Math.abs(s.getY()-this.y));
        int shorterDist = Math.min(Math.abs(s.getX()-this.x), Math.abs(s.getY()-this.y));
        return longerDist - shorterDist + shorterDist * Math.sqrt(2);
    }

    /**
     * Generates passengers randomly based on the current passenger generation rate,
     * which is the percent chance of a passenger appearing at any station in each
     * second. Also updates the display of the number of passengers at the station
     * 
     * @param passengerGenerationRate the change of a passenger generating at a
     *                                station in each second
     */
    public void generatePassengers(int passengerGenerationRate) {
        if(random.nextInt(100*GameAreaPanel.FPS)<passengerGenerationRate){
            passengers.add(new Passenger(this));
            updatePaxDisplay();
        }

    }

    /**
     * Gets whether or not this station is approaching the overcrowding limit
     * @return true if this station is approaching the overcrowding limit, false otherwise
     */
    public boolean approachingOvercrowding(){
        if(passengers.size()>=OVERCROWDING_WARNING){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Gets whether or not this station is past the overcrowding limit
     * @return true if this station is past the overcrowding limit, false otherwise
     */
    public boolean isOvercrowded(){
        if(passengers.size()>=OVERCROWDING_LIMIT){
            return true;
        }else{
            return false;
        }       
    }

    /**
     * Updates the display of the number of passengers waiting at this station, and
     * changes the color of the number to red when the station starts getting
     * crowded
     */
    public void updatePaxDisplay(){
        paxNumberDisplay.changeNumber(passengers.size()); 
        if(this.approachingOvercrowding()){
            paxNumberDisplay.changeColor(WARNING_COLOR);
        }else{
            paxNumberDisplay.changeColor(Color.BLACK);
        }
    }

    /**
     * Draws this station and the number of passengers in the station on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics g, ScreenGrid grid) {
        
        // draw the station icon
        g.setColor(Color.BLACK);
        g.fillOval(grid.gridXToScreen(x) - stationCircleOuterWidth / 2,
                grid.gridYToScreen(y) - stationCircleOuterWidth/2, stationCircleOuterWidth, stationCircleOuterWidth);
        g.setColor(Color.WHITE);
        g.fillOval(grid.gridXToScreen(x) - stationCircleInnerWidth / 2,
                grid.gridYToScreen(y) - stationCircleInnerWidth / 2, stationCircleInnerWidth, stationCircleInnerWidth);

        // draw the display of the number of passengers
        paxNumberDisplay.draw(g);
        
    }
}
