package net.mega2223.readify;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ApplicationState {
    private ApplicationState(){}

    public static boolean doingImportantTask = false;
    public static final int NO_LOGS = 0, IMPORTANT_ONLY = 1, RELEVANT_ONLY = 2, NOTABLE_ONLY = 3, EVERYTHING = 4;
    public static int debugLevel = RELEVANT_ONLY;
    private static final String[] logPriorityNames = {"IMPORTANT","RELEVANT","DEBUG","LOG"};
    public static String importantTaskName = null;
    public static String importantTaskMessage = null;
    public static int[] imageDimensions = {1000,400};

    public static void reportImportantTaskInProgress(String name, String description){
        importantTaskName = name;
        importantTaskMessage = description;
        doingImportantTask = true;
        log("Important task " + importantTaskName + " reported, description is \"" + importantTaskMessage + "\"", RELEVANT_ONLY);
    }
    public static void yieldImportantTask(){
        if(!doingImportantTask){return;}
        log("Important task " + importantTaskName + " rescinded, description was \"" + importantTaskMessage + "\"", RELEVANT_ONLY);
        doingImportantTask = false;
        importantTaskMessage = null;
        importantTaskName = null;
    }

    public static void checkForOngoingTask(boolean displayMessage, boolean throwException){
        if(doingImportantTask){
            if(displayMessage){
                JOptionPane.showMessageDialog(null,
                        "Cannot perform this task at this moment due to "+
                                importantTaskMessage + ", try again in a few seconds.");
            }
            if(throwException){throw new TaskInProgressException();}
        }
    }

    public static final BufferedImage APPLICATION_ICON = null;

    public static class TaskInProgressException extends RuntimeException {
        public TaskInProgressException(){
            super("Another task is currently going on:\n"+importantTaskName+": "+importantTaskMessage);
        }
    }

    public static void log(String message, int debugLevel){
        if(ApplicationState.debugLevel != 0 && debugLevel <= ApplicationState.debugLevel){
            System.out.println("["+Instant.now().truncatedTo(ChronoUnit.SECONDS)+"] ["+logPriorityNames[debugLevel-1]+"]: "+message);
        }
    }
}
