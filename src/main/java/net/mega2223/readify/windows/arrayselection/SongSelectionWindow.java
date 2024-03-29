package net.mega2223.readify.windows.arrayselection;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.panels.ImprovedStringSelector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SongSelectionWindow extends JFrame {

    String[] sortedArtists;
    ArrayList<PseudoArtist> pseudoArtists;
    List<String> currentArtistSongs;
    List<Runnable> conclusionTasks = new ArrayList<>();

    ArrayList<String[]> selectedSongs = new ArrayList<>();

    private static class PseudoArtist{
        String name;
        List<String> songs;
        private PseudoArtist(String name,List<String> songs){this.name = name; this.songs = songs;}
    }

    ImprovedStringSelector artistSelectionList;
    ImprovedStringSelector songSelectionList;
    JList<String> selectedSongsJL = new JList<>();

    JPanel internalPanel;
    JPanel pl = new JPanel(), pm = new JPanel(), pr = new JPanel();

    JButton addButton = new JButton("Add");
    JButton removeButton = new JButton("Remove");

    JButton endButton = new JButton("Done");

    BoxLayout layout;

    public SongSelectionWindow(String[] sortedArtists, SongHistory context){
        this.sortedArtists = sortedArtists;
        pseudoArtists = new ArrayList<>(sortedArtists.length);
        for (String sortedArtist : sortedArtists) {
            pseudoArtists.add(new PseudoArtist(sortedArtist, context.getSongsForArtist(sortedArtist)));
        }
        //JFrame logic
        internalPanel = new JPanel(new GridLayout(1,3));
        internalPanel.add(pl);internalPanel.add(pm);internalPanel.add(pr);
        artistSelectionList = new ImprovedStringSelector(true,"Select your artist.");
        artistSelectionList.setData(sortedArtists);
        songSelectionList = new ImprovedStringSelector(true,"Select the song.");
        pl.add(artistSelectionList);
        pm.setLayout(new BoxLayout(pm,BoxLayout.Y_AXIS));
        pm.add(songSelectionList);
        pm.add(addButton);
        pr.setLayout(new BoxLayout(pr,BoxLayout.Y_AXIS));
        JLabel removeLabel = new JLabel("remove songs");
        removeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pr.add(removeLabel);
        pr.add(new JScrollPane(selectedSongsJL));
        pr.add(removeButton);
        endButton.addActionListener(e -> {for(Runnable ac : conclusionTasks){ac.run();} dispose();});
        layout = new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS);
        this.getContentPane().setLayout(layout);
        add(internalPanel);
        add(endButton);
        //event handling
        artistSelectionList.addListSelectionListener(e -> {
            currentArtistSongs = context.getSongsForArtist(artistSelectionList.getSelectedValue(),false);
            songSelectionList.setData(currentArtistSongs.toArray());
        });

        addButton.addActionListener(e -> {
            List<Object> selected = songSelectionList.getSelectedValues();
            for(Object ac : selected){
                if(!selectedSongs.contains(ac)){
                    selectedSongs.add(new String[]{artistSelectionList.getSelectedValue(),ac.toString()});
                }
            }
            refreshSelectedSongs();
        });

        removeButton.addActionListener(e -> {
            List<String> sel = selectedSongsJL.getSelectedValuesList();
            selectedSongs.removeAll(sel);
            refreshSelectedSongs();
        });

        //init
        setLocationRelativeTo(null);
        setVisible(true);
        pack();
        this.invalidate();
    }

    /**Returns selected songs along with artist names*/
    public Object[] getSelectedSongs(){
        return songSelectionList.fullData;
    }
    /**Returns a list of segregated strings containing artist name and songs*/
    public List<String[]> getSelectedSongsPure(){
        return selectedSongs;
    }

    void refreshSelectedSongs(){
        String[][] toS = new String[selectedSongs.size()][];
        selectedSongs.toArray(toS);
        String[] assembled = new String[toS.length];
        for (int i = 0; i < toS.length; i++) {
            assembled[i] = toS[i][0] + " - " + toS[i][1];
        }
        selectedSongsJL.setListData(assembled);
    }

    public void addConclusionTask(Runnable task){
        conclusionTasks.add(task);
    }


}
