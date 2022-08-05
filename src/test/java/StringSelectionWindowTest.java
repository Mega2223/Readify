import net.mega2223.readify.windows.StringSelectionWindow;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Arrays;

public class StringSelectionWindowTest {
    public static void main(String[] args) {
        String eba[] = {"t1","t2","t2t2"};
        StringSelectionWindow window = new StringSelectionWindow("SELECIONE",eba);
        JCheckBox[] boxes = window.boxes;
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    String dbg = "";
                    boolean[] results = window.getResults();
                    for (int j = 0; j < results.length; j++) {
                        dbg += results[j] + ",";
                    }
                    System.out.println(dbg);
                }
            });
        }
    }
}
