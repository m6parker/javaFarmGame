package src.entities;
import java.awt.Color;
import java.util.Random;
import src.managers.TileManager;

public abstract class Mob {
    protected double x;
    protected double y;
    protected final double speed = 1.5;
    private double velocityX;
    private double velocityY;
    private int directionChangeCounter;
    private int nextDirectionChange;
    private final Random random = new Random();

    // constructor
    protected Mob(int startCol, int startRow, int tileSize) {
        double tileCenter = tileSize / 2.0;
        this.x = startCol * tileSize + tileCenter;
        this.y = startRow * tileSize + tileCenter;
        chooseRandomDirection();
    }

    // update position based on velocity
    // check for collisions and change direction if needed
    public void update(int maxCol, int maxRow, int tileSize, TileManager tileManager) {
        int currentCol = (int) (x / tileSize);
        int currentRow = (int) (y / tileSize);
        if (!isValidTile(tileManager, currentRow, currentCol)) {
            return;
        }

        // have the mob change direction after a certain number of updates
        directionChangeCounter++;
        if (directionChangeCounter >= nextDirectionChange) {
            chooseRandomDirection();
        }

        // calculate the next position based on velocity
        double radius = getRadius();
        double nextX = x + velocityX;
        double nextY = y + velocityY;
        int nextCol = (int) (nextX / tileSize);
        int nextRow = (int) (nextY / tileSize);

        // check for collisions with the edges of the grid
        if (nextCol != currentCol && !isValidTile(tileManager, currentRow, nextCol)) {
            velocityX = -velocityX;
            x = velocityX > 0
                    ? currentCol * tileSize + radius
                    : (currentCol + 1) * tileSize - radius;
        } else {
            x = nextX;
        }

        currentCol = (int) (x / tileSize);
        if (nextRow != currentRow && !isValidTile(tileManager, nextRow, currentCol)) {
            velocityY = -velocityY;
            y = velocityY > 0
                    ? currentRow * tileSize + radius
                    : (currentRow + 1) * tileSize - radius;
        } else {
            y = nextY;
        }
    }

    protected double getRadius() {
        return 5;
    }

    protected boolean canEnterTile(Color tileColor) {
        return true;
    }

    private boolean isValidTile(TileManager tileManager, int row, int col) {
        return row >= 0
            && row < tileManager.getRowCount()
            && col >= 0
            && col < tileManager.getColumnCount()
            && canEnterTile(tileManager.getColor(row, col));
    }

    // set a random direction for the mob to move in
    private void chooseRandomDirection() {
        double angle = random.nextDouble() * Math.PI * 2;
        velocityX = Math.cos(angle) * speed;
        velocityY = Math.sin(angle) * speed;
        directionChangeCounter = 0;
        nextDirectionChange = 45 + random.nextInt(76);
    }

    // draw mob on the screen
    public abstract void draw(java.awt.Graphics2D graphics, int tileSize);
}
