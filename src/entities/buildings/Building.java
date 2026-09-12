package src.entities.buildings;

public class Building {
    private final int typeIndex;
    private final String name;
    private int level;

    public Building(int typeIndex, String name) {
        this.typeIndex = typeIndex;
        this.name = name;
        this.level = 1;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getSize() {
        return 1;
    }

    public void upgradeLevel() {
        if (level < 2) {
            level++;
        }
    }

    public void upgrade() {
        upgradeLevel();
    }
}