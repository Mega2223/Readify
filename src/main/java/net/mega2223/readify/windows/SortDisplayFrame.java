package net.mega2223.readify.windows;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SortDisplayFrame extends JFrame {
    List<Comparable> values = new ArrayList<>();
    JList<Comparable> display = new JList<>();
    JScrollPane comp;

    SortDisplayFrame(String title){
        new OverlayLayout(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        comp = new JScrollPane(display);
        add(comp);
        setSize(400,600);
        setVisible(true);
        setTitle(title);
    }

    void updateDisplay(){
        display.removeAll();
        Comparable[] ls = new Comparable[values.size()];
        values.toArray(ls);
        display.setListData(ls);
    }

    public void add(Comparable data){values.add(data); updateDisplay();}
    public void add(List<Comparable> data){values.addAll(data); updateDisplay();}
    public void remove(Comparable data){values.remove(data); updateDisplay();}
    public void remove(List<Comparable> data){values.removeAll(data); updateDisplay();}
}