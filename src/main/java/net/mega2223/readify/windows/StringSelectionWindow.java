package net.mega2223.readify.windows;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StringSelectionWindow extends JFrame {


    String[] selection;
    public JCheckBox boxes[];
    public JButton confirmationButton = new JButton("Done!");

    public StringSelectionWindow(String initialPrompt, String[] array){
        JPanel panel = new JPanel(new GridLayout(array.length+2,1));
        JScrollPane scroll = new JScrollPane(panel);
        selection = array;
        panel.add(new JLabel(initialPrompt));
        createCheckboxes(panel);
        add(scroll);
        //panel.add(confirmationButton);
        panel.add(confirmationButton);
        setVisible(true);

        pack();
    }



    void createCheckboxes(JPanel panel){
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

    public List<String> getSelected(){
        ArrayList ret = new ArrayList<>();
        for (int i = 0; i < boxes.length; i++) {
            if(boxes[i].isSelected()){
                ret.add(selection[i]);
            }
        }
        return ret;
    }
}
