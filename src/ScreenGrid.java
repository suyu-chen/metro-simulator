/**
 * A class representing a virtual grid of points on a screen, which can be used
 * to position elements on the screen
 */
public class ScreenGrid {

    // class variables
    private int gridSize;
    private int gridW, gridH; // in units of gridSize
    private int gridTopLeftX, gridTopLeftY; // these are in screen space
    private int screenW, screenH; // in px

    /**
     * Creates a new ScreenGrid object with a specified grid size, screen size, and 
     * buffer around the edge of the screen
     * @param gridSize the size of the grid in pixels
     * @param screenW the width of the screen in pixels
     * @param screenH the height of the screen in pixels
     * @param edgeBuffer the number of pixels between the edge of the grid to the edge of the screen
     */
    public ScreenGrid(int gridSize, int screenW, int screenH, int edgeBuffer) {
        this.gridSize = gridSize;
        this.screenW = screenW;
        this.screenH = screenH;
        this.gridW = (int) Math.round(screenW*1.0 / gridSize - edgeBuffer*2);
        this.gridH = (int) Math.round(screenH*1.0 / gridSize - edgeBuffer*2);
        this.gridTopLeftX = (screenW - gridW * gridSize) / 2; // coordinates on the screen
        this.gridTopLeftY = (screenH - gridH * gridSize) / 2; 
    }

    /**
     * Gets the width of this grid
     * @return the number of columns in this grid
     */
    public int getW(){
        return gridW;
    }

    /**
     * Gets the height of this grid
     * @return the number of rows in this grid
     */
    public int getH(){
        return gridH;
    }

    /**
     * Gets the gridsize of this grid
     * @return the number of pixels between points on this grid
     */
    public int getGridSize(){
        return gridSize;
    }

    /**
     * Gets the width of the screen this grid is on
     * @return the width of the screen this grid is on, in pixels
     */
    public int getScreenW(){
        return screenW;
    }

    /**
     * Gets the height of the screen this grid is on
     * @return the height of the screen this grid is on, in pixels
     */
    public int getScreenH(){
        return screenH;
    }

    /**
     * Converts an x coordinate from grid coordinates to screen coordinates
     * @param x the x coordinate on the grid
     * @return the correspoding x coordinate on the screen
     */
    public int gridXToScreen(int x) {
        return gridTopLeftX + gridSize * x;
    }

    /**
     * Converts a y coordinate from grid coordinates to screen coordinates
     * @param y the y coordinate on the grid
     * @return the correspoding y coordinate on the screen
     */
    public int gridYToScreen(int y) {
        return gridTopLeftY + gridSize * y;
    }

    /**
     * Converts an xy coordinate pair from grid coordinates to screen coordinates
     * @param xy the coordinate on the grid in the form of a length 2 int array
     * @return the corresponding coordinate on the screen in the form of a length 2 int array
     */
    public int[] gridXYToScreen(int[] xy) {
        return new int[]{gridXToScreen(xy[0]), gridYToScreen(xy[1])};
    }

}