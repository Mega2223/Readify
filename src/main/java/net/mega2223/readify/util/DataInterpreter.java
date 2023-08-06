package net.mega2223.readify.util;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** This class is a bunch of static methods intended to be a middle step between the frontend of the application and the AguaLib renderer.
 * It compiles SongHistory objects in graph data based on the needs of the application, then returns said data to be made into a graph by the main process.
 * */
public class DataInterpreter {

    private DataInterpreter(){}
    public static double[][][] genTotalTimeListenedData(SongHistory history, long songIntervalMilis){
        Track latest = history.getLatest();
        Track oldest = history.getOldest();
        long oldestTime = oldest.getEndTime().getTime();
        long latestTime = latest.getEndTime().getTime();
        //estimated array lenght
        int EAL = (int) (Math.abs(latestTime - oldestTime)/songIntervalMilis) + 1;
        double[][][] ret = new double[1][EAL][];

        int iT = 0;

        for (long i = oldestTime; i < latestTime; i+=songIntervalMilis) {
            List<Track> listens = history.getAllSongsInRange(i,songIntervalMilis/2);
            long timeListened = 0;
            for (Track ac : listens){
                timeListened += ac.getMsPlayed();
            }
            ret[0][iT] = new double[]{i,timeListened};
            iT++;
        }
        preventNullValues(ret);
        return ret;
    }

    public static double[][][] genFromASetOfArtists(SongHistory history, List<String> artists, long songIntervalMilis){
        List<SongHistory> histories = new ArrayList<>();
        for (String ac : artists){
            histories.add(history.getSongsFromArtist(ac));
        }
        Date[] bounds = getOldestAndLatestDates(histories);
        int EAL = (int) (Math.abs(bounds[0].getTime() - bounds[1].getTime())/songIntervalMilis);
        double[][][] ret = new double[artists.size()][EAL+1][];
        int c = 0;
        for (long i = bounds[0].getTime(); i < bounds[1].getTime(); i += songIntervalMilis){
            for (int h = 0; h < histories.size(); h++){
                SongHistory act = histories.get(h);
                List<Track> listens = act.getAllSongsInRange(i,songIntervalMilis/2);
                long timeListened = 0;
                for(Track ac : listens){
                    timeListened += ac.getMsPlayed();
                }
                ret[h][c] = new double[]{i,timeListened};
            }
            c++;
        }
        preventNullValues(ret);
        return ret;
    }

    public static double[][][] genFromASetOfSongs(SongHistory history, List<String[]> songs, long songIntervalMilis){
        List<SongHistory> histories = new ArrayList<>(songs.size());
        for(int i = 0; i < songs.size(); i++){
            String[] song = songs.get(i);
            histories.add(new SongHistory(history.getListensForThisSong(song[1],song[0])));
        }
        Date[] bounds = getOldestAndLatestDates(histories);
        int EAL = (int) (Math.abs(bounds[0].getTime() - bounds[1].getTime())/songIntervalMilis);
        double[][][] ret = new double[histories.size()][EAL+1][];
        int c = 0;
        for (long i = bounds[0].getTime(); i < bounds[1].getTime(); i += songIntervalMilis){
            for (int h = 0; h < histories.size(); h++){
                SongHistory act = histories.get(h);
                List<Track> listens = act.getAllSongsInRange(i,songIntervalMilis/2);
                long timeListened = 0;
                for(Track ac : listens){
                    timeListened += ac.getMsPlayed();
                }
                ret[h][c] = new double[]{i,timeListened};
            }
            c++;
        }
        preventNullValues(ret);
        return ret;
    }

    protected static final int X_MIN = 0, X_MAX = 1, Y_MIN = 2, Y_MAX = 3;
    public static double[] getDataBounds(double[][][] data){
        double[] ret = {data[0][0][0],data[0][0][0],data[0][0][1],data[0][0][1]};
        for (double[][] l : data) {
            for (int p = 0; p < l.length; p++) {
                ret[X_MIN] = Math.min(l[p][0], ret[0]);
                ret[X_MAX] = Math.max(l[p][0], ret[1]);
                ret[Y_MIN] = Math.min(l[p][1], ret[2]);
                ret[Y_MAX] = Math.max(l[p][1], ret[3]);
            }
        }
        return ret;
    }
    public static double[][][] normalizeXAxis(double[][][] data){
        double[][][] dataClone = data.clone();
        //clones each array of data,
        //they need each to be new objects otherwise changes in dataClone may affect data
        for (int i = 0; i < data.length; i++) {
            dataClone[i] = data[i].clone();
            for (int j = 0; j < data[i].length; j++) {
                dataClone[i][j] = data[i][j].clone();
            }
        }
        double[] bounds = getDataBounds(dataClone);
        for (int l = 0; l < dataClone.length; l++) {
            for (int p = 0; p < dataClone[l].length; p++) {
                dataClone[l][p][0]-=bounds[X_MIN];
            }
        }
        return dataClone;
    }
    public static void debugData(double[][][] data){
        System.out.println("DATA OBJECT: \n\n");
        for (int i = 0; i < data.length; i++) {
            System.out.print("LINE " + i + ": \n");
            for (int j = 0; j < data[i].length; j++) {
                System.out.print("P" + j + ": [" + data[i][j][0] + "\\" + data[i][j][1] + "] ");
            }
        }
    }
    public static Date[] getOldestAndLatestDates(List<SongHistory> histories){
        Date old = histories.get(0).getOldest().getEndTime();
        Date newe = histories.get(0).getLatest().getEndTime();
        for (int i = 1; i < histories.size(); i++) {
            Date acO = histories.get(i).getOldest().getEndTime();
            Date acL = histories.get(i).getLatest().getEndTime();
            if(old.after(acO)){old = acO;}
            if(newe.before(acL)){newe = acL;}
        }
        return new Date[]{old,newe};
    }
    private static void preventNullValues(double[][][] data){
        for (int l = 0; l < data.length; l++) {
            double[] prev = data[l][0];
            for (int p = 0; p < data.length; p++) {
                if(data[l][p] == null){data[l][p] = prev;}
                prev = data[l][p];
            }
        }
    }
}
