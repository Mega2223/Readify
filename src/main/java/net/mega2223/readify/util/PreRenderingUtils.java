package net.mega2223.readify.util;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PreRenderingUtils {
    private PreRenderingUtils() {}

    public static List<Date> genDates(Date first, Date last, long stepInMilis){
        ArrayList<Date> ret = new ArrayList<>();
        for (long i = first.getTime(); i < last.getTime(); i+= stepInMilis) {
            ret.add(Date.from(Instant.ofEpochMilli(i)));
        }
        return ret;
    }

    public static List<String>[] genSubsForGraph(double[][][] data, double[] stepFraction, List<Date> dates) {
        List<String>[] ret = new List[2];
        ret[0] = new ArrayList<>();
        ret[1] = new ArrayList<>();

        double miX = data[0][0][0], maX = data[0][0][0], miY = data[0][0][1], maY = data[0][0][1];
        for (int l = 0; l < data.length; l++) {
            for (int p = 0; p < data[l].length; p++) {
                miX = Math.min(miX, data[l][p][0]);
                maX = Math.max(maX, data[l][p][0]);
                miY = Math.min(miY, data[l][p][1]);
                maY = Math.max(maY, data[l][p][1]);
            }
        }
        double[] step = {Math.abs(maX-miX)*stepFraction[0],Math.abs(maY-miY)*stepFraction[1]};

        int c = 0;
        for (double i = miX; i <= maX; i += step[0]) {
            Date ac = dates.get(c % dates.size());
            ret[0].add((ac.getDay()+1) + "/" + (ac.getMonth()+1) + "/" + (ac.getYear() + 1900));
            c++;
        }
        c = 0;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(2);
        for (double i = miY; i <= maY; i += step[1]) {
            ret[1].add(format.format(c));
            c++;
        }

        return ret;
    }
}
