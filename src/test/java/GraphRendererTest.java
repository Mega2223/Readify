import net.Mega2223.utils.ImageTools;
import net.Mega2223.utils.objects.GraphRenderer;
import net.mega2223.readify.util.Misc;
import net.mega2223.readify.windows.ApplicationWindow;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GraphRendererTest {
    public static void main(String[] args) {

        List<double[]> db = new ArrayList<>();

        List<List<double[]>> d = new ArrayList<>();
        d.add(db);

        db.add(new double[]{0,2});
        db.add(new double[]{1,3});
        db.add(new double[]{2,6});
        db.add(new double[]{3,4});
        db.add(new double[]{4,1});

        GraphRenderer graphRenderer = new GraphRenderer(d,new Dimension(30,10), Misc.PREFERRED_COLORS);

        JFrame frame = new JFrame();
        frame.setSize(100,100);
        JLabel label = new JLabel();
        frame.add(label);
        label.setIcon(new ImageIcon(ImageTools.getScaledGraph(new Dimension(100,100),1,graphRenderer)));
        frame.setVisible(true);
    }
}
