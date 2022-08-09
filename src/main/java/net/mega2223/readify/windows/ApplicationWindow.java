package net.mega2223.readify.windows;

import net.Mega2223.utils.objects.GraphRenderer;
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
import java.util.Date;
import java.util.List;

import static net.mega2223.readify.objects.SongHistory.isInDateRange;
import static net.mega2223.readify.util.Misc.HTMLize;

public class ApplicationWindow extends JFrame {

    //TODO: different states for if we are using songs from a playlist or from a user history or perhaps both, along with global variables
    //such is needed because playlists and user histories display different info

    public static final int[] DEFAULT_GRAPH_DIMENSIONS = {300,300};

    public SongHistory sharedSongHistory = new SongHistory();
    public JPanel statusCanvas = new JPanel();
    public JMenuBar jMenuBar = new JMenuBar();
     public JMenu importer = new JMenu("Import");
      public JMenuItem fromUserHistory = new JMenuItem("From user history");
      public JMenuItem fromPlaylist = new JMenuItem("From playlist");
      public JMenuItem clearSharedSongList = new JMenuItem("Clear songs");
     public JMenu visuals = new JMenu("Visuals");
      public JMenuItem specificArtistGraphics = new JMenuItem("Generate graphic from a set of artists");
      public JMenuItem specificSongGraphics = new JMenuItem("Generate graphic from a specific set of songs");
      public JMenuItem generateFromOverallListeningTime = new JMenuItem("Generate graphic from overall listening time");

    public ApplicationWindow(int sX, int sY) {

        fromUserHistory.addActionListener(e -> {
            File[] files = importFiles();
            int songEstimate = files.length * 10000;
            int fileSizeEstimate = songEstimate * 6;
            System.out.println("generating estimate");
            for (int i = 0; i < files.length; i++) {
                //generates song estimate based on size of all arrays from all selected files
                File act = files[i];
                /* this is too slow lol
                try {
                    songEstimate += JsonParser.parseString(Misc.readFromFile(act)).getAsJsonArray().size();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }*/
            }
            System.out.println("estimate generated");
            JLabel stats = new JLabel();
            statusCanvas.removeAll();
            statusCanvas.add(stats);
            JLabel updateStatus = new JLabel("Loading songs...");
            JProgressBar progressBar = new JProgressBar(0, fileSizeEstimate);
            JPanel panel = new JPanel(new GridLayout(1, 4));
            panel.add(updateStatus);
            panel.add(progressBar);

            final int[] counts = {0, 0};
            Thread loadingThread = new Thread(() -> {
                for (int i = 0; i < files.length; i++) {
                    File act = files[i];
                    try {
                        stats.setText("Loading file " + act.getName() + "...");
                        String fileData = Misc.readFromFile(act, () -> {
                            progressBar.setValue(counts[1]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[1] / (double) fileSizeEstimate * 100) + "%");
                            counts[1]++;
                        });
                        stats.setText("Compiling songs...");
                        SongHistory tracks = JsonConverter.convertFromUserHistory(fileData, () -> {
                            progressBar.setValue(counts[0]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[0] / (double) songEstimate * 100) + "%");
                            counts[0]++;
                        }, () -> {
                            statusCanvas.removeAll();
                        });
                        sharedSongHistory.loadSongs(tracks);
                    } catch (IOException | ParseException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ApplicationWindow.this, "Something happened :(, see the log for more details");
                    }
                }
                remove(panel);

                stats.setText(" ");
                refreshSongStats();
            });
            add(panel);
            loadingThread.start();
        });
        clearSharedSongList.addActionListener(e -> {
            sharedSongHistory.clearSongs();
            refreshSongStats();
        });
        generateFromOverallListeningTime.addActionListener(e -> {

            List<double[]> individualListeiningSessionsData = new ArrayList<>();
            List<double[]> milisListenedData = new ArrayList<>();

            double wideness = inputDouble("How wide should the day tolerance be? (Lower values make the curve sharper)");
            Date old = sharedSongHistory.getOldest().getEndTime();
            Date nu = sharedSongHistory.getNewest().getEndTime();
            int days = 0;
            for (Date dateAct = (Date) old.clone(); dateAct.before(nu);
                //goes checking each day if wideness = 1;
                 dateAct = Date.from(Instant.ofEpochSecond((long) (dateAct.toInstant().getEpochSecond() + 86400 * wideness)))) {
                //measurement of individual listens
                int ls = 0;
                //measurement of milis listened
                long msls = 0;
                List<Track> listens = sharedSongHistory.getListens();
                for (int i = 0; i < listens.size(); i++) {
                    Track act = listens.get(i);
                    if (isInDateRange(act.getEndTime(), dateAct, wideness)) {
                        ls++;
                        msls += act.getMsPlayed();
                    }
                }
                individualListeiningSessionsData.add(new double[]{days, ls});
                milisListenedData.add(new double[]{days, msls / 1000 / 60});
                days++;
            }

            ArrayList list = new ArrayList();
            list.add(individualListeiningSessionsData);
            list.add(milisListenedData);

            GraphRenderer renderer = new GraphRenderer(list, new Dimension(DEFAULT_GRAPH_DIMENSIONS[0], DEFAULT_GRAPH_DIMENSIONS[1]), Misc.PREFERRED_COLORS);

            double songNumber = 1000;

            double[] grid = {30.437, 10};

            statusCanvas.removeAll();
            JLabel stats = new JLabel();
            statusCanvas.add(stats);

            stats.setIcon(new ImageIcon(renderer.renderWithGrid(new ArrayList<>(), grid)));
            String repor = "All songs you listened during the span from " + old + " to " + nu +
                    ".\nEach vertical line represents roughly one month. \nEach horizontal block surpassed by the red line represents a sum of "
                    + songNumber +
                    " individual songs listened." +
                    "\nThe blue line represents the sum of minutes listened, each gray block it surpasses ammount to "
                    + songNumber + " minutes listened.";
            repor = HTMLize(repor);
            stats.setText(repor);
        });
        specificArtistGraphics.addActionListener(e -> {
            List<String> artists = sharedSongHistory.getArtists();
            String arr[] = new String[artists.size()];
            artists.toArray(arr);
            StringSelectionWindow artistSelectionWindow = new StringSelectionWindow("Select the artists that you wish to visually represent:", arr);
            artistSelectionWindow.confirmationButton.addActionListener(e1 -> {
                List<String> selected = artistSelectionWindow.getSelected();
                List<SongHistory> histories = new ArrayList<>();
                SongHistory global = new SongHistory();
                List<List<double[]>> data = new ArrayList<>();
                for (int i = 0; i < selected.size(); i++) {
                    String ac = selected.get(i);
                    SongHistory songsFromArtist = sharedSongHistory.getSongsFromArtist(ac);
                    histories.add(songsFromArtist);
                    global.loadSongs(songsFromArtist);
                }
                artistSelectionWindow.dispose();
                double wideness = inputDouble("How often should we check for songs? (Larger values mean a smoother curve, in case of doubt just input 1)");

                Date old = sharedSongHistory.getOldest().getEndTime();
                Date nu = sharedSongHistory.getNewest().getEndTime();
                int days = 0;

                for (int i = 0; i < artists.size(); i++) {
                    data.add(new ArrayList<>());
                }
                JLabel stats = new JLabel();
                statusCanvas.removeAll();
                statusCanvas.add(stats);
                //goes from a specific set of days periodically checking for each artist if they had songs played
                //in that period of time, and if they do they put a point in a score, wich is later evaluated and
                //put in the graph
                for (Date dateAct = (Date) old.clone(); dateAct.before(nu);
                     dateAct = Date.from(Instant.ofEpochSecond((long) (dateAct.toInstant().getEpochSecond() + 86400 * wideness)))) {

                    List<Track> close = global.getCloseToDate(dateAct, wideness);
                    for (int i = 0; i < artists.size(); i++) {
                        String act = artists.get(i);
                        int artistPonct = 0;
                        for (int j = 0; j < close.size(); j++) {
                            if (close.get(j).getArtistName().equals(act)) {
                                artistPonct++;
                            }
                        }
                        data.get(i).add(new double[]{days,artistPonct});
                    }
                    days++;
                }
                GraphRenderer renderer = new GraphRenderer(data, new Dimension(600,600),Misc.PREFERRED_COLORS);
                stats.setIcon(new ImageIcon(renderer.renderWithGrid(new ArrayList<>(),new double[]{100,365})));//fixme
            });

        });

        statusCanvas.add(new JLabel("Welcome :), no songs currently loaded"));

        visuals.add(specificArtistGraphics);
        visuals.add(specificSongGraphics);
        visuals.add(generateFromOverallListeningTime);

        importer.add(fromPlaylist);
        importer.add(fromUserHistory);
        importer.add(clearSharedSongList);

        jMenuBar.add(importer);
        jMenuBar.add(visuals);

        GridLayout layout = new GridLayout(1,1);
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
    }

    public String generateStatReport(boolean html) {
        return generateStatReport(html, "");
    }

    public String generateStatReport(boolean html, String embed) {
        List<Track> songs = sharedSongHistory.getListens();
        if (songs.size() == 0) {
            return ("No songs loaded");
        } else if (songs.size() == 1) {
            return ("1 song currently loaded");
        } else {
            List<Track> individualSongs = sharedSongHistory.getSongs();
            List<String> individualArtists = sharedSongHistory.getArtists();

            int totalTimeListenedSeconds = sharedSongHistory.getTotalTimeListenedSeconds();
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
