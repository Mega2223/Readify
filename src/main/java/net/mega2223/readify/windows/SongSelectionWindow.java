package net.mega2223.readify.windows;

import net.mega2223.readify.objects.SongHistory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SongSelectionWindow extends JFrame {

    String[] sortedArtists;
    ArrayList<PseudoArtist> pseudoArtists;
    List<String> currentArtistSongs;
    ArrayList<Object> selectedSongs = new ArrayList<>();

    private class PseudoArtist{
        String name;
        List<String> songs;
        private PseudoArtist(String name,List<String> songs){this.name = name; this.songs = songs;}
    }

    ImprovedSingleStringSelection artistSelectionList;
    ImprovedSingleStringSelection songSelectionList;
    JList<String> selectedSongsL = new JList<>();

    JPanel internalPanel;
    JPanel pl = new JPanel(), pm = new JPanel(), pr = new JPanel();

    JButton addButton = new JButton("Add");
    JButton removeButton = new JButton("Remove");

    JButton endButton = new JButton("Done");

    BoxLayout layout;

    public SongSelectionWindow(String[] sortedArtists, SongHistory context, Runnable onEnd){
        this.sortedArtists = sortedArtists;
        pseudoArtists = new ArrayList<>(sortedArtists.length);
        for (String sortedArtist : sortedArtists) {
            pseudoArtists.add(new PseudoArtist(sortedArtist, context.getSongsForArtist(sortedArtist)));
        }
        //JFrame logic
        internalPanel = new JPanel(new GridLayout(1,3));
        internalPanel.add(pl);internalPanel.add(pm);internalPanel.add(pr);
        artistSelectionList = new ImprovedSingleStringSelection(true,"Select your artist.");
        artistSelectionList.setData(sortedArtists);
        songSelectionList = new ImprovedSingleStringSelection(true,"Select the song.");
        pl.add(artistSelectionList);
        pm.setLayout(new BoxLayout(pm,BoxLayout.Y_AXIS));
        pm.add(songSelectionList);
        pm.add(addButton);
        pr.setLayout(new BoxLayout(pr,BoxLayout.Y_AXIS));
        JLabel removeLabel = new JLabel("remove songs");
        removeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pr.add(removeLabel);
        pr.add(new JScrollPane(selectedSongsL));
        pr.add(removeButton);
        endButton.addActionListener(e -> {
            if(onEnd!=null){onEnd.run();}
        });
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
                    selectedSongs.add(artistSelectionList.getSelectedValue() + " - " + ac);

                }
            }
            refreshSelectedSongs();
        });

        removeButton.addActionListener(e -> {
            List<String> sel = selectedSongsL.getSelectedValuesList();
            selectedSongs.removeAll(sel);
            refreshSelectedSongs();
        });

        //init

        setVisible(true);
        pack();
        this.invalidate();
    }

    public Object[] getSelectedSongs(){
        return songSelectionList.fullData;
    }

    void refreshSelectedSongs(){
        String[] toS = new String[selectedSongs.size()];
        selectedSongs.toArray(toS);
        selectedSongsL.setListData(toS);
    }
}
