package src.entities.mobs;

import java.awt.Color;
import java.awt.Graphics2D;

import src.entities.Mob;
import src.managers.TileManager;
import src.Enums.Colors;

public class Dweller extends Mob {
    public Dweller(int startCol, int startRow, int tileSize, int maxCol, int maxRow,
            TileManager tileManager) {
        super(startCol, startRow, tileSize, "img/mobs/dweller.png");
    }

    @Override
    protected boolean canEnterTile(Color tileColor) {
        return tileColor.equals(TileManager.GRASS_COLOR)
                || tileColor.equals(TileManager.SOIL_COLOR)
                || tileColor.equals(TileManager.SAND_COLOR)
                || tileColor.equals(TileManager.STONE_COLOR);
    }

    @Override
    public void draw(Graphics2D graphics, int tileSize) {
        if (drawImage(graphics, tileSize)) {
            return;
        }
    }
}