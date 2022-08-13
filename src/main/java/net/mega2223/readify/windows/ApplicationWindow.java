package net.mega2223.readify.windows;

import net.mega2223.utils.objects.GraphRenderer;
import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;
import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static net.mega2223.readify.objects.SongHistory.isInDateRange;
import static net.mega2223.readify.util.Misc.HTMLize;

public class ApplicationWindow extends JFrame {

    //TODO: different states for if we are using songs from a playlist or from a user history or perhaps both, along with global variables
    //such is needed because playlists and user histories display different info

    public static final int[] DEFAULT_GRAPH_DIMENSIONS = {300,300};

    public SongHistory songHistory = new SongHistory();
    public JPanel statusCanvas = new JPanel();
    public JMenuBar jMenuBar = new JMenuBar();
     public JMenu importer = new JMenu("Import");
      public JMenuItem fromUserHistory = new JMenuItem("From StreamHistory files");
      public JMenuItem fromEndSong = new JMenuItem("From EndSongFiles");
      public JMenuItem clearSharedSongList = new JMenuItem("Clear songs");
     public JMenu visuals = new JMenu("Visuals");
      public JMenuItem specificArtistGraphs = new JMenuItem("Generate graphic from a set of artists");
      public JMenuItem specificSongGraphics = new JMenuItem("Generate graphic from a specific set of songs");
      public JMenuItem generateFromOverallListeningTime = new JMenuItem("Generate graphic from overall listening time");

    public ApplicationWindow(int sX, int sY) {

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
            statusCanvas.removeAll();
            statusCanvas.add(stats);
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
                            statusCanvas.removeAll();
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
            statusCanvas.add(panel);
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
        generateFromOverallListeningTime.addActionListener(e -> {
            TimeSpanSelector sel = new TimeSpanSelector();
            sel.addConclusionTask(() -> {
                List list = genRendererDataBasedOnTimeSpan(songHistory,sel.getTimeInSeconds());
                sel.dispose();

                Date old = songHistory.getOldest().getEndTime();
                Date nu = songHistory.getNewest().getEndTime();

                GraphRenderer renderer = new GraphRenderer(list, new Dimension(DEFAULT_GRAPH_DIMENSIONS[0], DEFAULT_GRAPH_DIMENSIONS[1]), Misc.PREFERRED_COLORS);

                double songNumber = 1000;
                double[] grid = {30.437, 10};

                statusCanvas.removeAll();
                JLabel stats = new JLabel();
                statusCanvas.add(stats);

                stats.setVerticalTextPosition(SwingConstants.BOTTOM);
                stats.setHorizontalTextPosition(SwingConstants.CENTER);
                stats.setIcon(new ImageIcon(renderer.renderWithGridAndNumberNotation(new ArrayList<>(), grid)));
                String repor = "All songs you listened during the span from " + old + " to " + nu +
                        ".\nEach vertical line represents roughly one month. \nEach horizontal block surpassed by the red line represents a sum of "
                        + songNumber +
                        " individual songs listened." +
                        "\nThe blue line represents the sum of minutes listened, each gray block it surpasses ammount to "
                        + songNumber + " minutes listened.";
                repor = HTMLize(repor);
                stats.setText(repor);
                pack();
                invalidate();
            });


        });
        specificArtistGraphs.addActionListener(e -> {
            List<String> artists = songHistory.getArtists();
            String arr[] = new String[artists.size()];
            Collections.sort(artists);
            artists.toArray(arr);
            StringSelectionWindow artistSelectionWindow = new StringSelectionWindow("Select the artists that you wish to visually represent:", arr);
            artistSelectionWindow.confirmationButton.addActionListener(e1 -> {
                List<String> selected = artistSelectionWindow.getSelected();
                List<SongHistory> histories = new ArrayList<>();
                SongHistory global = new SongHistory();
                List<List<double[]>> data = new ArrayList<>();
                for (int i = 0; i < selected.size(); i++) {
                    String ac = selected.get(i);
                    SongHistory songsFromArtist = songHistory.getSongsFromArtist(ac);
                    histories.add(songsFromArtist);
                    global.loadSongs(songsFromArtist);
                }
                artistSelectionWindow.dispose();
                TimeSpanSelector selector = new TimeSpanSelector();
                selector.addConclusionTask(() -> {

                    double wideInSecs = selector.getTimeInSeconds();
                    selector.dispose();

                    Date old = songHistory.getOldest().getEndTime();
                    Date nu = songHistory.getNewest().getEndTime();

                    Color doubleColors[] = new Color[Misc.PREFERRED_COLORS.length*2];

                    for (int i = 0; i < Misc.PREFERRED_COLORS.length; i+=2) {
                        Color act = Misc.PREFERRED_COLORS[i];
                        doubleColors[i] = act;
                        int r = act.getRed();
                        int g = act.getGreen();
                        int b = act.getBlue();
                        //System.out.println(r + " " + g + " " + b);
                        r*=0.5;
                        g*=0.5;
                        b*=0.5;
                        //System.out.println(r + " " + g + " " + b);
                        //System.out.println();
                        doubleColors[i+1]=new Color(r,g,b);
                    }

                    for (int i = 0; i < histories.size(); i++) {
                        data.addAll(genRendererDataBasedOnTimeSpan(histories.get(i),wideInSecs));
                    }
                    JLabel stats = new JLabel();
                    statusCanvas.removeAll();
                    statusCanvas.add(stats);

                    System.out.println(doubleColors.length + " colors");
                    System.out.println(data.size() + " lines");

                    GraphRenderer renderer = new GraphRenderer(data, new Dimension(600,600), doubleColors);
                    stats.setIcon(new ImageIcon(renderer.renderWithGridAndNumberNotation(new ArrayList<>(),new double[]{100,365})));//fixme
                    stats.setText("All data from the selected artists between " + old + " and " + nu);
                });
                });

        });


        statusCanvas.add(new JLabel("Welcome :), no songs currently loaded"));

        visuals.add(specificArtistGraphs);
        visuals.add(specificSongGraphics);
        visuals.add(generateFromOverallListeningTime);

        importer.add(fromEndSong);
        importer.add(fromUserHistory);
        importer.add(clearSharedSongList);

        jMenuBar.add(importer);
        jMenuBar.add(visuals);

        BorderLayout layout = new BorderLayout(3,3);
        //layout.setAlignment(FlowLayout.CENTER);
        setLayout(layout);

        setJMenuBar(jMenuBar);
        add(statusCanvas);//, JFrame.CENTER_ALIGNMENT);

        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public double inputDouble(String message) {
        return Double.parseDouble(JOptionPane.showInputDialog(message));
    }

    public void refreshSongStats() {
        statusCanvas.removeAll();
        statusCanvas.add(new JLabel(generateStatReport(true)));
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
            return ("1 song currently loaded");
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
    public static List<List<double[]>> genRendererDataBasedOnTimeSpan(SongHistory songHistory, double loopFrequencyInSeconds){
        ArrayList l = new ArrayList();
        l.add(songHistory);
        return genRendererDataBasedOnTimeSpan(l,loopFrequencyInSeconds);
    }
    public static List<List<double[]>> genRendererDataBasedOnTimeSpan(List<SongHistory> songHistories, double loopFrequencyInSeconds){

        Date old = songHistories.get(0).getOldest().getEndTime();
        Date nu = songHistories.get(0).getNewest().getEndTime();

        for (int i = 1; i < songHistories.size(); i++) {
            //gets the oldest and newest songs from all songhistories and selects the oldest oldest, and newest newest respecively
            //thanks CGPGray for making the above sentence acceptable
            Date oldestEndTime = songHistories.get(i).getOldest().getEndTime();
            Date newestEndTime = songHistories.get(i).getNewest().getEndTime();
            if(old.after(oldestEndTime)){
                old = oldestEndTime;
            }
            if(nu.before(newestEndTime)){
                nu = newestEndTime;
            }
        }

        int dateItnerations = 0;

        List<List<double[]>> overallData = new ArrayList();



        for (int l = 0; l < songHistories.size(); l++) {
            SongHistory songHistory = songHistories.get(l);
            ArrayList<double[]> individualListeiningSessionsData = new ArrayList();
            ArrayList<double[]> minutesListenedData = new ArrayList();
            overallData.add(individualListeiningSessionsData);
            overallData.add(minutesListenedData);
            for (Date dateAct = (Date) old.clone(); dateAct.before(nu);
                //goes checking each day if wideness = 1;
                 dateAct = Date.from(Instant.ofEpochSecond((long) (dateAct.toInstant().getEpochSecond() + loopFrequencyInSeconds)))) {
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
                individualListeiningSessionsData.add(new double[]{dateItnerations, ls});
                minutesListenedData.add(new double[]{dateItnerations, msls / 1000 / 60});

                dateItnerations++;
            }
        }
        System.out.println(overallData.size() + " lines overall");
        return overallData;
    }

}
