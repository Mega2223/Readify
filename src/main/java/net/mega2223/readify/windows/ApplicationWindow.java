package net.mega2223.readify.windows;

import net.mega2223.readify.ApplicationState;
import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;
import net.mega2223.readify.util.DataInterpreter;
import net.mega2223.readify.util.GraphGenerator;
import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;
import net.mega2223.readify.windows.arrayselection.ArtistSelectionWindow;
import net.mega2223.readify.windows.arrayselection.SongSelectionWindow;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static net.mega2223.readify.objects.SongHistory.isInDateRange;
import static net.mega2223.readify.util.Misc.*;

public class ApplicationWindow extends JFrame {

    public SongHistory songHistory = new SongHistory();
    Font currentFont = new Font("Consolas",Font.PLAIN,10);

    //JFrame elements
    public JPanel centralCanvas = new JPanel();
    public JMenuBar jMenuBar = new JMenuBar();
    public JMenu importer = new JMenu("Import");
    public JMenuItem fromUserHistory = new JMenuItem("From StreamHistory files");
    public JMenuItem fromEndSong = new JMenuItem("From EndSongFiles");
    public JMenuItem clearSharedSongList = new JMenuItem("Clear songs");
    public JMenu visuals = new JMenu("Visuals");
    public JMenuItem specificArtistGraphs = new JMenuItem("Generate graph from a set of artists");
    public JMenuItem specificSongGraphs = new JMenuItem("Generate graph from a specific set of songs");
    public JMenuItem genOverallListeningTime = new JMenuItem("Generate graph from overall listening time");
    public JMenu statsJM = new JMenu("Stats");
    public JMenuItem sortArtistsByTimePlayed = new JMenuItem("Sort artists by time played");
    public JMenuItem sortSongsByTimePlayed = new JMenuItem("Sort songs by time played");

    public ApplicationWindow() {

        fromUserHistory.addActionListener(e -> {
            File[] files = importFiles();
            int songEstimate = files.length * 10000;
            int fileSizeEstimate = songEstimate * 6;

            JLabel updateStatus = new JLabel("Loading songs...");
            JProgressBar progressBar = new JProgressBar(0, fileSizeEstimate);
            JPanel panel = new JPanel(new GridLayout(2, 1));
            panel.add(updateStatus);
            panel.add(progressBar);
            pack();

            final int[] counts = {0, 0};
            Thread loadingThread = new Thread(() -> {
                for (int i = 0; i < files.length; i++) {
                    File act = files[i];
                    setStatus("Loading file " + act.getName() + "...",false);
                    try {
                        String fileData = Misc.readFromFile(act, () -> {
                            progressBar.setValue(counts[1]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[1] / (double) fileSizeEstimate * 100) + "%");
                            counts[1]++;
                        });
                        statsJM.setText("Compiling songs...");
                        SongHistory tracks = JsonConverter.convertFromStreamingHistoryFormat(fileData, () -> {
                            progressBar.setValue(counts[0]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[0] / (double) songEstimate * 100) + "%");
                            counts[0]++;
                        }, () -> centralCanvas.remove(panel));
                        songHistory.loadSongs(tracks);
                    } catch (IOException | ParseException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ApplicationWindow.this, "Something happened :(, see the log for more details");
                    }
                }
                remove(panel);
                refreshSongStats();
            });
            centralCanvas.removeAll();
            centralCanvas.add(panel);
            loadingThread.start();
        });
        fromEndSong.addActionListener(e -> {
            File[] files = importFiles();
            for (int i = 0; i < files.length; i++) {
                File act = files[i];
                try {
                    String data = Misc.readFromFile(act);
                    SongHistory history = JsonConverter.convertFromEndSongFormat(data);
                    songHistory.loadSongs(history);
                    refreshSongStats();
                } catch (IOException | ParseException ex) {
                    ex.printStackTrace();
                }

            }

        });
        clearSharedSongList.addActionListener(e -> {
            songHistory.clearSongs();
            refreshSongStats();
            invalidate();
        });
        genOverallListeningTime.addActionListener(e -> {
            TimeSpanSelector sel = new TimeSpanSelector();
            sel.addConclusionTask(() -> {
                String unit = sel.getSelectedTimeUnit().toString();
                double totalUnits = sel.getTimeInSelectedTimeUnit();
                if(totalUnits !=1){unit+="s";}
                sel.dispose();

                Date old = songHistory.getOldest().getEndTime();
                Date nu = songHistory.getLatest().getEndTime();

                double[][][] data = DataInterpreter.genTotalTimeListenedData(songHistory,(long)(sel.getTimeInSeconds()*1000));
                BufferedImage graph = GraphGenerator.genFullGraphAndCaptionsFromData(data,new int[]{10,10},old,nu,currentFont,800,500,genColorList(1));

                JLabel label = new JLabel();
                label.setVerticalTextPosition(SwingConstants.BOTTOM);
                label.setHorizontalTextPosition(SwingConstants.CENTER);

                String repor = "All songs you listened during the span from " + old + " to " + nu +
                        ".\nThe numbers in the vertical axis represent roughly one interval of " + totalUnits + " full "+ unit.toLowerCase() +".";
                setStatus(repor,true);
                label.setIcon(new ImageIcon(graph));
                centralCanvas.add(label);
                pack();
                invalidate();
            });


        });
        specificArtistGraphs.addActionListener(e -> {
            ApplicationState.checkForOngoingTask(true,true);
            List<String> artists = songHistory.getArtists();
            String[] arr = new String[artists.size()];
            artists = songHistory.sortBasedOnArtistPopularity(artists);
            artists.toArray(arr);

            ArtistSelectionWindow artistSelectionWindow = new ArtistSelectionWindow(arr,"Select the artists that you wish to visually represent:","Artist selector");

            artistSelectionWindow.addConclusionTask(() -> {
                List<Object> sel = artistSelectionWindow.getSelected();
                List<String> selected = new ArrayList<>();
                for(Object ac : sel){selected.add(ac.toString());}

                List<SongHistory> histories = new ArrayList<>();

                for (int i = 0; i < selected.size(); i++) {
                    String ac = selected.get(i);
                    SongHistory songsFromArtist = songHistory.getSongsFromArtist(ac);
                    histories.add(songsFromArtist);
                }

                artistSelectionWindow.dispose();
                TimeSpanSelector selector = new TimeSpanSelector();
                selector.addConclusionTask(() -> {
                    final double wideInSecs = selector.getTimeInSeconds();
                    final double unadaptedAmount = selector.getTimeInSelectedTimeUnit();
                    TimeSpanSelector.AcceptedTimeUnit timeUnit = selector.getSelectedTimeUnit();
                    selector.dispose();
                    Date[] bounds = DataInterpreter.getOldestAndLatestDates(histories);
                    double[][][] data = DataInterpreter.genFromASetOfArtists(songHistory,selected, (long) (wideInSecs*1000));
                    JPanel colorIndexPanel = new JPanel(new GridLayout(0, 1));
                    List<Color> colors = genColorList(selected.size());
                    for (int i = 0; i < selected.size(); i++) {
                        String artist = selected.get(i);
                        Color act = colors.get(i);
                        String infoDisplay = artist + " (Minutes listened per each TIMEU " + timeUnit.timeUnit.toLowerCase();
                        if(unadaptedAmount != 1.0){infoDisplay += "s";}
                        if(unadaptedAmount == Math.floor(unadaptedAmount)){
                            infoDisplay = infoDisplay.replace("TIMEU",(int) unadaptedAmount + "");
                        } else {
                            infoDisplay = infoDisplay.replace("TIMEU",unadaptedAmount + "");
                        }
                        infoDisplay += ")";
                        JLabel timeListened = new JLabel(infoDisplay);
                        timeListened.setIcon(new ImageIcon(Misc.generateMonochromaticImage(10, 10, act)));
                        timeListened.setHorizontalAlignment(SwingConstants.CENTER);
                        timeListened.setVerticalTextPosition(SwingConstants.CENTER);
                        colorIndexPanel.add(timeListened);
                    }
                    //statusCanvas.add(colorIndexPanel,BorderLayout.WEST);
                    centralCanvas.removeAll();
                    JLabel statusLabel = new JLabel();
                    statusLabel.setIcon(new ImageIcon(GraphGenerator.genFullGraphAndCaptionsFromData(data,new int[]{10,10},bounds[0],bounds[1],currentFont,800,500,colors)));
                    statusLabel.setText("All data from the selected artists.");
                    statusLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                    statusLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
                    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

                    centralCanvas.add(statusLabel);
                    centralCanvas.add(colorIndexPanel);
                    pack();
                });
            });

        });
        specificSongGraphs.addActionListener(e -> {
            List<String> artists = songHistory.getArtists();
            String[] artistsArray = new String[artists.size()];
            artists.toArray(artistsArray);

            SongSelectionWindow songSelectionWindow = new SongSelectionWindow(artistsArray, songHistory);
            songSelectionWindow.addConclusionTask(() -> {
                List<String[]> selectedSongs = songSelectionWindow.getSelectedSongsPure();
                TimeSpanSelector sel = new TimeSpanSelector();
                sel.addConclusionTask(()->{
                    long timeIntervalMilis = (long) (sel.getTimeInSeconds()*1000);
                    double timeInUnit = sel.getTimeInSelectedTimeUnit();
                    double[][][] data = DataInterpreter.genFromASetOfSongs(songHistory,selectedSongs,timeIntervalMilis);

                    double[] dataBounds = DataInterpreter.getDataBounds(data);
                    Date old = new Date((long) dataBounds[0]);
                    Date nu = new Date((long) dataBounds[1]);

                    List<Color> colors = genColorList(data.length);

                    JPanel captionPanel = new JPanel(new GridLayout(0, 1));
                    String timeAmount;
                    if(timeInUnit == Math.floor(timeInUnit)){timeAmount = (int) timeInUnit + "";} else {timeAmount = timeInUnit + "";}
                    for (int i = 0; i < selectedSongs.size(); i++) {
                        StringBuilder labelText = new StringBuilder();
                        String[] act = selectedSongs.get(i);
                        labelText.append(act[1]).append(" - ").append(act[0]).append(" (Minutes listened in a ").append(timeAmount).append(" ").append(sel.getSelectedTimeUnit().timeUnit).append(" interval)");
                        JLabel ac = new JLabel(labelText.toString());
                        ac.setIcon(new ImageIcon(Misc.generateMonochromaticImage(10,10,colors.get(i))));
                        captionPanel.add(ac);
                    }

                    BufferedImage graph = GraphGenerator.genFullGraphAndCaptionsFromData(data,new int[]{10,10},old,nu,currentFont,800,500,colors);
                    JLabel label = new JLabel(new ImageIcon(graph));
                    label.setHorizontalTextPosition(SwingConstants.CENTER);
                    label.setVerticalTextPosition(SwingConstants.BOTTOM);
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    centralCanvas.removeAll();
                    centralCanvas.add(label);
                    centralCanvas.add(captionPanel);
                    sel.dispose();
                });
            });
        });
        sortArtistsByTimePlayed.addActionListener(e -> {

            class ComparableArtist implements Comparable {
                final String name; final long criteriaWeight;
                ComparableArtist(String artist, long criteriaWeight){
                    name = artist; this.criteriaWeight = criteriaWeight;
                }
                @Override
                public int compareTo(Object o) {
                    if(o instanceof ComparableArtist){
                        return (int) (((ComparableArtist) o).criteriaWeight - this.criteriaWeight);
                    }
                    return 0;
                }

                @Override
                public String toString() {
                    return name + ": " + criteriaWeight + " minutes played in total";
                }
            }

            SortDisplayFrame display = new SortDisplayFrame("Artists sorted by playtime");
            
            List<String> artists = songHistory.getArtists();
            long[] minutesPlayed = new long[artists.size()];
            List<Track> listens = songHistory.getListens();
            for(Track ac : listens){
                int index = artists.indexOf(ac.getArtistName());
                if(index < 0){
                    continue;
                }
                minutesPlayed[index] += ac.getMsPlayed()/1000/60;
            }
            List<ComparableArtist> comparableArtists = new ArrayList<>(minutesPlayed.length);
            for (int i = 0; i < minutesPlayed.length; i++) {
                comparableArtists.add(new ComparableArtist(artists.get(i),minutesPlayed[i]));
            }
            Collections.sort(comparableArtists);
            for (ComparableArtist act : comparableArtists){
                display.add(act);
            }

            
        });
        sortSongsByTimePlayed.addActionListener(e -> {
            SortDisplayFrame frame = new SortDisplayFrame("Songs sorted by playtime");
            class ComparableSong implements Comparable {
                final String name; final long criteriaWeight;
                ComparableSong(String name, long criteriaWeight){
                    this.name = name; this.criteriaWeight = criteriaWeight;
                }
                @Override
                public int compareTo(Object o) {
                    if(o instanceof ComparableSong){
                        return (int) (((ComparableSong) o).criteriaWeight - this.criteriaWeight);
                    }
                    return 0;
                }

                @Override
                public String toString() {
                    return name + ": " + criteriaWeight + " minutes played in total";
                }
            }
            List<Track> songs = songHistory.getSongs();
            ComparableSong[] songArray = new ComparableSong[songs.size()];
            for(int i = 0; i < songArray.length; i++){
                Track actTrack = songs.get(i);
                songArray[i] = new ComparableSong(actTrack.getTrackName(),actTrack.getMsPlayed()/1000/60);}
            Arrays.sort(songArray);
            for(ComparableSong act : songArray){frame.add(act);}
        });

        visuals.add(specificArtistGraphs);
        visuals.add(specificSongGraphs);
        visuals.add(genOverallListeningTime);

        importer.add(fromEndSong);
        importer.add(fromUserHistory);
        importer.add(clearSharedSongList);

        statsJM.add(sortArtistsByTimePlayed);
        statsJM.add(sortSongsByTimePlayed);

        jMenuBar.add(importer);
        jMenuBar.add(visuals);
        jMenuBar.add(statsJM);

        BorderLayout layout = new BorderLayout(3, 3);
        //layout.setAlignment(FlowLayout.CENTER);
        setLayout(layout);

        setJMenuBar(jMenuBar);
        centralCanvas.setLayout(new BoxLayout(centralCanvas,BoxLayout.Y_AXIS));
        add(centralCanvas);//, JFrame.CENTER_ALIGNMENT);

        setSize(500,400);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static List<List<double[]>> genRendererDataBasedOnTimeSpan(List<SongHistory> songHistories, double loopFrequencyInSeconds) {

        Date old = songHistories.get(0).getOldest().getEndTime();
        Date nu = songHistories.get(0).getLatest().getEndTime();

        Track oldTrack = songHistories.get(0).getOldest();
        Track newTrack = songHistories.get(0).getLatest();

        for (int i = 1; i < songHistories.size(); i++) {
            //gets the oldest and newest songs from all songhistories and selects the oldest oldest, and newest newest respecively
            //thanks CGPGray for making the above sentence acceptable
            Track oldest = songHistories.get(i).getOldest();
            Date oldestEndTime = oldest.getEndTime();
            Track newest = songHistories.get(i).getLatest();
            Date newestEndTime = newest.getEndTime();
            if (old.after(oldestEndTime)) {
                old = oldestEndTime;
                oldTrack = oldest;
            }
            if (nu.before(newestEndTime)) {
                nu = newestEndTime;
                newTrack = newest;
            }
        }

        System.out.println("OLDEST: " + oldTrack);
        System.out.println("NEWEST: " + newTrack);

        List<List<double[]>> overallData = new ArrayList<>();

        for (int i = 0; i < songHistories.size(); i++) {
            overallData.add(new ArrayList<>());
        }

        int dateItnerations = 0;

        for (Date dateAct = (Date) old.clone(); dateAct.before(nu);
            //goes checking each day if wideness = 1;
             dateAct = Date.from(Instant.ofEpochSecond((long) (dateAct.toInstant().getEpochSecond() + loopFrequencyInSeconds)))) {
            for (int l = 0; l < songHistories.size(); l++) {
                SongHistory songHistory = songHistories.get(l);
                //measurement of individual listens
                int ls = 0;
                //measurement of milis listened
                long msls = 0;
                List<Track> listens = songHistory.getListens();
                for (int i = 0; i < listens.size(); i++) {
                    Track act = listens.get(i);
                    if (isInDateRange(act.getEndTime(), dateAct, loopFrequencyInSeconds / 60 / 60 / 24)) {
                        ls++;
                        msls += act.getMsPlayed();
                    }
                }
                overallData.get(l).add(new double[]{dateItnerations, msls/1000./60});
            }
            dateItnerations++;
        }
        return overallData;
    }

    public double inputDouble(String message) {
        return Double.parseDouble(JOptionPane.showInputDialog(message));
    }

    public void setStatus(String text, boolean htmlize){
        centralCanvas.removeAll();
        if(htmlize){text = Misc.HTMLize(text);}
        centralCanvas.removeAll();
        centralCanvas.add(new JLabel(text));
    }

    public void refreshSongStats() {
        setStatus(generateStatReport(true),true);
        invalidate();
    }

    public String generateStatReport(boolean html) {
        return generateStatReport(html, "");
    }
    public String generateStatReport(boolean html, String embed){
        return generateStatReport(html,embed,true);
    }
    public String generateStatReport(boolean html, String embed, boolean displayGeneratingReport) {
        if(displayGeneratingReport){
            setStatus("Generating report...",true);
            invalidate(); this.repaint();
        }
        List<Track> songs = songHistory.getListens();

        if (songs.isEmpty()) {
            return ("No songs loaded");
        } else if (songs.size() == 1) {
            return ("1 session currently loaded");
        } else {
            List<Track> individualSongs = songHistory.getSongs();
            List<String> individualArtists = songHistory.getArtists(); //these two take too much time
            int totalTimeListenedSeconds = songHistory.getTotalTimeListenedSeconds();
            String ret = "" + songs.size() + " individual sessions loaded.\n " +
                    "With a total of " + individualSongs.size() + " individual songs and " + individualArtists.size() + " individual artists." +
                    "\n Overall watchtime of " + totalTimeListenedSeconds + " seconds. (" + totalTimeListenedSeconds / 60 + " minutes, " + totalTimeListenedSeconds / 60 / 60 + " hours or " + totalTimeListenedSeconds / 60 / 60 / 24 + " days)\n" + embed;
            if (html) {
                ret = HTMLize(ret);
            }
            return (ret);
        }
    }

    public File[] importFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Json files", "json", "txt"));
        chooser.showOpenDialog(this);
        return chooser.getSelectedFiles();
    }

}
