package net.mega2223.readify.util;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;
import net.mega2223.utils.objects.GraphBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import static net.mega2223.readify.util.Misc.genColorList;

public class DataInterpreter {

    protected static final int FONT_SIZE = 12;

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
        double sm = ret[0][0][0];

        for (int i = 0; i < ret[0].length; i++) {
            sm = Math.min(sm,ret[0][i][0]);
            double time = ret[0][i][0];
            double timesListened = ret[0][i][1];
            //System.out.println(time + ":" + timesListened);
        }
        //fixme data won't show properly unless this is done, likely a rouding issue in aguaLib


        return ret;
    }

    public static BufferedImage genFullGraphFromData(double[][][] data, int[] numberOfLines, Date oldest, Date latest, Font font, int sX, int sY){
        long dateRange = Math.abs(oldest.getTime()-latest.getTime());
        List<Color> col = genColorList(1);
        BufferedImage graph = GraphBuilder.buildPureGraph(data, col,sX,sY);
        List<Date> datesToSub = PreRenderingUtils.genDates(oldest, latest, dateRange / numberOfLines[0]);
        double[] dataBounds = getDataBounds(data);
        double[] interval = {
                Math.abs(dataBounds[X_MIN] - dataBounds[X_MAX])/numberOfLines[0], Math.abs(dataBounds[Y_MIN] - dataBounds[Y_MAX])/numberOfLines[1]
        };
        GraphBuilder.buildAuxiliarLines(graph, data, Color.gray, interval);
        return graph;
    }

    public static BufferedImage genFullGraphAndCaptionsFromData(double [][][] data, int[] numberOfLines, Date oldest, Date latest, Font font, int sX, int sY){
        long dateRange = Math.abs(oldest.getTime()-latest.getTime());
        List<Color> col = genColorList(1);
        List<Date> datesToSub = PreRenderingUtils.genDates(oldest, latest, dateRange / numberOfLines[0]);
        double[] dataBounds = getDataBounds(data);
        double[] interval = {
                Math.abs(dataBounds[X_MIN] - dataBounds[X_MAX])/numberOfLines[0], Math.abs(dataBounds[Y_MIN] - dataBounds[Y_MAX])/numberOfLines[1]
        };
        List<String>[] subs = PreRenderingUtils.genSubsForGraph(data,new double[]{.1,.1}, datesToSub);

        return GraphBuilder.generateGraphAndSubs(data,col,sX,sY,interval,font,GraphBuilder.DIRECTION_LEFT,GraphBuilder.DIRECTION_DOWN,subs[0],subs[1],sX/8,Color.gray,Color.black);
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
}
