package src.managers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import src.Enums.Colors;

public class FenceManager {
    public static final int FENCE_TOP = 0;
    public static final int FENCE_RIGHT = 1;
    public static final int FENCE_BOTTOM = 2;
    public static final int FENCE_LEFT = 3;

    private final int[][] fenceSides;

    public FenceManager(int rows, int cols) {
        fenceSides = new int[rows][cols];
    }

    public boolean placeFence(int row, int col, int side) {
        if (!isInBounds(row, col) || side < FENCE_TOP || side > FENCE_LEFT
                || hasFenceSide(row, col, side)) {
            return false;
        }
        fenceSides[row][col] |= 1 << side;

        int neighborRow = row;
        int neighborCol = col;
        int neighborSide = oppositeSide(side);
        if (side == FENCE_TOP) {
            neighborRow--;
        } else if (side == FENCE_RIGHT) {
            neighborCol++;
        } else if (side == FENCE_BOTTOM) {
            neighborRow++;
        } else {
            neighborCol--;
        }
        if (isInBounds(neighborRow, neighborCol)) {
            fenceSides[neighborRow][neighborCol] |= 1 << neighborSide;
        }
        return true;
    }

    public int removeFences(int row, int col) {
        if (!isInBounds(row, col)) {
            return 0;
        }
        int sides = fenceSides[row][col];
        if (sides == 0) {
            return 0;
        }
        for (int side = FENCE_TOP; side <= FENCE_LEFT; side++) {
            if ((sides & (1 << side)) != 0) {
                int neighborRow = row;
                int neighborCol = col;
                int neighborSide = oppositeSide(side);
                if (side == FENCE_TOP) {
                    neighborRow--;
                } else if (side == FENCE_RIGHT) {
                    neighborCol++;
                } else if (side == FENCE_BOTTOM) {
                    neighborRow++;
                } else {
                    neighborCol--;
                }
                if (isInBounds(neighborRow, neighborCol)) {
                    fenceSides[neighborRow][neighborCol] &= ~(1 << neighborSide);
                }
            }
        }
        fenceSides[row][col] = 0;
        return Integer.bitCount(sides);
    }

    public boolean hasFence(int row, int col) {
        return isInBounds(row, col) && fenceSides[row][col] != 0;
    }

    public boolean hasFenceSide(int row, int col, int side) {
        return isInBounds(row, col) && side >= FENCE_TOP && side <= FENCE_LEFT
                && (fenceSides[row][col] & (1 << side)) != 0;
    }

    public void draw(Graphics2D graphics, int row, int col, int x, int y,
            int tileSize, Color color) {
        int sides = fenceSides[row][col];
        for (int side = FENCE_TOP; side <= FENCE_LEFT; side++) {
            if ((sides & (1 << side)) != 0 && shouldDrawSide(row, col, side)) {
                drawSide(graphics, x, y, tileSize, side, color);
            }
        }
    }

    public void drawPreview(Graphics2D graphics, int x, int y, int tileSize, int side) {
        drawSide(graphics, x, y, tileSize, side, Colors.FENCE.lowOpacity(150));
    }

    private void drawSide(Graphics2D graphics, int x, int y, int tileSize,
            int side, Color color) {
        Stroke previousStroke = graphics.getStroke();
        Color previousColor = graphics.getColor();
        graphics.setColor(color);
        graphics.setStroke(new BasicStroke(Math.max(2f, tileSize / 8f)));
        if (side == FENCE_TOP) {
            graphics.drawLine(x, y, x + tileSize, y);
        } else if (side == FENCE_RIGHT) {
            graphics.drawLine(x + tileSize, y, x + tileSize, y + tileSize);
        } else if (side == FENCE_BOTTOM) {
            graphics.drawLine(x, y + tileSize, x + tileSize, y + tileSize);
        } else {
            graphics.drawLine(x, y, x, y + tileSize);
        }
        graphics.setStroke(previousStroke);
        graphics.setColor(previousColor);
    }

    private int oppositeSide(int side) {
        if (side == FENCE_TOP) {
            return FENCE_BOTTOM;
        }
        if (side == FENCE_RIGHT) {
            return FENCE_LEFT;
        }
        if (side == FENCE_BOTTOM) {
            return FENCE_TOP;
        }
        return FENCE_RIGHT;
    }

    private boolean shouldDrawSide(int row, int col, int side) {
        return side == FENCE_TOP || side == FENCE_LEFT
                || side == FENCE_BOTTOM && row == fenceSides.length - 1
                || side == FENCE_RIGHT && col == fenceSides[row].length - 1;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < fenceSides.length
                && col >= 0 && col < fenceSides[row].length;
    }
}
