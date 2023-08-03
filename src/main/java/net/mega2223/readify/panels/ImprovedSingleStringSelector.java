package net.mega2223.readify.panels;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImprovedSingleStringSelector extends JPanel {
    JList<Object> selList = new JList();
    JTextField searchBox = null;
    JLabel label = null;

    public Object[] fullData;

    public ImprovedSingleStringSelector(boolean hasSearchBox, String optionalLabel){
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        if(optionalLabel != null){
            this.label = new JLabel(optionalLabel);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(label);
        }
        if(hasSearchBox){
            this.searchBox = new JTextField("",8);
            add(this.searchBox);
            this.searchBox.addActionListener(e -> {
                List<Object> filtered = new ArrayList<>(fullData.length);
                for (int i = 0; i < fullData.length; i++) {
                    if(fullData[i].toString().contains(searchBox.getText())){
                        filtered.add(fullData[i]);
                    }
                }
                Object[] toAr = new Object[filtered.size()];
                filtered.toArray(toAr);
                selList.setListData(toAr);
            });
        }

        //selList.setBackground(new Color(0,0,0,1));
        add(new JScrollPane(selList));


        setVisible(true);
    }

    public void setData(Object[] data){
        selList.setListData(data);
        fullData = data;
    }
    public String getSelectedValue(){
        try {return selList.getSelectedValue().toString();}
        catch (NullPointerException ex){}
        return "";
    }
    public List<Object> getSelectedValues (){
        return selList.getSelectedValuesList();
    }

    public void addListSelectionListener(ListSelectionListener listener){
        selList.addListSelectionListener(listener);
    }
}
