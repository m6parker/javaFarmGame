package src.entities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;
import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;

public abstract class Mob {
    private static final double MOVEMENT_SPEED = 1.0;
    private static final double TILE_CENTER_DIVISOR = 2.0;
    private static final double MOB_RADIUS = 5;
    private static final int MIN_DIRECTION_CHANGE_TICKS = 45;
    private static final int DIRECTION_CHANGE_TICK_RANGE = 76;
    private static final int MIN_PAUSE_TICKS = 10;
    private static final int PAUSE_TICK_RANGE = 61;
    private static final int MIN_NEXT_PAUSE_TICKS = 100;
    private static final int NEXT_PAUSE_TICK_RANGE = 241;
    private static final int INITIAL_NEXT_PAUSE_TICK_RANGE = 200;

    protected double x;
    protected double y;
    protected final double speed = MOVEMENT_SPEED;
    private double velocityX;
    private double velocityY;
    private int directionChangeCounter;
    private int nextDirectionChange;
    private int pauseCounter;
    private int movementCounter;
    private int nextPause;
    private final Random random = new Random();
    private final BufferedImage image;
    private final int tileSize;

    // constructor
    protected Mob(int startCol, int startRow, int tileSize) {
        this(startCol, startRow, tileSize, null);
    }

    protected Mob(int startCol, int startRow, int tileSize, String imagePath) {
        this.tileSize = tileSize;
        double tileCenter = tileSize / TILE_CENTER_DIVISOR;
        this.x = startCol * tileSize + tileCenter;
        this.y = startRow * tileSize + tileCenter;
        this.image = loadImage(imagePath);
        chooseRandomDirection();
    }

    protected boolean drawImage(Graphics2D graphics, int tileSize) {
        if (image == null) {
            return false;
        }
        Image scaledImage = image.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
        graphics.drawImage(scaledImage, (int) x - tileSize / 2, (int) y - tileSize / 2, null);
        return true;
    }

    public boolean occupiesTile(int row, int col) {
        return (int) (y / tileSize) == row && (int) (x / tileSize) == col;
    }

    private BufferedImage loadImage(String imagePath) {
        if (imagePath == null) {
            return null;
        }
        try {
            return ImageIO.read(new File(imagePath));
        } catch (IOException exception) {
            System.err.println("Could not load " + imagePath + ": "
                    + exception.getMessage());
            return null;
        }
    }

    // update position based on velocity
    // check for collisions and change direction if needed
    public void update(int maxCol, int maxRow, int tileSize, TileManager tileManager,
            BuildingManager buildingManager, CropManager cropManager) {
        int currentCol = (int) (x / tileSize);
        int currentRow = (int) (y / tileSize);
        if (!isValidTile(tileManager, buildingManager, cropManager, currentRow, currentCol)) {
            return;
        }

        // occationally stop moving for a random amount of time
        if (canPause()) {
            if (pauseCounter > 0) {
                pauseCounter--;
                return;
            }
            movementCounter++;
            if (movementCounter >= nextPause) {
                movementCounter = 0;
                pauseCounter = MIN_PAUSE_TICKS + random.nextInt(PAUSE_TICK_RANGE);
                nextPause = MIN_NEXT_PAUSE_TICKS + random.nextInt(NEXT_PAUSE_TICK_RANGE);
                return;
            }
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
        boolean blockedHorizontally = nextCol != currentCol
            && (!isValidTile(tileManager, buildingManager, cropManager, currentRow, nextCol)
                    || hasFenceBetween(buildingManager, currentRow, currentCol,
                        nextCol > currentCol, true));
        if (blockedHorizontally) {
            velocityX = -velocityX;
            x = velocityX > 0
                    ? currentCol * tileSize + radius
                    : (currentCol + 1) * tileSize - radius;
        } else {
            x = nextX;
        }

        currentCol = (int) (x / tileSize);
        boolean blockedVertically = nextRow != currentRow
            && (!isValidTile(tileManager, buildingManager, cropManager, nextRow, currentCol)
                    || hasFenceBetween(buildingManager, currentRow, currentCol,
                        nextRow > currentRow, false));
        if (blockedVertically) {
            velocityY = -velocityY;
            y = velocityY > 0
                    ? currentRow * tileSize + radius
                    : (currentRow + 1) * tileSize - radius;
        } else {
            y = nextY;
        }
    }

    protected double getRadius() {
        return MOB_RADIUS;
    }

    protected boolean canEnterTile(Color tileColor) {
        return true;
    }

    protected boolean canPause() {
        return true;
    }

    private boolean isValidTile(TileManager tileManager, BuildingManager buildingManager,
            CropManager cropManager, int row, int col) {
        return row >= 0
            && row < tileManager.getRowCount()
            && col >= 0
            && col < tileManager.getColumnCount()
            && !buildingManager.hasBuilding(row, col)
            && !cropManager.hasCrop(row, col)
            && canEnterTile(tileManager.getColor(row, col));
    }

    // check if there is a fence between the current tile and the next tile in the direction of movement
    private boolean hasFenceBetween(BuildingManager buildingManager, int row, int col,
            boolean movingPositive, boolean horizontal) {
        int side;
        if (horizontal) {
            side = movingPositive ? BuildingManager.FENCE_RIGHT : BuildingManager.FENCE_LEFT;
        } else {
            side = movingPositive ? BuildingManager.FENCE_BOTTOM : BuildingManager.FENCE_TOP;
        }
        return buildingManager.hasFenceSide(row, col, side);
    }

    // set a random direction for the mob to move in
    private void chooseRandomDirection() {
        double angle = random.nextDouble() * Math.PI * 2;
        velocityX = Math.cos(angle) * speed;
        velocityY = Math.sin(angle) * speed;
        directionChangeCounter = 0;
        nextDirectionChange = MIN_DIRECTION_CHANGE_TICKS
            + random.nextInt(DIRECTION_CHANGE_TICK_RANGE);
        // set a random amount of time before the mob can pause again
        nextPause = MIN_NEXT_PAUSE_TICKS + random.nextInt(INITIAL_NEXT_PAUSE_TICK_RANGE);
    }

    // draw mob on the screen
    public abstract void draw(java.awt.Graphics2D graphics, int tileSize);
}
