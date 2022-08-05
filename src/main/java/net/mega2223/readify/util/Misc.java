package net.mega2223.readify.util;

import java.io.*;

public class Misc {

    public static String readFromFile(File file) throws IOException {return readFromFile(file,null);}

    public static String readFromFile(File file, Runnable task) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String nextLine = reader.readLine();
        String sampleString = "";

        while (nextLine != null){
            sampleString += nextLine + "\n";
            nextLine = reader.readLine();
            if(task!=null){task.run();}
        }

        return sampleString;
    }
    public static String adaptDate(String previous){
        //2022-04-26 11:05 -> 26/04/2022 12:41
        String pa1[] = previous.split(" ");
        String pa2[] = pa1[0].split("-");

        return pa2[2] + "/" + pa2[1] + "/" + pa2[0] + " " + pa1[1];

    }

}
