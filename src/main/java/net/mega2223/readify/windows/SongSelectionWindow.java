package net.mega2223.readify.windows;

import net.mega2223.readify.objects.SongHistory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SongSelectionWindow extends JFrame {

    String[] sortedArtists;
    ArrayList<PseudoArtist> pseudoArtists;
    private class PseudoArtist{
        String name;
        List<String> songs;
        private PseudoArtist(String name,List<String> songs){this.name = name; this.songs = songs;}
    }

    JList artistSelectionList;
    JList songSelectionList;

    JPanel internalPanel;
    JPanel pl = new JPanel(), pm = new JPanel(), pr = new JPanel();

    public SongSelectionWindow(String[] sortedArtists, SongHistory context){
        this.sortedArtists = sortedArtists;
        pseudoArtists = new ArrayList<>(sortedArtists.length);
        for (String sortedArtist : sortedArtists) {
            pseudoArtists.add(new PseudoArtist(sortedArtist, context.getSongsForArtist(sortedArtist)));
        }
        //JFrame logic
        internalPanel = new JPanel(new GridLayout(1,3));
        internalPanel.add(pl);internalPanel.add(pm);internalPanel.add(pr);
        artistSelectionList = new JList(sortedArtists);
        songSelectionList = new JList();
        pl.add(artistSelectionList);
        pm.add(songSelectionList);
        add(internalPanel);
        //event handling
        artistSelectionList.addListSelectionListener(e -> {
            List<String> c = context.getSongsForArtist((String) artistSelectionList.getSelectedValue());
            songSelectionList.setListData(c.toArray());
        });

        //init
        setVisible(true);
        pack();
    }

}
