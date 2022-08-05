package net.mega2223.readify.windows;

import com.google.gson.JsonParser;
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

public class ApplicationWindow extends JFrame {

    //TODO: different states for if we are using songs from a playlist or from a user history or perhaps both, along with global variables
    //such is needed because playlists and user histories display different info

    public static final Color[] PREFERED_COLORS = {Color.RED, Color.BLUE};
    public SongHistory sharedSongHistory = new SongHistory();
    public JLabel statsReport = new JLabel(generateStatReport(true));
    public JLabel stats = new JLabel();
    public JLabel statsInfo = new JLabel();
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
            int songEstimate = files.length*10000;
            int fileSizeEstimate = songEstimate * 6;
            System.out.println("generating estimate");
            for (int i = 0; i < files.length; i++) {
                //generates song estimate based on size of all arrays from all selected files
                File act = files[i];


                /* this is too slow loltry {
                    songEstimate += JsonParser.parseString(Misc.readFromFile(act)).getAsJsonArray().size();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }*/
            }
            System.out.println("estimate generated");

            JLabel updateStatus = new JLabel("Loading songs...");
            JProgressBar progressBar = new JProgressBar(0,fileSizeEstimate);
            final int[] counts = {0,0};
            Thread loadingThread = new Thread(() -> {
                for (int i = 0; i < files.length; i++) {
                    File act = files[i];
                    try {
                        stats.setText("Loading files...");
                        String fileData = Misc.readFromFile(act, () -> {
                            progressBar.setValue(counts[1]);
                            updateStatus.setText("Estimated progress: " + (int)((double)counts[1]/(double)fileSizeEstimate*100) + "%");
                            counts[1]++;
                        });
                        stats.setText("Compiling songs...");
                        SongHistory tracks = JsonConverter.convertFromUserHistory(fileData, () -> {
                            progressBar.setValue(counts[0]);
                            updateStatus.setText("Estimated progress: " + (int) ((double) counts[0] / (double) songEstimate * 100) + "%");
                            counts[0]++;
                        }, () -> {
                            remove(progressBar);
                            remove(updateStatus);
                            stats.setText(" ");
                        });
                        sharedSongHistory.loadSongs(tracks);
                    } catch (IOException | ParseException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ApplicationWindow.this, "Something happened :(, see the log for more details");
                    }
                    refreshSongStats();
                }
            });

            add(updateStatus);
            add(progressBar);
            loadingThread.start();
            //remove(updateStatus);
            //remove(progressBar);


        });
        clearSharedSongList.addActionListener(e -> sharedSongHistory.clearSongs());
        generateFromOverallListeningTime.addActionListener(e -> {

            List<double[]> individualListeiningSessionsData = new ArrayList<>();
            List<double[]> milisListenedData = new ArrayList<>();

            double wideness = Double.parseDouble(JOptionPane.showInputDialog("How wide should the day tolerance be? (Lower values make the curve sharper)"));

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

            GraphRenderer renderer = new GraphRenderer(list, new Dimension(300, 300), PREFERED_COLORS);

            double songNumber = 1000;

            double[] grid = {30.437, 10};
            stats.setIcon(new ImageIcon(renderer.renderWithGrid(new ArrayList<>(), grid)));
            String repor = "All songs you listened during the span from " + old + " to " + nu +
                    ".\nEach vertical line represents roughly one month. \nEach horizontal block surpassed by the red line represents a sum of "
                    + songNumber +
                    " individual songs listened." +
                    "\nThe blue line represents the sum of minutes listened, each gray block it surpasses ammount to "
                    + songNumber + " minutes listened.";
            repor = HTMLize(repor);
            statsInfo.setText(repor);
        });

        visuals.add(specificArtistGraphics);
        visuals.add(specificSongGraphics);
        visuals.add(generateFromOverallListeningTime);

        importer.add(fromPlaylist);
        importer.add(fromUserHistory);
        importer.add(clearSharedSongList);

        jMenuBar.add(importer);
        jMenuBar.add(visuals);

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.CENTER);
        setLayout(layout);

        setJMenuBar(jMenuBar);
        add(statsReport);//, JFrame.CENTER_ALIGNMENT);
        add(stats);//, JFrame.CENTER_ALIGNMENT);
        add(statsInfo);//, JFrame.CENTER_ALIGNMENT);

        setSize(sX, sY);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    ;

    public static String HTMLize(String what) {
        String ret = "<html><body>" + what + "</body></html>";
        ret = ret.replace("\n", "<br>");
        return ret;
    }

    public void refreshSongStats() {
        statsReport.setText(generateStatReport(true));
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
