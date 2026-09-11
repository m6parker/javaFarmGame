package src.entities.mobs;
import java.awt.Color;
import java.awt.Graphics2D;

import src.entities.Mob;
import src.managers.TileManager;

public class Fish extends Mob {
    public Fish(int startCol, int startRow, int tileSize, int maxCol, int maxRow,
            TileManager tileManager) {
        super(startCol, startRow, tileSize, "img/mobs/fish.png");
    }

    @Override
    // Override the canEnterTile method to allow movement only on blue tiles
    protected boolean canEnterTile(Color tileColor) {
        return tileColor.equals(new Color(66, 135, 245));
    }

    @Override
    protected boolean canPause() {
        return false;
    }

    @Override
    public void draw(Graphics2D graphics, int tileSize) {
        if (drawImage(graphics, tileSize)) {
            return;
        }
        graphics.setColor(Color.ORANGE);
        int size = 10;
        graphics.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        graphics.setColor(Color.RED);
        graphics.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
    }
}
