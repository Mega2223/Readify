package net.mega2223.readify.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class TimeSpanSelector extends JFrame {

    JTextField timeInput = new JTextField("",7);
    JList timeUnitSelector = new JList();
    JButton selectButton = new JButton("Select");

    List<Runnable> conclusionTasks = new ArrayList();

    public TimeSpanSelector(){
        JPanel bothColumns = new JPanel();

        setLayout(new BorderLayout());

        GridLayout bothColumnsLayout = new GridLayout(1, 2);
        bothColumns.setLayout(bothColumnsLayout);

        JPanel columnOne = new JPanel();
        JPanel columnTwo = new JPanel();

        timeUnitSelector.setListData(AcceptedTimeUnit.DEFAULT_UNITS);
        //timeInput.setBounds(new Rectangle(400,10));

        columnOne.setLayout(new BorderLayout());
        columnOne.add(new JLabel("Time unit:"),BorderLayout.PAGE_START);
        columnOne.add(timeUnitSelector,BorderLayout.CENTER);

        columnTwo.setLayout(new BorderLayout());
        columnTwo.add(new JLabel("Time amount:"),BorderLayout.PAGE_START);
        columnTwo.add(timeInput,BorderLayout.BEFORE_LINE_BEGINS);
        //columnTwo.setLayout(new BoxLayout(columnTwo,BoxLayout.Y_AXIS));

        bothColumns.add(columnOne);
        bothColumns.add(columnTwo);
        add(bothColumns,BorderLayout.CENTER);
        add(selectButton,BorderLayout.SOUTH);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void addConclusionTask(Runnable conclusionTask){
        if(conclusionTask != null){
            selectButton.addActionListener(e -> conclusionTask.run());
        }
    }

    public double getTimeInSeconds(){
        AcceptedTimeUnit selected = (AcceptedTimeUnit) timeUnitSelector.getSelectedValue();
        double timeAmount = Double.parseDouble(timeInput.getText());
        return timeAmount * selected.durationInSeconds;
    }

    public AcceptedTimeUnit getSelectedTimeUnit(){
        return (AcceptedTimeUnit) timeUnitSelector.getSelectedValue();
    }

    public double getTimeInSelectedTimeUnit(){
        return Double.parseDouble(timeInput.getText());
    }

    public static class AcceptedTimeUnit{
        public String timeUnit;
        public double durationInSeconds;

        public static final AcceptedTimeUnit[] DEFAULT_UNITS = {
                new AcceptedTimeUnit("Year",3600*24*365.25),//every year has on average 365 days and 6 hours, or 1/4th of a day
                new AcceptedTimeUnit("Month",3600*24*30.437),//30.437 is the average number of days in a month, ik it's lazy but damn i don't like messing with time units
                new AcceptedTimeUnit("Week",3600*24*7),
                new AcceptedTimeUnit("Day",3600*24),
                new AcceptedTimeUnit("Hour",3600),
                new AcceptedTimeUnit("Minute",60),
                new AcceptedTimeUnit("Second",1),
                //new AcceptedTimeUnit("Milis",1/1000), honestly should I even support that?
        };

        private AcceptedTimeUnit(String timeUnit, double durationInSeconds){
            this.durationInSeconds = durationInSeconds;
            this.timeUnit = timeUnit;
        }

        @Override
        public String toString() {
            return timeUnit;
        }
    }
}
