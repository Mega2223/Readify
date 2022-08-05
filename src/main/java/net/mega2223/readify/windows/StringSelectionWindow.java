package net.mega2223.readify.windows;

import javax.swing.*;
import java.awt.*;

public class StringSelectionWindow extends JFrame {

    private JPanel panel;
    String[] selection;
    public JCheckBox boxes[];
    JButton confirmationButton = new JButton("Done!");

    public StringSelectionWindow(String initialPrompt, String[] array){
        setVisible(true);
        setSize(400,300);
        setLayout(new FlowLayout());

        panel = new JPanel(new GridLayout(array.length+2,1));

        selection = array;
        panel.add(new JLabel(initialPrompt));
        add(panel);
        createCheckboxes();
        add(confirmationButton);
    }



    void createCheckboxes(){
        boxes = new JCheckBox[selection.length];
        for (int i = 0; i < selection.length; i++) {
            String act = selection[i];
            JCheckBox box = new JCheckBox(act);
            panel.add(box);
            boxes[i] = box;
        }
    }

    public boolean[] getResults() {
        boolean results[] = new boolean[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            results[i] = boxes[i].isSelected();
        }
        return results;
    }

}
