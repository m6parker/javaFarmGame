package src.entities;

import java.awt.Color;

public class Tile {
    private Color terrainColor;

    public Tile(Color terrainColor) {
        this.terrainColor = terrainColor;
    }

    public Color getTerrainColor() {
        return terrainColor;
    }

    public void setTerrainColor(Color terrainColor) {
        this.terrainColor = terrainColor;
    }
}
