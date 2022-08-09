import net.mega2223.readify.windows.ApplicationWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ScrollTest extends JFrame {

    public ScrollTest(){

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Random r = new Random();

        int[][] cool = new int[100][100];
        String big = "";
        for (int i = 0; i < cool.length; i++) {
            int[] act = cool[i];
            for (int j = 0; j < act.length; j++) {
                cool[i][j] = (int) ((r.nextDouble()-0.5)*100);
                String add = String.valueOf(cool[i][j]);
                while (add.length() < 4){add = "0"+add;}
                add +=  " | ";
                big += add;
            }
            big += "\n";
        }

        JLabel label = new JLabel(ApplicationWindow.HTMLize(big));
        label.setFont(Font.decode("Consolas"));

        JPanel panel = new JPanel();
        panel.setSize(30,30);
        panel.add(label);
        JScrollPane pane = new JScrollPane(panel);

        add(pane);

        setVisible(true);
        pack();
    }

    public static void main(String[] args) {
        ScrollTest act = new ScrollTest();
    }

}
