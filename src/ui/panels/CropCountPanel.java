package src.ui.panels;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import src.managers.CropManager;

public class CropCountPanel extends JPanel implements CropManager.CountListener {
    private final CropManager cropManager;
    private final JLabel[] countLabels = new JLabel[CropManager.CROP_COUNT];

    public CropCountPanel(CropManager cropManager, int screenHeight) {
        this.cropManager = cropManager;
        setBorder(BorderFactory.createTitledBorder("crops"));
        setLayout(new GridLayout(0, 1, 4, 4));
        setPreferredSize(new Dimension(150, screenHeight));
        createRows();
        cropManager.addCountListener(this);
    }

    @Override
    public void countsChanged() {
        for (int i = 0; i < countLabels.length; i++) {
            String cropName = cropManager.getCropName(i);
            if (cropName == "tree"){
                cropName = "wood";
            }
            countLabels[i].setText(cropName + ": " + cropManager.getCropCount(i));
        }
    }

    private void createRows() {
        for (int i = 0; i < CropManager.CROP_COUNT; i++) {
            JPanel cropRow = new JPanel(new BorderLayout(6, 0));
            BufferedImage crop = cropManager.getCropImage(i);
            if (crop != null) {
                Image thumbnail = crop.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                cropRow.add(new JLabel(new ImageIcon(thumbnail)), BorderLayout.WEST);
            }

            countLabels[i] = new JLabel();
            cropRow.add(countLabels[i], BorderLayout.CENTER);
            add(cropRow);
        }
        countsChanged();
    }
}
