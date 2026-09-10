package src;
import javax.swing.*;

import src.entities.LilyPad;
import src.entities.mobs.Chicken;
import src.entities.mobs.Dweller;
import src.entities.mobs.Fish;
import src.entities.mobs.Sheep;
import src.entities.Mob;
import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;
import src.ui.menus.TileMenu;
import src.ui.panels.BuildingCountPanel;
import src.ui.panels.CropCountPanel;
import src.ui.panels.ModePanel;
import src.ui.panels.MobCountPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameWindow extends JPanel implements Runnable {

    // screen settings
    final int originalTileSize = 16; // 16x16 base tile
    final int scale = 3;
    final int tileSize = originalTileSize * scale; // 48x48 pixel tiles
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = tileSize * maxScreenCol; // 768 pixels
    final int screenHeight = tileSize * maxScreenRow; // 576 pixels

    int FPS = 60;
    Thread gameThread;
    MouseHandler mouseH = new MouseHandler();

    // setup entities / grid state
    List<Mob> movingComponents = new ArrayList<>();
    List<Dweller> dwellers = new ArrayList<>();
    List<LilyPad> lilyPads = new ArrayList<>();
    TileManager tileManager = new TileManager(maxScreenRow, maxScreenCol);
    BuildingManager buildingManager = new BuildingManager(maxScreenRow, maxScreenCol);
    CropManager cropManager = new CropManager(maxScreenRow, maxScreenCol);
    TileMenu tileMenu = new TileMenu(this, tileManager, buildingManager, cropManager, lilyPads);
    ModePanel modePanel;
    GameMode currentMode = GameMode.SELECT;
    int selectedCol = -1;
    int selectedRow = -1;
    int hoveredCol = -1;
    int hoveredRow = -1;
    int hoveredFenceSide = BuildingManager.FENCE_BOTTOM;
    MobCountPanel mobCountPanel;

    // constructor
    public GameWindow() {
        // setup window size / background color
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addMouseListener(mouseH);
        this.addMouseMotionListener(mouseH);
        this.setFocusable(true);
        modePanel = new ModePanel(screenHeight, buildingManager, cropManager, this::setMode);

        // setup world state
        plantTrees();
        spawnLilyPads();
        spawnFish(5);
        spawnSheep(5);
        spawnChicken(5);
    }

    private void plantTrees() {
        Random random = new Random();
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (tileManager.isGrassTile(row, col) && random.nextBoolean()) {
                    cropManager.plantMatureCrop(row, col, 1);
                }
            }
        }
    }

    private void spawnLilyPads() {
        Random random = new Random();
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                boolean available = !cropManager.hasCrop(row, col)
                        && !buildingManager.hasBuilding(row, col);
                if (available && tileManager.isWaterTile(row, col) && random.nextBoolean()) {
                    lilyPads.add(new LilyPad(row, col, random.nextInt(2)));
                }
            }
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // game loop
        double drawInterval = 1000000000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            // calculate time since last frame
            // update game state and repaint if enough time has passed
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void spawnFish(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int[] startTile = findRandomAvailableTile(TileManager.WATER_COLOR, rand);
            if (startTile == null) {
                break;
            }
            movingComponents.add(new Fish(
                    startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileManager));
        }
    }

    public void spawnSheep(int count){
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int[] startTile = findRandomAvailableTile(TileManager.GRASS_COLOR, rand);
            if (startTile == null) {
                break;
            }
            movingComponents.add(new Sheep(
                    startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileManager));
        }
    }

    public void spawnChicken(int count){
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int[] startTile = findRandomAvailableTile(TileManager.GRASS_COLOR, rand);
            if (startTile == null) {
                break;
            }
            movingComponents.add(new Chicken(
                    startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileManager));
        }
    }


    private int[] findRandomAvailableTile(Color terrainColor, Random random) {
        int maxAttempts = maxScreenCol * maxScreenRow;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int[] tile = tileManager.findRandomTile(terrainColor, random);
            if (tile != null && !buildingManager.hasBuilding(tile[1], tile[0])
                    && !cropManager.hasCrop(tile[1], tile[0])) {
                return tile;
            }
        }
        return null;
    }

    public void update() {
        // update positions of mobs
        cropManager.update(tileManager);
        updateDwellers();
        if (mobCountPanel != null) {
            mobCountPanel.refreshCounts();
        }
        for (Mob component : movingComponents) {
            component.update(maxScreenCol, maxScreenRow, tileSize, tileManager,
                    buildingManager, cropManager);
        }
    }

    private void updateDwellers() {
        int targetCount = buildingManager.getDwellerCount();
        while (dwellers.size() < targetCount) {
            Random random = new Random();
            int[] startTile = findRandomAvailableTile(TileManager.GRASS_COLOR, random);
            if (startTile == null) {
                return;
            }
            Dweller dweller = new Dweller(startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileManager);
            dwellers.add(dweller);
            movingComponents.add(dweller);
        }
        while (dwellers.size() > targetCount) {
            Dweller dweller = dwellers.remove(dwellers.size() - 1);
            movingComponents.remove(dweller);
        }
    }

    public void paintComponent(Graphics g) {
        // call the super to ensure proper painting
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // draw terrain
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                g2.setColor(tileManager.getColor(row, col));
                g2.fillRect(x, y, tileSize, tileSize);
            }
        }

        // draw fish below lily pads
        for (Mob component : movingComponents) {
            if (component instanceof Fish) {
                component.draw(g2, tileSize);
            }
        }

        // draw lily pads, crops, and buildings
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                for (LilyPad lilyPad : lilyPads) {
                    if (lilyPad.isAt(row, col)) {
                        lilyPad.draw(g2, tileSize);
                    }
                }

                // draw crops and buildings
                cropManager.draw(g2, row, col, x, y, tileSize);
                buildingManager.draw(g2, row, col, x, y, tileSize);

                if (currentMode == GameMode.CONSTRUCTION
                    && modePanel.getSelectedBuildingIndex() == BuildingManager.FENCE_INDEX
                    && col == hoveredCol && row == hoveredRow
                        && tileMenu.canPlaceBuilding(col, row,
                            modePanel.getSelectedBuildingIndex())) {
                    buildingManager.drawFencePreview(g2, row, col, x, y, tileSize,
                        hoveredFenceSide);
                }

                // tile borders
                // g2.setColor(new Color(30, 30, 30));
                // g2.drawRect(x, y, tileSize, tileSize);

                // highlight clicked tile
                if (col == selectedCol && row == selectedRow) {
                    g2.setColor(Color.YELLOW);
                    g2.drawRect(x, y, tileSize, tileSize);
                }
            }
        }

        // moving components
        for (Mob component : movingComponents) {
            if (!(component instanceof Fish)) {
                component.draw(g2, tileSize);
            }
        }

        g2.dispose();
    }

    // clicking tiles
    public void setMode(GameMode mode) {
        currentMode = mode;
        hoveredCol = -1;
        hoveredRow = -1;
        tileMenu.hideInformationBox();
        repaint();
    }

    // handler for clicking and hovering over tiles
    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            // Convert pixel coordinates to tile grid coordinates
            selectedCol = mouseX / tileSize;
            selectedRow = mouseY / tileSize;
            if (currentMode == GameMode.SELECT) {
                tileMenu.showInformation(e, selectedCol, selectedRow);
            } else if (currentMode == GameMode.TERRAIN_PAINT) {
                tileMenu.paintTerrain(selectedCol, selectedRow,
                        modePanel.getSelectedTerrainColor());
            } else if (currentMode == GameMode.CONSTRUCTION) {
                tileMenu.placeBuilding(selectedCol, selectedRow,
                        modePanel.getSelectedBuildingIndex(), getNearestTileSide(mouseX, mouseY));
            } else if (currentMode == GameMode.CROP_PLANT) {
                tileMenu.placeCrop(selectedCol, selectedRow,
                        modePanel.getSelectedCropIndex());
            } else if (currentMode == GameMode.BULLDOZE) {
                tileMenu.bulldoze(selectedCol, selectedRow);
            } else if (currentMode == GameMode.HARVEST) {
                tileMenu.harvest(selectedCol, selectedRow);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (currentMode == GameMode.CONSTRUCTION) {
                hoveredCol = e.getX() / tileSize;
                hoveredRow = e.getY() / tileSize;
                hoveredFenceSide = getNearestTileSide(e.getX(), e.getY());
            } else {
                hoveredCol = -1;
                hoveredRow = -1;
            }
            repaint();
        }

        private int getNearestTileSide(int mouseX, int mouseY) {
            int localX = mouseX % tileSize;
            int localY = mouseY % tileSize;
            int distanceToLeft = localX;
            int distanceToRight = tileSize - localX;
            int distanceToTop = localY;
            int distanceToBottom = tileSize - localY;
            int nearestDistance = Math.min(Math.min(distanceToLeft, distanceToRight),
                    Math.min(distanceToTop, distanceToBottom));

            if (nearestDistance == distanceToTop) {
                return BuildingManager.FENCE_TOP;
            }
            if (nearestDistance == distanceToRight) {
                return BuildingManager.FENCE_RIGHT;
            }
            if (nearestDistance == distanceToBottom) {
                return BuildingManager.FENCE_BOTTOM;
            }
            return BuildingManager.FENCE_LEFT;
        }

        // @Override
        // public void mouseMoved(MouseEvent e) {
        //     int hoveredCol = e.getX() / tileSize;
        //     int hoveredRow = e.getY() / tileSize;
        //     if (currentMode == GameMode.SELECT) {
        //         tileMenu.showInformation(e, hoveredCol, hoveredRow);
        //     }
        // }

        @Override
        public void mouseExited(MouseEvent e) {
            hoveredCol = -1;
            hoveredRow = -1;
            tileMenu.hideInformationBox();
            repaint();
        }
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("farming game name");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GameWindow gamePanel = new GameWindow();
        window.setLayout(new BorderLayout());
        window.add(gamePanel, BorderLayout.CENTER);

        JPanel inventoryPanel = new JPanel(new GridLayout(3, 1));
        int inventoryHeight = gamePanel.screenHeight / 3;
        inventoryPanel.add(new BuildingCountPanel(gamePanel.buildingManager, inventoryHeight));
        inventoryPanel.add(new CropCountPanel(gamePanel.cropManager, inventoryHeight));
        gamePanel.mobCountPanel = new MobCountPanel(gamePanel.movingComponents, inventoryHeight);
        inventoryPanel.add(gamePanel.mobCountPanel);
        
        window.add(gamePanel.modePanel, BorderLayout.WEST);
        window.add(inventoryPanel, BorderLayout.EAST);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}