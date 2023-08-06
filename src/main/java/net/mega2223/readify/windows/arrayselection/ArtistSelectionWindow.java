package net.mega2223.readify.windows.arrayselection;

import net.mega2223.readify.panels.ImprovedStringSelector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtistSelectionWindow extends JFrame {

    List<Runnable> conclusionTasks = new ArrayList<>();
    GridLayout layout = new GridLayout(0,2);
    ArrayList<Object> selected = new ArrayList<>();

    JPanel centralPanel = new JPanel();
    JPanel leftSide = new JPanel();
    JButton addButton = new JButton("Add");
    ImprovedStringSelector artistSelector = new ImprovedStringSelector(true,"All of your artists:");
    JPanel rightSide = new JPanel();
    JButton removeButton = new JButton("Remove");
    ImprovedStringSelector selectedArtists = new ImprovedStringSelector(true,"Selected artists:");
    JButton conclusionButton = new JButton("Done!");

    public ArtistSelectionWindow(String[] artists, String label, String name){
        artistSelector.setData(artists);

        leftSide.setLayout(new BoxLayout(leftSide,BoxLayout.Y_AXIS));
        leftSide.add(artistSelector);
        leftSide.add(addButton);

        rightSide.setLayout(new BoxLayout(rightSide,BoxLayout.Y_AXIS));
        rightSide.add(selectedArtists);
        rightSide.add(removeButton);

        addButton.addActionListener(e -> {
            List<Object> sel =  artistSelector.getSelectedValues();
            selected.addAll(sel);
            refreshSelected();
        });
        removeButton.addActionListener(e -> {
            List<Object> sel =  selectedArtists.getSelectedValues();
            selected.removeAll(sel);
            refreshSelected();
        });
        conclusionButton.addActionListener(e -> {
            for(Runnable t : conclusionTasks){t.run();}
            this.dispose();
        });

        centralPanel.setLayout(layout);
        centralPanel.add(leftSide);
        centralPanel.add(rightSide);
        setTitle(name);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
        add(new JLabel(label));
        add(centralPanel);
        add(conclusionButton);
        pack();
        setVisible(true);
    }

    protected void refreshSelected(){
        Object[] newData = new Object[selected.size()];
        for(int i = 0; i < newData.length; i++){newData[i] = selected.get(i);}
        selectedArtists.setData(newData);
    }

    public void addConclusionTask(Runnable task){
        conclusionTasks.add(task);
    }

    public List<Object> getSelected() {
        return Collections.unmodifiableList(selected);
    }
}
