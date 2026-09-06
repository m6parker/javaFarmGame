package src.entities;

import java.awt.Color;

public class Tile {
    private Color terrainColor;
    private final int temperature;
    private final int moisture;
    private final int nutrients;

    public Tile(Color terrainColor, int temperature, int moisture, int nutrients) {
        this.terrainColor = terrainColor;
        this.temperature = temperature;
        this.moisture = moisture;
        this.nutrients = nutrients;
    }

    public Color getTerrainColor() {
        return terrainColor;
    }

    public void setTerrainColor(Color terrainColor) {
        this.terrainColor = terrainColor;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getMoisture() {
        return moisture;
    }

    public int getNutrients() {
        return nutrients;
    }
}
