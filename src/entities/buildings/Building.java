package src.entities.buildings;

public class Building {
    private final int typeIndex;
    private final String name;
    private int level;
    private int size;

    public Building(int typeIndex, String name) {
        this.typeIndex = typeIndex;
        this.name = name;
        this.level = 1;
        this.size = 1;
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
        return size;
    }

    public void upgradeLevel() {
        level++;
    }

    public void upgradeSize() {
        size++;
    }

    public void upgrade() {
        upgradeLevel();
        upgradeSize();
    }
}