import javax.swing.*;
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
    Color[][] tileColors = new Color[maxScreenRow][maxScreenCol];
    BuildingManager buildingManager = new BuildingManager(maxScreenRow, maxScreenCol);
    CropManager cropManager = new CropManager(maxScreenRow, maxScreenCol);
    TileMenu tileMenu = new TileMenu(this, tileColors, buildingManager, cropManager);
    int selectedCol = -1;
    int selectedRow = -1;

    // constructor
    public GameWindow() {
        // setup window size / background color
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addMouseListener(mouseH);
        this.setFocusable(true);
        initializeTileColors();
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
            int[] startTile = findRandomBlueTile(rand);
            if (startTile == null) {
                break;
            }
            movingComponents.add(new Fish(
                    startTile[0], startTile[1], tileSize,
                    maxScreenCol, maxScreenRow, tileColors));
        }
    }

    public void update() {
        // update positions of mobs
        for (Mob component : movingComponents) {
            component.update(maxScreenCol, maxScreenRow, tileSize, tileColors);
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

                g2.setColor(tileColors[row][col]);
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

    //set the color of the terrain
    private void initializeTileColors() {
        Color green = new Color(76, 175, 80);
        Color brown = new Color(145, 95, 55);
        Color blue = new Color(66, 135, 245);
        Color grey = new Color(150, 150, 150);
        Color yellow = new Color(245, 205, 60);
        Random random = new Random();

        // set color probablilities - mostly green, with some brown and blue, grey least often
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                int colorRoll = random.nextInt(20);
                tileColors[row][col] = colorRoll < 12 ? green
                    : colorRoll < 17 ? brown
                    : colorRoll < 19 ? blue
                    : grey;
            }
        }

        // check for tiles next to blue tiles to add sand near water
        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (tileColors[row][col] != blue
                        && isNextToColor(row, col, blue)
                        && random.nextInt(4) == 0) {
                    tileColors[row][col] = yellow;
                }
            }
        }
    }

    // utility function for checking if a tile is next to a specific color
    private boolean isNextToColor(int row, int col, Color color) {
        return (row > 0 && tileColors[row - 1][col] == color)
                || (row < maxScreenRow - 1 && tileColors[row + 1][col] == color)
                || (col > 0 && tileColors[row][col - 1] == color)
                || (col < maxScreenCol - 1 && tileColors[row][col + 1] == color);
    }

    private boolean isBlueTile(int row, int col) {
        return tileColors[row][col].equals(new Color(66, 135, 245));
    }

    private int[] findRandomBlueTile(Random random) {
        for (int attempt = 0; attempt < maxScreenCol * maxScreenRow; attempt++) {
            int col = random.nextInt(maxScreenCol);
            int row = random.nextInt(maxScreenRow);
            if (isBlueTile(row, col)) {
                return new int[]{col, row};
            }
        }

        for (int row = 0; row < maxScreenRow; row++) {
            for (int col = 0; col < maxScreenCol; col++) {
                if (isBlueTile(row, col)) {
                    return new int[]{col, row};
                }
            }
        }
        return null;
    }

    // clicking tiles
    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            // Convert pixel coordinates to tile grid coordinates
            selectedCol = mouseX / tileSize;
            selectedRow = mouseY / tileSize;
            tileMenu.show(e, selectedCol, selectedRow);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int hoveredCol = e.getX() / tileSize;
            int hoveredRow = e.getY() / tileSize;
            tileMenu.showInformationOnHover(e, hoveredCol, hoveredRow);
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
        inventoryPanel.add(gamePanel.buildingManager.createPanel(gamePanel.screenHeight));
        inventoryPanel.add(gamePanel.cropManager.createPanel(gamePanel.screenHeight));
        window.add(inventoryPanel, BorderLayout.EAST);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}