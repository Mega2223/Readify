import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ParsingTest {

    public static void main(String[] args) throws ParseException {
        System.out.println(new SimpleDateFormat().parse("07/08/2022 4:5 PM, PDT"));
        System.out.println(new SimpleDateFormat().parse("2022-02-08 12:41"));
        //2022-04-26 11:05 -> 26/04/2022 12:41
    }

}
