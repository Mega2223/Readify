package net.mega2223.readify.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Misc {

    public static final Color PREFERRED_COLORS[] = {Color.BLUE,Color.RED,Color.GREEN,Color.CYAN,Color.MAGENTA,Color.YELLOW,Color.ORANGE,Color.DARK_GRAY,Color.PINK};

    public static Color[] genColorArray(int amount){
        Color ret[] = new Color[amount];
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            ret[i] = new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256));
        }
        return ret;
    }
    public static List<Color> genColorList(int amount){
        Random random = new Random();
        ArrayList<Color> ret = new ArrayList();
        for (int i = 0; i < amount; i++) {
            Color c = new Color(random.nextInt(256),random.nextInt(256),random.nextInt(256));
            ret.add(c);
        }
        return ret;
    }
    public static String readFromFile(File file) throws IOException {return readFromFile(file,null);}

    public static String readFromFile(File file, Runnable task) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String nextLine = reader.readLine();
        StringBuilder sampleString = new StringBuilder();

        while (nextLine != null){
            sampleString.append(nextLine).append("\n");
            nextLine = reader.readLine();
            if(task!=null){task.run();}
        }

        return sampleString.toString();
    }
    public static String adaptDate(String previous){
        if(previous.contains("T")){return adaptDateEndSong(previous);}
        return adaptDateStreamingHistory(previous);
    }

    static String adaptDateStreamingHistory(String previous){
        //2022-04-26 11:05 -> 26/04/2022 10:05
        String pa1[] = previous.split(" ");
        String pa2[] = pa1[0].split("-");
        return pa2[2] + "/" + pa2[1] + "/" + pa2[0] + " " + pa1[1];
    }
    static String adaptDateEndSong(String previous){
        //2019-06-11T12:30:46Z -> 11/06/2019 12:30
        String pa1[] = previous.split("T");
        String pa11[] = pa1[0].split("-");
        String pa12[] = pa1[1].split(":");


        return pa11[2] + "/" + pa11[1] + "/" + pa11[0] + " " + pa12[0] + ":" + pa12[1];

    }

    public static BufferedImage generateMonochromaticImage(int sX, int sY, Color color){
        BufferedImage image = new BufferedImage(sX,sY,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics = image.getGraphics();
        graphics.setColor(color);
        graphics.fillRect(0,0,sX,sY);
        return image;
    }

    public static void debugGraph(List<List<double[]>> data){
        System.out.println("GRAPH_DEBUG: ");
        for (int i = 0; i < data.size(); i++) {
            debugGraphBar(data.get(i));
        }
        System.out.println();
    }
    public static void debugGraphBar(List<double[]> data){
        String debug = "[";
        for (int i = 0; i < data.size(); i++) {
            double[] point = data.get(i);
            debug += "(" +point[0] + "," + point[1] + ")";
        }
        debug += "]";
        System.out.println(debug);
    }

    public static String HTMLize(String what) {
        String ret = "<html><body>" + what + "</body></html>";
        ret = ret.replace("\n", "<br>");
        return ret;
    }

    public static void debugGraphData(List<List<double[]>> graphData){

        System.out.println("DATA DEBUG:");

        for (int i = 0; i < graphData.size(); i++) {
            List<double[]> act = graphData.get(i);
            String out = i + ": [";
            for (int j = 0; j < act.size(); j++) {
                double[] actAct = act.get(j);
                out += "{" + actAct[0] + ";" + actAct[1] + "}/";
            }
            out += "]";
            System.out.println(out+"\n");
        }
    }

}
