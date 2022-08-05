import net.mega2223.readify.util.JsonConverter;

import java.io.*;
import java.text.ParseException;

public class JSonConverterTest {

    public static void main(String[] args) throws IOException, ParseException {

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Imperiums\\Desktop\\MyData\\StreamingHistory0.json"));

        String nextLine = reader.readLine();
        String sampleString = "";

        while (nextLine != null){
            sampleString += nextLine + "\n";
            nextLine = reader.readLine();
        }

        System.out.println(JsonConverter.convertFromUserHistory(sampleString));


    }

}
