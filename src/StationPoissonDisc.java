import java.util.ArrayList;
import java.util.Random;

/**
 * StationPoissonDisc.java
 * A class for generating new station locations with Poisson Disc sampling with
 * a simplified implementation of Bridson's algorithm. Ensures that stations
 * generate in a random-looking pattern but also propogate outwards from the
 * starting point and are spread out evenly
 * 
 * @author Suyu
 * @version 1.0
 */
public class StationPoissonDisc {

    // Random
    private Random random = new Random();

    // information from the game
    private ScreenGrid grid;
    private ArrayList<Station> stations; // reference to list in Network

    // variables 
    private ArrayList<int[]> activeSamples = new ArrayList<int[]>(); // used for generation of new stations
    private double minStationSpacing;
    private double maxStationSpacing;
    private final int MAX_SAMPLE_ATTEMPTS = 5; // the maximum number of times the algorithm will look for points around 
                                               // any existing station before excluding the station    

    /**
     * Creates a new StationPoissonDisc object with an initial minimum and maximum
     * station spacing and information from the game
     * 
     * @param minSpacing the minimum spacing allowed between stations
     * @param maxSpacing the maximum spacing allowed between stations
     * @param stations the ArrayList of Station objects in the network
     * @param grid ScreenGrid object representing the game grid
     */
    public StationPoissonDisc(double minSpacing, double maxSpacing, ArrayList<Station> stations, ScreenGrid grid){
        this.minStationSpacing = minSpacing;
        this.maxStationSpacing = maxSpacing;
        this.stations = stations;
        this.grid = grid;
        for(Station station:stations){ // all initial stations are considered part of the active samples
            this.activeSamples.add(new int[]{station.getX(),station.getY()});
        }
    }

    /**
     * Adds a station to the active samples for poisson disk sampling. 
     * @param station the station to be added
     */
    public void addActiveSample(Station station){
        this.activeSamples.add(new int[]{station.getX(),station.getY()});
    }

    /**
     * Changes the minimum and maximum station spacing.
     * @param minSpacing the new minimum station spacing
     * @param maxSpacing the new maximum station spacing.
     */
    public void updateStationSpacing(double minSpacing, double maxSpacing){
        this.minStationSpacing = minSpacing;
        this.maxStationSpacing = maxSpacing;
    }

    /**
     * This method finds a location for placing a new station with Poisson disc
     * distribution using a simplified version of Bridson's algorithm for Poisson
     * disc sampling. The algorithm will ensure that newly generated stations appear
     * random while also being positioned not too close or too far away from
     * existing stations, as defined by the class variables minStationSpacing and
     * maxStationSpacing.

     * @return an integer array of length 2 containing the x and y coordinates of
     *         the new station. If no valid locations are found, the integer array
     *         will be {-1, -1}
     */
    public int[] generateNewStationLocation() {

        // Variables and constants
        int[] result = { -1, -1 };
        int selectedSampleIndex;
        int sampleAttempts;
        double distanceFromSelected;
        double angle;

        // keep repeating until a valid location is found or until all stations are excluded
        while ((result[0] == -1) && (activeSamples.size() > 0)) {

            // select a random station from the list of active samples (stations that have not been excluded)
            selectedSampleIndex = random.nextInt(activeSamples.size());

            // randomly sample points around the station chosen that are between the minimum
            // and maximum distances away from the station until either a valid location is found or until the
            // maximum number of attempts has been reached
            sampleAttempts = 1;
            while ((sampleAttempts <= MAX_SAMPLE_ATTEMPTS) && (result[0] == -1)) {

                // randomly sampling points that are in the appropriate region around the chosen station
                distanceFromSelected = Math.sqrt(random.nextDouble()) * (maxStationSpacing - minStationSpacing) + minStationSpacing;
                angle = random.nextDouble() * 2 * Math.PI;
                result[0] = (int) Math.round(activeSamples.get(selectedSampleIndex)[0] + Math.cos(angle) * distanceFromSelected);
                result[1] = (int) Math.round(activeSamples.get(selectedSampleIndex)[1] + Math.sin(angle) * distanceFromSelected);
               
                // checking if the new point is in the grid 
                if((result[0]<0)||(result[1]<0)||(result[0]>grid.getW())||(result[1]>grid.getH())){
                    result[0] = -1;
                    result[1] = -1;
                }else{

                    // checking if the new point is too close to any other stations
                    for (int i = 0; i < stations.size(); i++) {
                        if (Math.hypot(stations.get(i).getX() - result[0],
                                stations.get(i).getY() - result[1]) < minStationSpacing) {
                            result[0] = -1;
                            result[1] = -1;
                        }
                    }
                }
                // increment number of attempts
                sampleAttempts++;

            } // end of looping through random points around one station

            // if the maximum number of attempts for sampling points around this station was
            // reached, remove it from the list of active samples to prevent it from being
            // used again
            if(sampleAttempts>MAX_SAMPLE_ATTEMPTS){
                activeSamples.remove(selectedSampleIndex); 
            }

        } // end of looping through random stations
        
        // add the newly generated point to the list of points to be sampled in the future
        if(activeSamples.size() > 0){
            activeSamples.add(result);
        }        

        return result;
    }
}
