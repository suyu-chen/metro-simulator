import java.awt.geom.Path2D;
import java.util.LinkedList;

/**
 * A collection of methods to use with java.awt.geom.Path2D objects.
 * @author Suyu
 */
public class Path2DTools {

    /**
     * Creates a Path2D.Float object connecting a list of points with straight lines
     * @param points a LinkedList of length 2 int arrays representing the coordinates making up the path
     * @return the Path2D.Float object consisting of the points in the LinkedList joined by straight lines
     */
    public static Path2D.Float makePathWithPoints(LinkedList<int[]> points){
        Path2D.Float path = new Path2D.Float();
        path.moveTo(points.getFirst()[0], points.getFirst()[1]);
        for(int i=0; i<points.size(); i++){
            path.lineTo(points.get(i)[0], points.get(i)[1]);
        }
        return path;
    }
}
