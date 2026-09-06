package src;
import javax.swing.*;

import src.entities.Fish;
import src.entities.Mob;
import src.managers.BuildingManager;
import src.managers.CropManager;
import src.managers.TileManager;
import src.ui.menus.TileMenu;
import src.ui.panels.BuildingCountPanel;
import src.ui.panels.CropCountPanel;
import src.ui.panels.ModePanel;

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
    TileManager tileManager = new TileManager(maxScreenRow, maxScreenCol);
    BuildingManager buildingManager = new BuildingManager(maxScreenRow, maxScreenCol);
    CropManager cropManager = new CropManager(maxScreenRow, maxScreenCol);
    TileMenu tileMenu = new TileMenu(this, tileManager, buildingManager, cropManager);
    ModePanel modePanel;
    GameMode currentMode = GameMode.SELECT;
    int selectedCol = -1;
    int selectedRow = -1;

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
        spawnFish();
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

    public void spawnFish(){
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            int[] startTile = tileManager.findRandomTile(TileManager.WATER_COLOR, rand);
            if (startTile == null) {
                break;
            }
            movingComponents.add(new Fish(
                    startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileManager));
        }
    }

    public void update() {
        // update positions of mobs
        for (Mob component : movingComponents) {
            component.update(maxScreenCol, maxScreenRow, tileSize, tileManager);
        }
    }

    public void paintComponent(Graphics g) {
        // call the super to ensure proper painting
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // tile map
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                int x = col * tileSize;
                int y = row * tileSize;

                g2.setColor(tileManager.getColor(row, col));
                g2.fillRect(x, y, tileSize, tileSize);

                // draw buildings and crops
                buildingManager.draw(g2, row, col, x, y, tileSize);
                cropManager.draw(g2, row, col, x, y, tileSize);

                // tile borders
                g2.setColor(new Color(30, 30, 30));
                g2.drawRect(x, y, tileSize, tileSize);

                // highlight clicked tile
                if (col == selectedCol && row == selectedRow) {
                    g2.setColor(Color.YELLOW);
                    g2.drawRect(x, y, tileSize, tileSize);
                }
            }
        }

        // moving components
        for (Mob component : movingComponents) {
            component.draw(g2, tileSize);
        }

        g2.dispose();
    }

    // clicking tiles
    public void setMode(GameMode mode) {
        currentMode = mode;
        tileMenu.hideInformationBox();
    }

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
                tileManager.setColor(selectedRow, selectedCol,
                        modePanel.getSelectedTerrainColor());
                repaint();
            } else if (currentMode == GameMode.CONSTRUCTION) {
                tileMenu.placeBuilding(selectedCol, selectedRow,
                        modePanel.getSelectedBuildingIndex());
            } else if (currentMode == GameMode.CROP_PLANT) {
                tileMenu.placeCrop(selectedCol, selectedRow,
                        modePanel.getSelectedCropIndex());
            } else if (currentMode == GameMode.BULLDOZE) {
                tileMenu.bulldoze(selectedCol, selectedRow);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int hoveredCol = e.getX() / tileSize;
            int hoveredRow = e.getY() / tileSize;
            if (currentMode == GameMode.SELECT) {
                tileMenu.showInformationOnHover(e, hoveredCol, hoveredRow);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            tileMenu.hideInformationBox();
        }
    }

    public static void main(String[] args) {
        JFrame window = new JFrame("farming game name");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GameWindow gamePanel = new GameWindow();
        window.setLayout(new BorderLayout());
        window.add(gamePanel, BorderLayout.CENTER);
        JPanel inventoryPanel = new JPanel(new GridLayout(1, 2));
        inventoryPanel.add(new BuildingCountPanel(gamePanel.buildingManager, gamePanel.screenHeight));
        inventoryPanel.add(new CropCountPanel(gamePanel.cropManager, gamePanel.screenHeight));
        window.add(gamePanel.modePanel, BorderLayout.WEST);
        window.add(inventoryPanel, BorderLayout.EAST);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}