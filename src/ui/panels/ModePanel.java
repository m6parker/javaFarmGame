package src.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import src.managers.TileManager;
import src.GameMode;
import src.managers.BuildingManager;
import src.managers.CropManager;

public class ModePanel extends JPanel {
    private static final String[] TERRAIN_NAMES = {
        "grass", "water", "lava", "sand", "soil", "stone"
    };
    private static final String[] MODE_LABELS = {
        "select", "terrain paint", "construction", "crop planting", "bulldoze"
    };
    private static final GameMode[] MODES = {
        GameMode.SELECT, GameMode.TERRAIN_PAINT, GameMode.CONSTRUCTION,
        GameMode.CROP_PLANT, GameMode.BULLDOZE
    };

    private final JComboBox<String> terrainSelector = new JComboBox<>(TERRAIN_NAMES);
    private final JComboBox<String> buildingSelector;
    private final JComboBox<String> cropSelector;
    private GameMode selectedMode = GameMode.SELECT;

        public ModePanel(int screenHeight, BuildingManager buildingManager, CropManager cropManager,
            ModeChangeListener listener) {
        setBorder(BorderFactory.createTitledBorder("mode"));
        setLayout(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(180, screenHeight));

        String[] buildingNames = new String[BuildingManager.BUILDING_COUNT];
        for (int i = 0; i < buildingNames.length; i++) {
            buildingNames[i] = buildingManager.getBuildingName(i);
        }
        buildingSelector = new JComboBox<>(buildingNames);

        String[] cropNames = new String[CropManager.CROP_COUNT];
        for (int i = 0; i < cropNames.length; i++) {
            cropNames[i] = cropManager.getCropName(i);
        }
        cropSelector = new JComboBox<>(cropNames);

        JPanel modeChoices = new JPanel(new GridLayout(0, 1, 4, 4));
        ButtonGroup modeGroup = new ButtonGroup();
        for (int i = 0; i < MODES.length; i++) {
            JRadioButton modeButton = new JRadioButton(MODE_LABELS[i]);
            modeButton.setActionCommand(MODES[i].name());
            modeButton.setSelected(i == 0);
            modeButton.addActionListener(action -> {
                selectedMode = GameMode.valueOf(action.getActionCommand());
                terrainSelector.setEnabled(selectedMode == GameMode.TERRAIN_PAINT);
                buildingSelector.setEnabled(selectedMode == GameMode.CONSTRUCTION);
                cropSelector.setEnabled(selectedMode == GameMode.CROP_PLANT);
                listener.modeChanged(selectedMode);
            });
            modeGroup.add(modeButton);
            modeChoices.add(modeButton);
        }

        JPanel terrainChoice = new JPanel(new BorderLayout(4, 0));
        terrainChoice.add(new JLabel("terrain"), BorderLayout.WEST);
        terrainChoice.add(terrainSelector, BorderLayout.CENTER);
        terrainSelector.setEnabled(false);

        JPanel buildingChoice = new JPanel(new BorderLayout(4, 0));
        buildingChoice.add(new JLabel("building"), BorderLayout.WEST);
        buildingChoice.add(buildingSelector, BorderLayout.CENTER);
        buildingSelector.setEnabled(false);

        JPanel cropChoice = new JPanel(new BorderLayout(4, 0));
        cropChoice.add(new JLabel("crop"), BorderLayout.WEST);
        cropChoice.add(cropSelector, BorderLayout.CENTER);
        cropSelector.setEnabled(false);

        JPanel toolChoices = new JPanel(new GridLayout(0, 1, 4, 4));
        toolChoices.add(terrainChoice);
        toolChoices.add(buildingChoice);
        toolChoices.add(cropChoice);

        add(modeChoices, BorderLayout.NORTH);
        add(toolChoices, BorderLayout.SOUTH);
    }

    public GameMode getSelectedMode() {
        return selectedMode;
    }

    public java.awt.Color getSelectedTerrainColor() {
        return TileManager.getTerrainColor(terrainSelector.getSelectedIndex());
    }

    public int getSelectedBuildingIndex() {
        return buildingSelector.getSelectedIndex();
    }

    public int getSelectedCropIndex() {
        return cropSelector.getSelectedIndex();
    }

    public interface ModeChangeListener {
        void modeChanged(GameMode mode);
    }
}
