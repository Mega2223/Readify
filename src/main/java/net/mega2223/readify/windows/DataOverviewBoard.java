package net.mega2223.readify.windows;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DataOverviewBoard extends JFrame {
    public DataOverviewBoard(SongHistory data){
        this.setLayout(new GridLayout(0,1));

        List<Track> listens = data.getListens();
        add(new JLabel(listens.size() + " sessions in record."));

        Track oldest = data.getOldest();
        add(new JLabel("Oldest track in record: " + oldest.getIdentifier() + "; first played at " + oldest.getEndTime().toString() + "."));
        Track latest = data.getLatest();
        add(new JLabel("Latest track in record: " + latest.getIdentifier() + "; last played at " + latest.getEndTime().toString() + "."));
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
}
