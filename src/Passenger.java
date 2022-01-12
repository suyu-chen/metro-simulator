import java.util.Random;
import java.util.ArrayList;

/**
 * Passenger.java 
 * Class representing a passenger travelling on the network
 * @author Suyu
 * @version 1.0
 */
public class Passenger{

    // Static variables
    private static Network network;
    private static ArrayList<Station> stations;
    private static Dijkstra dijkstra;

    // variables
    private Station start;
    private Station destination;
    private ArrayList<Station> pathToDest;
    private int indexOnPath;

    // random
    private Random random = new Random();

    /**
     * Creates a new Passenger at a station, picks a random destination, and finds a
     * path between the two.
     * 
     * @param station the station where the Passenger appears
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called yet
     */
    public Passenger(Station station) throws ClassNotInitializedException{
        if(!isInitialized()){
            throw new ClassNotInitializedException();
        }
        start = station;
        destination = pickRandomDestination();
        findPath(); // Dijkstra's algorithm to find the path
        indexOnPath = 0;
    }

    /**
     * Selects a random station that is not the starting station 
     * @return the station selected as the destination
     */
    private Station pickRandomDestination(){
        if(stations.size()<=1){
            return null;
        }
        Station output = this.start;
        while(output == start){
            output = network.getStations().get(random.nextInt(network.getStations().size()));
        }
        return output;
    }    

    /**
     * Returns whether or not a path has been found between this Passenger's staring
     * and destination stations
     * @return true if a path has been found, false otherwise.
     */
    public boolean hasPath(){
        if(pathToDest == null){
            return false;
        }else{
            return true;
        }
    }

    /**
     * Uses Dijkstra's algorithm to find a path between this Passenger's staring
     * and destination stations
     */
    public void findPath(){
        pathToDest = dijkstra.dijkstraPath(start, destination);        
    }

    /**
     * Gets the next station on this Passenger's journey.
     * @return the next station on this Passenger's journey, or null if this
     *         Passenger has reached their destination station or if a path has not
     *         yet been found.
     */
    public Station getNextOnPath(){
        if(hasPath()){
            if(indexOnPath < pathToDest.size()-1){
                return pathToDest.get(indexOnPath + 1);
            }
        }
        return null;
        
    }

    /**
     * Updates the location of this Passenger, should be called when this
     * passenger reaches the next station on its path.
     */
    public void updateAtNextStation(){
        indexOnPath++;
    }

    /**
     * Initializes class static variables with information from the game network,
     * used for passenger path finding
     * @param network the Network object of the game
     */
    public static void initializeClass(Network network, Dijkstra dijkstra){
        Passenger.network = network;
        Passenger.stations = network.getStations();
        Passenger.dijkstra = dijkstra;
    }

    /**
     * Returns whether or not initializeClass() has been called yet
     * @return true if this class has been initialized, false otherwise
     */
    public static boolean isInitialized(){
        if(network == null){
            return false;
        }else{
            return true;
        }
    }
    
}
