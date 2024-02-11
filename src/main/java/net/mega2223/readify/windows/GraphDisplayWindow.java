package net.mega2223.readify.windows;

import net.mega2223.readify.util.Misc;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;

public class GraphDisplayWindow extends JFrame { //todo, or maybe don't??
    BufferedImage graph;
    JPanel mainFrame = new JPanel(); //thanks boxlayout
    JPanel imagePanel = new JPanel();
    JPanel captions = new JPanel();

    GraphDisplayWindow(String informationReport, List<String> labels, List<Color> colors, BufferedImage graph){
        this.graph = graph;
        mainFrame.setLayout(new BoxLayout(mainFrame,BoxLayout.Y_AXIS));
        captions.setLayout(new GridLayout(0,Math.min(labels.size(),3)));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        add(new JLabel(informationReport));
        add(mainFrame);
        JLabel imageLabel = new JLabel(new ImageIcon(graph));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalTextPosition(SwingConstants.CENTER);
        imagePanel.setLayout(new FlowLayout()); imagePanel.add(imageLabel);
        mainFrame.add(imagePanel);
        mainFrame.add(captions);
        for (int i = 0; i < labels.size(); i++) {
            Color act = colors.get(i);
            JLabel timeListened = new JLabel(labels.get(i));
            timeListened.setIcon(new ImageIcon(Misc.generateMonochromaticImage(10, 10, act)));
            timeListened.setHorizontalAlignment(SwingConstants.CENTER);
            timeListened.setVerticalTextPosition(SwingConstants.CENTER);
            captions.add(timeListened);
        }
        pack();
        setSize(getWidth()-60,getHeight()+20);
        setTitle("Graph Display");
        setName(getTitle());

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
