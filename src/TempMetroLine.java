import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;
import java.util.LinkedList;

/**
 * TempMetroLine.java
 * A class representing a metro line while it is being drawn by the user
 * @author Suyu
 * @version 1.0
 */
public class TempMetroLine implements DrawableGameObject{

    // Static variables
    private static BasicStroke lineStroke;
    private static ScreenGrid grid; // reference to game grid

    // variables
    private Color lineColor;
    private MetroLine extendingLine = null;
    private LinkedList<Station> stations = new LinkedList<Station>();
    private LinkedList<int[]> points = new LinkedList<int[]>();
    private Path2D.Float path = new Path2D.Float();
    private int[] lastIntermediatePoint = null;
    private int[] mousePoint = new int[2];
    private int[] mouseIntermediatePoint = new int[2];    
    private Path2D.Float pathToMouse = new Path2D.Float();

    /**
     * Creates a new TempMetroLine that is not extending and existing line, starting
     * at a specific station and with a specific color
     * @param color   the color of this TempMetroLine
     * @param station the station that this TempMetroLine starts at
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called yet
     */
    public TempMetroLine(Color color, Station station) throws ClassNotInitializedException{
        if(!isInitialized()){
            throw new ClassNotInitializedException();
        }
        lineColor = color;
        addRemoveStation(station);
    }

    /**
     * Creates a new TempMetroLine that is extending an existing line
     * @param station the station that this TempMetroLine starts at
     * @param extendingLine the MetroLine to be extended by this TempMetroLine
     * @throws ClassNotInitializedException if initializeClass() has not yet been
     *                                      called yet
     */
    public TempMetroLine(Station station, MetroLine extendingLine) throws ClassNotInitializedException{
        this(extendingLine.getColor(),station);
        this.extendingLine = extendingLine;
    }

    /**
     * Initializes class static variables with information from the game grid (screen 
     * size). Must be called before any TempMetroLine instances are created
     * @param grid the ScreenGrid object representing the game grid
     */
    public static void initializeClass(ScreenGrid grid){
        lineStroke = new BasicStroke((int) Math.round(grid.getGridSize() * 0.2), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND);
        TempMetroLine.grid = grid;
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
     * Returns whether or not this TempMetroLine has more than 1 station
     * @return true if this TempMetroLine has more than 1 station, false otherwise
     */
    public boolean hasMultipleStations(){
        if(stations.size()>1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Returns whether or not this TempMetroLine is extending an existing line
     * @return true if this TempMetroLine is extending an existing line, false otherwise
     */
    public boolean isExtendingLine(){
        if(extendingLine==null){
            return false;
        }
        return true;
    }

    /**
     * Gets the MetroLine that this TempMetroLine is extending
     * @return the MetroLine that this TempMetroLine is extending, null if this
     *         TempMetroLine is not extending an existing MetroLine
     */
    public MetroLine getExtendingLine(){
        return extendingLine;
    }

    /**
     * Gets a LinkedList of all stations on this TempMetroLine, in order
     * @return a LinkedList of all stations on this TempMetroLine, in order
     */
    public LinkedList<Station> getStations(){
        return stations;
    }

    /**
     * Gets the Path2D.Float object used to draw this TempMetroLine
     * @return the Path2D.Float object used to draw this TempMetroLine
     */
    public Path2D.Float getPath(){
        return path;
    }

    /**
     * Gets a LinkedList of all points on this TempMetroLine, in order
     * @return a LinkedList of all points on this , in order
     */
    public LinkedList<int[]> getPoints(){
        return points;
    }

    /**
     * Gets the color of this TempMetroLine
     * @return A Color object; the color of this TempMetroLine
     */
    public Color getColor(){
        return lineColor;
    }

    /**
     * Gets the BasicStroke object used to draw this TempMetroLine
     * @return A BasicStroke object; the stroke of this TempMetroLine
     */
    public static BasicStroke getStroke(){
        return lineStroke;
    }

    /**
     * Adds or removes a station from this TempMetroLine.
     * 
     * If the station is not already on the TempMetroLine or on the line it is
     * extending (if any), then the station will be added to the end of the
     * TempMetroLine.
     * 
     * If the station is the last station on the TempMetroLine, it will be removed.
     * 
     * If the station is already on the TempMetroLine or on the line it is extending
     * (if any), no action will be taken.
     * 
     * @param station the station to be added/removed from this TempMetroLine.
     */
    public void addRemoveStation(Station station){
        
        // remove station if there is more than 1 station and if the station is already on this TempMetroLine
        if((stations.size()>1)&&(station == stations.getLast())){
            stations.removeLast();

            // remove point at removed station and point leading to removed station, if there is one
            while (!((grid.gridXToScreen(stations.getLast().getX()) == points.getLast()[0])
                    && (grid.gridYToScreen(stations.getLast().getY()) == points.getLast()[1]))) {
                points.removeLast();
            }

            // redo the path to reflect change, using the points stored in the ArrayList
            path.reset();
            path = Path2DTools.makePathWithPoints(points);

        // don't do anything if this tempLine is extending an existing line and the user
        // attempts to connect the tempLine to a station already on the line being extended
        }else if((this.isExtendingLine())&&(extendingLine.getStations().contains(station))){
        
        // add station if it's not already on this TempMetroLine
        }else if(!stations.contains(station)){
            stations.add(station);

            // if this is the first station, start the path and add a point to the points arraylist
            if(stations.size()==1){
                path.moveTo(grid.gridXToScreen(station.getX()), grid.gridYToScreen(station.getY()));
                points.add(grid.gridXYToScreen(station.getXY()));
            
            // if not the first station, check if any intermediate points are needed to get
            // from the previous station to this one. Intermediate points are needed when
            // this station and the previous one don't fall on the same horizontal,
            // vertical, or 45 deg line.
            }else{
                lastIntermediatePoint = findIntermediatePoint((int) path.getCurrentPoint().getX(),
                        (int) path.getCurrentPoint().getY(), grid.gridXToScreen(station.getX()), grid.gridYToScreen(station.getY()));
                
                // add the points to the path and list of points
                if(lastIntermediatePoint!=null){
                    path.lineTo(lastIntermediatePoint[0], lastIntermediatePoint[1]);
                    points.add(lastIntermediatePoint);
                }
                path.lineTo(grid.gridXToScreen(station.getX()), grid.gridYToScreen(station.getY()));
                points.add(grid.gridXYToScreen(station.getXY()));
            }
        }
    }

    /**
     * Finds an intermediate point between two points such that the three points can
     * be connected by only horizontal, vertical, and 45 degree diagonal lines, with
     * the diagonal line starting from the first point.
     * 
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @return the intermediate point between the two points such that the three
     *         points can be connected by only horizontal, vertical, and 45 degree
     *         diagonal lines, with the diagonal line starting from the first point.
     *         If the two points are already horizontal, vertical, or directly 45
     *         degrees from each other, null will be returned instead.
     */
    private int[] findIntermediatePoint(int x1, int y1, int x2, int y2){
        // return null if both of the x or both of the y coordinates are the same (horizontal/vertical line)
        if((x1==x2)||(y1==y2)){
            return null;
        }else{
            int dx = x2-x1;
            int dy = y2-y1;
            int dxdyMin = Math.min(Math.abs(dx), Math.abs(dy));
            if(Math.abs(dx)-Math.abs(dy) == 0){ // i.e. if the line connecting the stations is 45 deg from horizontal
                return null;
            }

            // all other cases
            if((dx>0)&&(dy>0)){
                return new int[]{x1+dxdyMin, y1+dxdyMin};
            }else if((dx<0)&&(dy>0)){
                return new int[]{x1-dxdyMin, y1+dxdyMin};
            }else if((dx>0)&&(dy<0)){
                return new int[]{x1+dxdyMin, y1-dxdyMin};
            }else{
                return new int[]{x1-dxdyMin, y1-dxdyMin};
            }
        }
    }

    /**
     * Uses the mouse location to update this TempMetroLine to end at the mouse
     * cursor, using up to 1 intermediate point to ensure only 45 degree, vertical,
     * or horizontal lines are used,
     * 
     * @param mouseX the x coordinate of the mouse on the screen
     * @param mouseY the y coordinate of the mouse on the screen
     */
    public void updateMousePoint(int mouseX, int mouseY){
        mousePoint[0] = mouseX;
        mousePoint[1] = mouseY;

        // find the intermediate point needed to connect the last station on the
        // tempLine to the mouse with nice angled lines
        mouseIntermediatePoint = findIntermediatePoint((int) path.getCurrentPoint().getX(),
                (int) path.getCurrentPoint().getY(), mouseX, mouseY);

        // recreate the path to the mouse with the new points
        pathToMouse.reset();
        pathToMouse.moveTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY());
        if(mouseIntermediatePoint!=null){
            pathToMouse.lineTo(mouseIntermediatePoint[0], mouseIntermediatePoint[1]);
        }
        pathToMouse.lineTo(mouseX, mouseY);
    }

    /**
     * Draws this TempMetroLine on the screen
     * @param g Graphics object to draw with
     * @param grid ScreenGrid object of the game grid
     */
    public void draw(Graphics graphics, ScreenGrid grid) {
        Graphics2D g = (Graphics2D) graphics;
        g.setStroke(lineStroke);
        g.setColor(lineColor);
        g.draw(path);
        g.draw(pathToMouse);
    }

}
