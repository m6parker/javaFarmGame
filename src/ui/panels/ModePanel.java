package src.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import src.managers.TileManager;
import src.Enums.GameModes;
import src.managers.BuildingManager;
import src.managers.CropManager;

public class ModePanel extends JPanel {
    private static final String[] TERRAIN_NAMES = {
        "grass", "water", "lava", "sand", "soil", "stone"
    };
    private static final String[] MODE_NAMES = {
        "select", "terrain paint", "construction", "crop planting", "bulldoze", "harvest"
    };
    private static final String[] MODE_ICON_PATHS = {
        "img/ui/icons/select_tool.png",
        "img/ui/icons/terrain_tool.png",
        "img/ui/icons/construction_tool.png",
        "img/ui/icons/plant_tool.png",
        "img/ui/icons/bulldoze_tool.png",
        "img/ui/icons/harvest_tool.png"
    };
    private static final GameModes[] MODES = {
        GameModes.SELECT, GameModes.TERRAIN_PAINT, GameModes.CONSTRUCTION,
        GameModes.CROP_PLANT, GameModes.BULLDOZE, GameModes.HARVEST
    };

    private final JPanel submenuPanel = new JPanel(new GridLayout(0, 2, 4, 4));
    private final List<JButton> modeButtons = new ArrayList<>();
    private final BuildingManager buildingManager;
    private final CropManager cropManager;
    private int selectedTerrainIndex;
    private int selectedBuildingIndex;
    private int selectedCropIndex;
    private GameModes selectedMode = GameModes.SELECT;

    public ModePanel(int screenHeight, BuildingManager buildingManager, CropManager cropManager,
        ModeChangeListener listener) {
        setBorder(BorderFactory.createTitledBorder("mode"));
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(180, screenHeight));
        this.buildingManager = buildingManager;
        this.cropManager = cropManager;

        JPanel modeChoices = new JPanel(new GridLayout(MODES.length, 1, 4, 4));
        for (int i = 0; i < MODES.length; i++) {
            JButton modeButton = new JButton(loadIcon(MODE_ICON_PATHS[i]));
            int modeIndex = i;
            modeButton.setToolTipText(MODE_NAMES[i]);
            modeButton.setMargin(new Insets(4, 4, 4, 4));
            modeButton.setFocusable(false);
            modeButton.addActionListener(action -> {
                selectMode(MODES[modeIndex], listener);
            });
            modeButtons.add(modeButton);
            modeChoices.add(modeButton);
        }

        add(modeChoices, BorderLayout.WEST);
        add(submenuPanel, BorderLayout.CENTER);
        selectMode(GameModes.SELECT, listener);
    }

    public java.awt.Color getSelectedTerrainColor() {
        return TileManager.getTerrainColor(selectedTerrainIndex);
    }

    public int getSelectedBuildingIndex() {
        return selectedBuildingIndex;
    }

    public int getSelectedCropIndex() {
        return selectedCropIndex;
    }

    public void setToolsEnabled(boolean enabled) {
        for (int i = 0; i < modeButtons.size(); i++) {
            modeButtons.get(i).setEnabled(enabled || i == 0);
        }
        submenuPanel.setEnabled(enabled);
        for (java.awt.Component component : submenuPanel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void selectMode(GameModes mode, ModeChangeListener listener) {
        selectedMode = mode;
        submenuPanel.removeAll();
        if (mode == GameModes.TERRAIN_PAINT) {
            addTerrainOptions();
        } else if (mode == GameModes.CONSTRUCTION) {
            addBuildingOptions();
        } else if (mode == GameModes.CROP_PLANT) {
            addCropOptions();
        }
        submenuPanel.setVisible(mode == GameModes.TERRAIN_PAINT
            || mode == GameModes.CONSTRUCTION || mode == GameModes.CROP_PLANT);
        submenuPanel.revalidate();
        submenuPanel.repaint();
        listener.modeChanged(mode);
    }

    private void addTerrainOptions() {
        for (int i = 0; i < TERRAIN_NAMES.length; i++) {
            int optionIndex = i;
            JButton option = createOptionButton("img/tiles/" + TERRAIN_NAMES[i] + ".png",
                    TERRAIN_NAMES[i]);
            option.addActionListener(action -> selectedTerrainIndex = optionIndex);
            submenuPanel.add(option);
        }
    }

    private void addBuildingOptions() {
        for (int i = 0; i < BuildingManager.BUILDING_COUNT; i++) {
            int optionIndex = i;
            JButton option = createOptionButton(buildingManager.getBuildingImage(i),
                    buildingManager.getBuildingName(i));
            option.addActionListener(action -> selectedBuildingIndex = optionIndex);
            submenuPanel.add(option);
        }
    }

    private void addCropOptions() {
        for (int i = 0; i < CropManager.CROP_COUNT; i++) {
            int optionIndex = i;
            JButton option = createOptionButton(cropManager.getCropImage(i),
                    cropManager.getCropName(i));
            option.addActionListener(action -> selectedCropIndex = optionIndex);
            submenuPanel.add(option);
        }
    }

    private JButton createOptionButton(String path, String name) {
        return createOptionButton(new ImageIcon(path), name);
    }

    private JButton createOptionButton(java.awt.image.BufferedImage image, String name) {
        if (image == null) {
            JButton button = new JButton();
            button.setToolTipText(name);
            button.setMargin(new Insets(3, 3, 3, 3));
            button.setFocusable(false);
            return button;
        }
        return createOptionButton(new ImageIcon(image), name);
    }

    private JButton createOptionButton(ImageIcon icon, String name) {
        JButton button = new JButton(scaleIcon(icon));
        button.setToolTipText(name);
        button.setMargin(new Insets(3, 3, 3, 3));
        button.setFocusable(false);
        return button;
    }

    private ImageIcon scaleIcon(ImageIcon icon) {
        Image image = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private ImageIcon loadIcon(String path) {
        return scaleIcon(new ImageIcon(path));
    }

    public interface ModeChangeListener {
        void modeChanged(GameModes mode);
    }
}
