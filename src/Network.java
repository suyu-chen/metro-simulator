import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Network.java
 * The network of lines and stations, handling interactions between 
 * different network elements and changes to the network
 * @author Suyu
 * @version 1.0
 */
public class Network{

    // Game constants
    private final int INITIAL_STATIONS = 3;
    private final int GRID_DENSITY = 36; // higher is denser
    private final int GRID_EDGE_BUFFER = 2; // number of grid spaces to be left empty around edges of screen
    private final int ALPHA = 200;  // alpha values of line colors
    private final Color[] LINE_COLORS = { new Color(255, 203, 12, ALPHA), new Color(22, 167, 83, ALPHA), 
            new Color(31, 153, 213, ALPHA), new Color(179, 32, 120, ALPHA), new Color(248, 112, 5, ALPHA), 
            new Color(136, 136, 136, ALPHA), new Color(149, 216, 163, ALPHA), new Color(239, 115, 171, ALPHA) };
            // ^ this also defines the max number of lines

    // Random
    private Random random = new Random();
    
    // Game grid
    private ScreenGrid grid; 

    // Game objects
    private ArrayList<Station> stations = new ArrayList<Station>();
    private LinkedList<MetroLine> lines = new LinkedList<MetroLine>();
    private TempMetroLine tempLine;
    private Alert alert;

    // Station generation variables
    private StationPoissonDisc stationPoissonDisc;
    private boolean generateStations = true; // is set to false when there are no more valid locations for staitons
    private int stationGenerationChance = 20; // percent chance of a new station in each second
    private int[] newLocation;

    // Passenger generation and path finding 
    private Dijkstra dijkstra;
    private int passengerGenerationRate = 5; // percent change of a passenger appearing at any station in each second

    // Game info
    private Score score;
    private boolean gameOver;
    private boolean gameOverDisplayed = false;


    /**
     * Creates a new Network object and initializes all relevant classes and objects.
     * @param screenW The width of the screen in pixels
     * @param screenH The height of the screen in pixels
     */
    public Network(int screenW, int screenH){
        grid = new ScreenGrid((int) Math.round(Math.sqrt(screenW * screenH)) / GRID_DENSITY, screenW, screenH,
                GRID_EDGE_BUFFER);
        Station.initializeClass(grid, this);
        Train.initializeClass(grid);
        MetroLine.initializeClass(grid);
        TempMetroLine.initializeClass(grid);
        dijkstra = new Dijkstra(stations);
        stationPoissonDisc = new StationPoissonDisc(2.1, 3.5, stations, grid);
        Passenger.initializeClass(this, dijkstra);
        for(int i=0; i<INITIAL_STATIONS; i++){
            addStation();
        }
        score = new Score(grid);
    }

    /**
     * Adds a station to the network. The first station will be placed in the center
     * of the map, all others will spread out from the center with a rough poisson disc
     * distribuition.
     */
    private void addStation(){

        // use constructor that places station at center of map if there are no stations yet
        if(stations.size()==0){
            stations.add(new Station(grid));
            stationPoissonDisc.addActiveSample(stations.get(0)); // initializes the poisson disc algorithm with the first station       
        
        // Use poisson disc algorithm to generate stations
        }else if((generateStations) && (stations.size()>0)){
            newLocation = stationPoissonDisc.generateNewStationLocation();
            if(newLocation[0] != -1){
                stations.add(new Station(newLocation, grid));

            // if newLocation is {-1,-1}, that means the algorithm ran out of valid
            // locations to put stations, so stop generating stations after that
            }else{
                generateStations = false;
            }
        }
    }


    /**
     * Creates a new tempMetroLine that forms a new line and is not an extension of
     * an existing line, or display an alert that the maximum number of lines has been reached.
     * @param s the station that the tempMetroLine will start at
     * @return the new tempMetroLine created
     */
    public TempMetroLine newTempLine(Station s){
        return newTempLine(s,null);
    }

    /**
     * Creates a new tempMetroLine that either extends a line or creates a new line,
     * or display an alert that the maximum number of lines has been reached.
     * @param s the station that the tempMetroLine will start at
     * @param extension the MetroLine to be extended or null to indicate a completely new line
     * @return the tempMetroLine that was created, or null if the maximum number of lines was reached.
     */
    public TempMetroLine newTempLine(Station s, MetroLine extension){
        if(extension != null){
            tempLine = new TempMetroLine(s, extension);
            return tempLine;
        }else if(lines.size()<LINE_COLORS.length){
            tempLine = new TempMetroLine(LINE_COLORS[lines.size()], s);
            return tempLine;
        }else if(!gameOver){  // to prevent the alert from changing after the game is over
            alert = new Alert("Maximum number of lines reached", grid.getGridSize()*3/2, GameAreaPanel.FPS);
        }
        return null;
        
    }

    /**
     * Finalizes a tempMetroLine converting it into a new MetroLine or using it to
     * extend a MetroLine depending on what it was originally created for.
     * 
     * This method also attempts to find new paths for all passengers that could not
     * previously find a path to their destination, since the new line or extension
     * may make it possible.
     * 
     * This method also updates the adjacency lists of all stations along the
     * tempLine to reflect new changes.
     * 
     * If the tempMetroLine has only one station, it is discarded.
     */
    public void finalizeTempLine(){
        if(tempLine.hasMultipleStations()){
            updateStationConnectionsOnTempLine(tempLine);
            if(!tempLine.isExtendingLine()){
                lines.add(new MetroLine(tempLine));
            }else{
                tempLine.getExtendingLine().extendLine(tempLine);
            }

            // if any passenger has no path currently (because when they appeared the
            // journey was impossible), try again because there's a new line now
            for(Station station:stations){
                station.findPathsForPassengersWithNoPath();
            }

        }
        tempLine = null;
    }

    /**
     * Updates the graph adjacency lists of all stations along a tempMetroLine,
     * reflecting new connections made by the new line/extension
     * @param tempLine the tempMetroLine that is being turned into a new line/extension
     */
    private void updateStationConnectionsOnTempLine(TempMetroLine tempLine){
        // store in arraylist for faster random access
        ArrayList<Station> tempStns = new ArrayList<Station>();
        tempStns.addAll(tempLine.getStations());

        // go through all stations
        for(int i=0; i<tempStns.size(); i++){

            // for termini add a connection to the second or second-to last station
            if(i==0){
                tempStns.get(0).addConnection(tempStns.get(1));
            }else if(i==tempStns.size()-1){
                tempStns.get(tempStns.size()-1).addConnection(tempStns.get(tempStns.size()-2));

            // for all other stations add a conncetion to the stations before and after 
            }else{
                tempStns.get(i).addConnection(tempStns.get(i-1));
                tempStns.get(i).addConnection(tempStns.get(i+1));
            }
        }

    }

    /**
     * Gets an ArrayList of all lines terminating at a station
     * @param station the station in question
     * @return an ArrayList of all MetroLines terminating at the station
     */
    public ArrayList<MetroLine> getLinesTerminatingAtStation(Station station){
        ArrayList<MetroLine> linesTerminating = new ArrayList<MetroLine>();
        for(MetroLine line : lines){
            if ((station == line.getStations().getFirst()) || (station == line.getStations().getLast())) {
              linesTerminating.add(line);
            }
        }
        return linesTerminating;
    }

    /**
     * Gets the ArrayList of all stations on the map
     * @return the ArrayList of all stations on the map
     */
    public ArrayList<Station> getStations(){
        return stations;
    }

    /**
     * Gets the ArrayList of all MetroLines on the map
     * @return the ArrayList of all MetroLines on the map
     */
    public LinkedList<MetroLine> getLines(){
        return lines;
    }
    
    /**
     * Gets the game grid
     * @return the ScreenGrid object representing the game grid
     */
    public ScreenGrid getGrid(){
        return grid;
    }

    /**
     * Increases the score of the player by 1. Use when a train has delivered a
     * passenger to their destination.
     */
    public void incrementScore(){
        score.incrementScore();
    }

    /**
     * Updates the game state, or displays "Game Over" and the score if the player
     * has lost.
     */
    public void update(){
        if(!gameOver){
            updateGame();
        }else if(!gameOverDisplayed){
            alert = new Alert("Game Over. Score: " + score.getScore(), grid.getGridSize()*3);
            gameOverDisplayed = true;
        }
    }

    /**
     * Updates the game state. Generates stations and passengers and updates all
     * game objects.
     */
    public void updateGame(){

        // generate new stations randomly
        if(random.nextInt(100*GameAreaPanel.FPS)<stationGenerationChance){
            addStation();
        }

        // make stations more spread apart as more stations are generated
        if(stations.size()>30){
            stationPoissonDisc.updateStationSpacing(4, 7);
        }else if(stations.size()>20){
            stationPoissonDisc.updateStationSpacing(4, 5.5);
        }else if(stations.size()>9){
            stationPoissonDisc.updateStationSpacing(2.5, 3.5);
        }

        // increase the passenger generation rate once stations stop generating, so the
        // player eventually has to lose
        if (!generateStations) {
            passengerGenerationRate++;
        }

        for(Station station:stations){
            // generate passengers at stations
            station.generatePassengers(passengerGenerationRate);

            // player loses if a station is overcrowded
            if(station.isOvercrowded()){
                gameOver = true;
            }
        }

        // update all lines
        for(MetroLine line:lines){
            line.updateTrains();
        }

        // update the alert, let garbage collector delete it after it should no longer be displayed
        if(alert!=null){
            alert.update();
            if(alert.isOver()){
                alert = null;
            }
        }

    }

    /**
     * Draws all game objects on the screen.
     * @param g Graphics object to draw with
     */
    public void draw(Graphics g) {

        // draw all lines
        for(int i=0; i<lines.size(); i++){
            lines.get(i).draw(g,grid);
        }

        // draw temporary line
        if(tempLine != null){
            tempLine.draw(g, grid);
        }

        // draw all stations
        for(int i=0; i<stations.size(); i++){
            stations.get(i).draw(g, grid);
        }
        
        // draw score display
        score.draw(g, grid);

        // draw alert if there is one
        if(alert!=null){
            alert.draw(g, grid);
        }
        
    }



}
