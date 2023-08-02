package net.mega2223.readify.windows;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;
import net.mega2223.readify.util.DataInterpreter;
import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;
import net.mega2223.utils.objects.GraphRenderer;

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

    //TODO: different states for if we are using songs from a playlist or from a user history or perhaps both, along with global variables
    //such is needed because playlists and user histories display different info

    public static final int[] DEFAULT_GRAPH_DIMENSIONS = {400, 500};

    public SongHistory songHistory = new SongHistory();
    public JPanel centralCanvas = new JPanel();
    public JLabel statusLabel = new JLabel(generateStatReport(true));
    public JMenuBar jMenuBar = new JMenuBar();
    public JMenu importer = new JMenu("Import");
    public JMenuItem fromUserHistory = new JMenuItem("From StreamHistory files");
    public JMenuItem fromEndSong = new JMenuItem("From EndSongFiles");
    public JMenuItem clearSharedSongList = new JMenuItem("Clear songs");
    public JMenu visuals = new JMenu("Visuals");
    public JMenuItem specificArtistGraphs = new JMenuItem("Generate graphic from a set of artists");
    public JMenuItem specificSongGraphs = new JMenuItem("Generate graphic from a specific set of songs");
    public JMenuItem genOverallListeningTime = new JMenuItem("Generate graphic from overall listening time");
    public JMenu stats = new JMenu("Stats");
    public JMenuItem sortArtistsByTimePlayed = new JMenuItem("Sort artists by time played");
    public JMenuItem sortSongsByTimePlayed = new JMenuItem("Sort songs by time played");

    public ApplicationWindow() {

        fromUserHistory.addActionListener(e -> {
            File[] files = importFiles();
            int songEstimate = files.length * 10000;
            int fileSizeEstimate = songEstimate * 6;
            /*for (int i = 0; i < files.length; i++) {
                //generates song estimate based on size of all arrays from all selected files
                File act = files[i];
                 this is too slow lol
                try {
                    songEstimate += JsonParser.parseString(Misc.readFromFile(act)).getAsJsonArray().size();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }*/
            JLabel stats = new JLabel();
            centralCanvas.removeAll();
            centralCanvas.add(stats);
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
                    stats.setText("Loading file " + act.getName() + "...");
                    try {
                        String fileData = Misc.readFromFile(act, () -> {
                            progressBar.setValue(counts[1]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[1] / (double) fileSizeEstimate * 100) + "%");
                            counts[1]++;
                        });
                        stats.setText("Compiling songs...");
                        SongHistory tracks = JsonConverter.convertFromStreamingHistoryFormat(fileData, () -> {
                            progressBar.setValue(counts[0]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[0] / (double) songEstimate * 100) + "%");
                            counts[0]++;
                        }, () -> {
                            centralCanvas.removeAll();
                        });
                        songHistory.loadSongs(tracks);
                    } catch (IOException | ParseException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ApplicationWindow.this, "Something happened :(, see the log for more details");
                    }
                }
                remove(panel);
                refreshSongStats();
            });
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
                /*List<Color> col = genColorList(1);
                BufferedImage graph = GraphBuilder.buildPureGraph(data, col,400,400);
                List<Date> datesToSub = PreRenderingUtils.genDates(old, nu, dateRange / 10);
                List<String>[] subs = PreRenderingUtils.genSubsForGraph(data,new double[]{.1,.1}, datesToSub);

                graph = GraphBuilder.buildAxisCaptions(graph,data,new double[]{dateRange/10,},subs[0],subs[1],400,400,100,Color.black);
                GraphBuilder.buildCorners(graph);

                 */
                BufferedImage graph = DataInterpreter.genFullGraphFromData(data,new int[]{10,10},old,nu);
                //DataInterpreter.debugData(data);
                centralCanvas.removeAll();
                JLabel stats = new JLabel();
                centralCanvas.add(stats);

                stats.setVerticalTextPosition(SwingConstants.BOTTOM);
                stats.setHorizontalTextPosition(SwingConstants.CENTER);
                stats.setIcon(new ImageIcon(graph));

                String repor = "All songs you listened during the span from " + old + " to " + nu +
                        ".\nThe numbers in the vertical axis represent roughly one interval of " + totalUnits + " full "+ unit.toLowerCase() +".";
                repor = HTMLize(repor);
                stats.setText(repor);
                pack();
                invalidate();
            });


        });
        specificArtistGraphs.addActionListener(e -> {
            List<String> artists = songHistory.getArtists();
            String arr[] = new String[artists.size()];
            //Collections.sort(artists);
            artists = songHistory.sortBasedOnArtistPopularity(artists);
            //System.out.println(artists);
            artists.toArray(arr);
            StringSelectionWindow artistSelectionWindow = new StringSelectionWindow("Select the artists that you wish to visually represent:", arr);
            artistSelectionWindow.confirmationButton.addActionListener(e1 -> {
                List<String> selected = artistSelectionWindow.getSelected();
                List<SongHistory> histories = new ArrayList<>();

                for (int i = 0; i < selected.size(); i++) {
                    String ac = selected.get(i);
                    SongHistory songsFromArtist = songHistory.getSongsFromArtist(ac);
                    histories.add(songsFromArtist);
                }
                Date old = histories.get(0).getOldest().getEndTime();
                Date nu = histories.get(0).getLatest().getEndTime();
                for (int i = 0; i < histories.size(); i++) {
                    SongHistory act = histories.get(i);
                    Date oldestEndTime = act.getOldest().getEndTime();
                    if (oldestEndTime.before(old)){old = oldestEndTime;}
                    Date newestEndTime = act.getLatest().getEndTime();
                    if (newestEndTime.after(nu)){nu = newestEndTime;}
                }
                artistSelectionWindow.dispose();
                TimeSpanSelector selector = new TimeSpanSelector();
                Date finalOld = old;
                Date finalNu = nu;
                selector.addConclusionTask(() -> {
                    double wideInSecs = selector.getTimeInSeconds();
                    double unadaptedAmount = selector.getTimeInSelectedTimeUnit();
                    TimeSpanSelector.AcceptedTimeUnit timeUnit = selector.getSelectedTimeUnit();
                    selector.dispose();
                    JPanel colorIndexPanel = new JPanel(new GridLayout(0, 1));
                    Color[] colors = genColorArray(selected.size());
                    for (int i = 0; i < selected.size(); i++) {
                        String artist = selected.get(i);
                        Color act = colors[i];
                        int r = act.getRed();
                        int g = act.getGreen();
                        int b = act.getBlue();
                        Color newColor = new Color(r, g, b);
                        String infoDisplay = artist + " (Minutes listened per each " + unadaptedAmount + " " + timeUnit.timeUnit.toLowerCase();
                        if(unadaptedAmount != 1.0){infoDisplay += "s";}
                        infoDisplay += ")";
                        JLabel timeListened = new JLabel(infoDisplay);
                        timeListened.setIcon(new ImageIcon(Misc.generateMonochromaticImage(10, 10, newColor)));
                        timeListened.setHorizontalAlignment(SwingConstants.RIGHT);
                        timeListened.setVerticalTextPosition(SwingConstants.CENTER);
                        colorIndexPanel.add(timeListened);
                    }
                    //statusCanvas.add(colorIndexPanel,BorderLayout.WEST);

                    List<List<double[]>> data = genRendererDataBasedOnTimeSpan(histories, wideInSecs);

                    JLabel stats = new JLabel();
                    centralCanvas.removeAll();
                    centralCanvas.add(stats);
                    centralCanvas.add(colorIndexPanel);

                    double[] gridIndex = {0,0};

                    for (List<double[]> dataAct : data) {
                        for(double[] dataActAct : dataAct){
                            if(dataActAct[0]>gridIndex[0]*10){gridIndex[0] = dataActAct[0]/10;}
                            if(dataActAct[1]>gridIndex[1]*10){gridIndex[1] = dataActAct[1]/10;}
                        }
                    }

                    GraphRenderer renderer = new GraphRenderer(data, new Dimension(DEFAULT_GRAPH_DIMENSIONS[0], DEFAULT_GRAPH_DIMENSIONS[1]), colors);
                    stats.setIcon(new ImageIcon(renderer.renderWithGridAndNumberNotation(new ArrayList<>(),gridIndex)));//fixme
                    stats.setText("All data from the selected artists between " + finalOld + " and " + finalNu);
                    stats.setHorizontalTextPosition(SwingConstants.CENTER);
                    stats.setVerticalTextPosition(SwingConstants.BOTTOM);
                });
            });

        });
        sortArtistsByTimePlayed.addActionListener(e -> {
            class SortingFrame extends JFrame{
                SortingFrame(){
                    setSize(100,500);
                    GridBagLayout manager = new GridBagLayout();
                    setLayout(manager);
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    setVisible(true);
                }
            }
            class ComparableArtist implements Comparable {
                String name; long criteriaWeight;
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
            
            SortingFrame sortingFrame = new SortingFrame();
            
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
            ComparableArtist[] artistsArray = new ComparableArtist[comparableArtists.size()];
            comparableArtists.toArray(artistsArray);
            sortingFrame.add(new JList<>(artistsArray));
            
        });
        sortSongsByTimePlayed.addActionListener(e -> {
            class SortingFrame extends JFrame{
                SortingFrame(){
                    setSize(100,500);
                    GridBagLayout manager = new GridBagLayout();
                    setLayout(manager);
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    setVisible(true);
                }
            }
            class ComparableSong implements Comparable {
                String name; long criteriaWeight;
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
            SortingFrame sortingFrame = new SortingFrame();
            sortingFrame.add(new JList<>(songArray));
            sortingFrame.setVisible(true);
        });
        specificSongGraphs.addActionListener(e -> {
            List<String> artists = songHistory.getArtists();
            String[] artistsArray = new String[artists.size()];
            artists.toArray(artistsArray);
            SongSelectionWindow songSelectionWindow = new SongSelectionWindow(artistsArray, songHistory,
                    () -> {
                        //todo
                    });
        });

        centralCanvas.add(statusLabel);

        visuals.add(specificArtistGraphs);
        visuals.add(specificSongGraphs);
        visuals.add(genOverallListeningTime);

        importer.add(fromEndSong);
        importer.add(fromUserHistory);
        importer.add(clearSharedSongList);

        stats.add(sortArtistsByTimePlayed);
        stats.add(sortSongsByTimePlayed);

        jMenuBar.add(importer);
        jMenuBar.add(visuals);
        jMenuBar.add(stats);

        BorderLayout layout = new BorderLayout(3, 3);
        //layout.setAlignment(FlowLayout.CENTER);
        setLayout(layout);

        setJMenuBar(jMenuBar);
        add(centralCanvas);//, JFrame.CENTER_ALIGNMENT);

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static List<List<double[]>> genRendererDataBasedOnTimeSpan(SongHistory songHistory, double loopFrequencyInSeconds) {
        ArrayList l = new ArrayList();
        l.add(songHistory);
        return genRendererDataBasedOnTimeSpan(l, loopFrequencyInSeconds);
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

    public void refreshSongStats() {
        statusLabel.setText(generateStatReport(true));
        invalidate();
    }

    public String generateStatReport(boolean html) {
        return generateStatReport(html, "");
    }

    public String generateStatReport(boolean html, String embed) {
        List<Track> songs = songHistory.getListens();
        if (songs.size() == 0) {
            return ("No songs loaded");
        } else if (songs.size() == 1) {
            return ("1 session currently loaded");
        } else {
            List<Track> individualSongs = songHistory.getSongs();
            List<String> individualArtists = songHistory.getArtists();

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
