package src.managers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorldManager {
    private static final Path CONFIG_PATH = Paths.get("config", "world.json");
    private static final Pattern NUMBER = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    private final Map<String, Double> values;

    private WorldManager(Map<String, Double> values) {
        this.values = values;
    }

    // load world config from json file
    public static WorldManager load() {
        try {
            String json = new String(Files.readAllBytes(CONFIG_PATH), StandardCharsets.UTF_8);
            Map<String, Double> values = new LinkedHashMap<>();
            Matcher matcher = NUMBER.matcher(json);
            while (matcher.find()) {
                values.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
            }
            WorldManager config = new WorldManager(values);
            config.requireAll();
            return config;
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not load " + CONFIG_PATH, exception);
        }
    }

    // ensure all setting are in config file
    private void requireAll() {
        String[] required = {"grass", "soil", "water", "stone", "sandAdjacentWater", "tree", "lilyPad", "fish", "sheep", "chicken", "moistureDistanceLimit", "temperatureMaximum", "nutrientsMaximum", "outOfRangeMoistureMaximum"};
        for (String key : required) {
            if (!values.containsKey(key)) {
                throw new IllegalArgumentException("Missing world setting: " + key);
            }
        }
    }

    public double tileProbability(String terrain) {
        return value(terrain);
    }

    public double sandAdjacentWaterProbability() {
        return value("sandAdjacentWater");
    }

    public int spawnCount(String mob) {
        return integerValue(mob);
    }

    public int moistureDistanceLimit() {
        return integerValue("moistureDistanceLimit");
    }

    public int temperatureMaximum() {
        return integerValue("temperatureMaximum");
    }

    public int nutrientsMaximum() {
        return integerValue("nutrientsMaximum");
    }

    public int outOfRangeMoistureMaximum() {
        return integerValue("outOfRangeMoistureMaximum");
    }

    public Map<String, Double> values() {
        return Collections.unmodifiableMap(values);
    }

    private double value(String key) {
        return values.get(key);
    }

    private int integerValue(String key) {
        return (int) Math.round(value(key));
    }
}
