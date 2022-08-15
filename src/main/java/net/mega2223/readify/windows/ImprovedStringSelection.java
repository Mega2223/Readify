package net.mega2223.readify.windows;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImprovedStringSelection extends JFrame {
    List<Runnable> conclustionTasks = new ArrayList<>();
    String[] selectors;
    ArrayList<String> selected = new ArrayList<>();
    ArrayList<String> notSelected = new ArrayList<>();

    JList inputSelector;
    JPanel buttonPanel = new JPanel(new BorderLayout());
    JPanel middlePanel = new JPanel(new FlowLayout());
    JButton rightArrow = new JButton("->");
    JButton leftArrow = new JButton("<-");
    JList includedSelector;

    public ImprovedStringSelection(String[] selectors){
        this.selectors = selectors;
        inputSelector = new JList(selectors);
        includedSelector = new JList();

        GridBagLayout manager = new GridBagLayout();
        setLayout(manager);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        rightArrow.setMaximumSize(new Dimension(10,10));
        leftArrow.setMaximumSize(new Dimension(10,10));

        rightArrow.setFont(Font.decode("Consolas"));
        leftArrow.setFont(Font.decode("Consolas"));

        buttonPanel.add(rightArrow,BorderLayout.NORTH);
        buttonPanel.add(leftArrow,BorderLayout.SOUTH);

        middlePanel.add(buttonPanel);
        middlePanel.setMaximumSize(new Dimension(200,100));
        buttonPanel.setMaximumSize(new Dimension(200,100));

        add(inputSelector);
        add(middlePanel);
        add(includedSelector);

        setSize(300,150);
        setVisible(true);
    }


    public List<String> getSelected(){
        return selected;
    }

    public void addConclusionTask(Runnable task){conclustionTasks.add(task);}

}
