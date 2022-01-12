import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Dijkstra.java
 * An implementation of Dijkstra's algorithm for passenger path finding through the 
 * network of stations to get to a destination.
 * @author Suyu
 * @version 1.0
 */
public class Dijkstra {

    // Data for the algorithm
    private ArrayList<Station> stations; // ArrayList of all stations
    private ArrayList<Station> unsettledStations = new ArrayList<Station>(); // stores stations where the shortest path has not yet been found
    private HashMap<Station, Double> distancesToStart = new HashMap<Station, Double>(); // stores shorest distance to each station found thus far
    private HashMap<Station, Station> nextOnPathToStart = new HashMap<Station, Station>(); // stores the next station along the shortest path from each station to the start
    private Station closestUnsettledStation; // stores the station in unsettledStations with the least distance to the start
    private ArrayList<Station> fullPath = new ArrayList<Station>(); // stores the shortest path

    /**
     * Creates a new Dijstra object with the ArrayList of all stations in the network
     * @param stations an ArrayList of all stations in the network
     */
    Dijkstra(ArrayList<Station> stations){
        this.stations=stations;
    }

    /**
     * Implementation of Dijkstra's algorithm. Finds the shortest path between the
     * start and end stations.
     * 
     * @param start the starting station
     * @param end   the ending station
     * @return an ArrayList of stations, indicating the stations to travel along to
     *         get to the ending station for the shortest path
     */
    public ArrayList<Station> dijkstraPath(Station start, Station end){

        // Reset everything - clear variables that need to be reset, put all stations
        // in the unsettled stations list and make all distances the max value for ints
        // to indicate distance not found yet
        unsettledStations.clear();
        nextOnPathToStart.clear();
        closestUnsettledStation = null;
        fullPath.clear();
        for(Station station:stations){
            unsettledStations.add(station);
            distancesToStart.put(station, Double.MAX_VALUE);
        }

        // set distance to starting station as 0
        distancesToStart.replace(start, 0.0);

        // main loop - go through every station and find the shortest path until the
        // shortest path to the end is found (don't need to look through more stations
        // after path to end is found, save compute)
        while(unsettledStations.contains(end)){

            // set the closest unsettled station
            closestUnsettledStation = findClosestUnsettledStation();
            if(closestUnsettledStation == null){ // i.e. if there is no possible path
                return null;
            }

            // loop through all of the stations connected to the closest unsettled station
            for(Entry<Station, Double> entry:closestUnsettledStation.getConnections().entrySet()){

                // if the shortest path to the station has not been determined already, find the
                // length of the path to get to that station via the current closest unsettled station
                if(unsettledStations.contains(entry.getKey())){  
                    double distanceToStationViaCurrentStation = distancesToStart.get(closestUnsettledStation) + entry.getValue();
                    
                    // update the distance to the starting point and the next station along the
                    // shortest path to the starting point if the path via this station is shorter
                    if(distanceToStationViaCurrentStation < distancesToStart.get(entry.getKey())){
                        nextOnPathToStart.put(entry.getKey(), closestUnsettledStation);
                        distancesToStart.replace(entry.getKey(), distanceToStationViaCurrentStation);
                    }

                }
            }

            // Because the closest unsettled station is closer to the start than any other
            // unsettled station, the path found for it must be the shortest, so the path is
            // found
            unsettledStations.remove(closestUnsettledStation);

        }

        // method uses the data from nextOnPathToStart, starting from the end and going
        // backwards until it reaches the start to determine the path that was the
        // shortest
        return getFullPath(start, end);
        
    }

    /**
     * Uses the data in the nextOnPathToStart HashMap to generate the ArrayList of
     * stations that represents the shortest path from start to end
     * 
     * @param start the starting station
     * @param end   the ending station
     * @return an ArrayList of stations that represents the shortest path from start
     *         to end
     */
    private ArrayList<Station> getFullPath(Station start, Station end){
        fullPathRecursive(start, end);
        Collections.reverse(fullPath);
        return new ArrayList<Station>(fullPath);
    }

    /**
     * Recursive method used by the getFullPath() method to obtain the ArrayList of
     * stations of the shortest path
     * 
     * @param start the starting station
     * @param end   the ending station
     * @return an ArrayList of stations that represents the shortest path from start
     *         to end
     */
    private void fullPathRecursive(Station start, Station end){
        if(start == end){
            fullPath.add(start);
        }else{
            fullPath.add(end);
            fullPathRecursive(start, nextOnPathToStart.get(end));
        }
    }

    /**
     * Finds the station in the list of unsettled stations that has the minimum
     * distance to the start. If all stations in the unsettled stations list cannot
     * be reached from the start, null is returned instead.
     * 
     * @return the station in the list of unsettled stations that has the minimum
     *         distance to the start, or null if all stations in the unsettled list
     *         are not connected to the start
     */
    private Station findClosestUnsettledStation(){
        double min = Double.MAX_VALUE;
        Station output = null;
        for(Station station:unsettledStations){
            if(distancesToStart.get(station)<min){
                output = station;
            }
        }
        return output;
    }
}
