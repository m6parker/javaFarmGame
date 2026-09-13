package src;
import java.awt.Color;

public final class Enums {
    private Enums() {
        // prevent instantiation
    }

    public enum GameModes {
        SELECT,
        TERRAIN_PAINT,
        CONSTRUCTION,
        CROP_PLANT,
        BULLDOZE,
        HARVEST
    }

    // used to identify tile terrain types
    public enum TerrainType {
        GRASS(76, 175, 80),
        WATER(66, 135, 245),
        LAVA(220, 70, 70),
        SAND(245, 205, 60),
        SOIL(145, 95, 55),
        STONE(150, 150, 150);

        private final Color color;

        TerrainType(int red, int green, int blue) {
            color = new Color(red, green, blue);
        }

        public Color color() {
            return color;
        }
    }

    public enum Colors {
        FENCE(64, 16, 45),
        VALID_PREVIEW(50, 210, 80),
        INVALID_PREVIEW(220, 50, 50),
        TILE_BORDER(30, 30, 30);

        private final Color color;

        Colors(int red, int green, int blue) {
            color = new Color(red, green, blue);
        }

        public Color color() {
            return color;
        }

        // returns same color with transparancy for hover preview
        public Color lowOpacity(int alpha) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        }
    }
}