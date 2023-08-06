package net.mega2223.readify.util;

import net.mega2223.utils.objects.GraphBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import static net.mega2223.readify.util.DataInterpreter.*;
import static net.mega2223.readify.util.Misc.genColorList;

public class GraphGenerator {
    private GraphGenerator(){}
    public static BufferedImage genFullGraphFromData(double[][][] data, int[] numberOfLines, Date oldest, Date latest, Font font, int sX, int sY, List<Color> colors){
        long dateRange = Math.abs(oldest.getTime()-latest.getTime());
        BufferedImage graph = GraphBuilder.buildPureGraph(data,colors,sX,sY);
        //List<Date> datesToSub = PreRenderingUtils.genDates(oldest, latest, dateRange / numberOfLines[0]);
        double[] dataBounds = getDataBounds(data);
        double[] interval = {
                Math.abs(dataBounds[X_MIN] - dataBounds[X_MAX])/numberOfLines[0], Math.abs(dataBounds[Y_MIN] - dataBounds[Y_MAX])/numberOfLines[1]
        };
        GraphBuilder.buildAuxiliarLines(graph, data, Color.gray, interval);
        return graph;
    }

    public static BufferedImage genFullGraphAndCaptionsFromData(double [][][] data, int[] numberOfLines, Date oldest, Date latest, Font font, int sX, int sY, List<Color> colors){
        long dateRange = Math.abs(oldest.getTime()-latest.getTime());
        List<Date> datesToSub = PreRenderingUtils.genDates(oldest, latest, dateRange / numberOfLines[0]);
        double[] dataBounds = getDataBounds(data);
        double[] interval = {
                Math.abs(dataBounds[X_MIN] - dataBounds[X_MAX])/numberOfLines[0], Math.abs(dataBounds[Y_MIN] - dataBounds[Y_MAX])/numberOfLines[1]
        };
        List<String>[] subs = PreRenderingUtils.genSubsForGraph(data,new double[]{.1,.1}, datesToSub);

        return GraphBuilder.generateGraphAndSubs(data,colors,sX,sY,interval,font,GraphBuilder.DIRECTION_LEFT,GraphBuilder.DIRECTION_DOWN,subs[0],subs[1],sX/8,Color.gray,Color.black);
    }
}
