package net.mega2223.readify;

import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;
import net.mega2223.readify.windows.ApplicationWindow;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Application {
    /*
     * TODO LIST:
     * Halt data from a specific interval (Maybe an entire data-handling category?)
     * Code cleanup
     * Unified status update method
     * Update all options to new graph building class (done)
     * Demonstration of stats for the readme.md
     * Create logo and social media preview for GitHub repo
     * Create Icon
     * Add font configuring to the frontend (font chooser and parameter)
     * Perhaps a saveable settings file might help?
     * Data overview board
     * Do tasks automatically based on commmand line arguments
     * Return of the progress bar
     * The application takes too long to calculate all-time playtime and other variables that can be buffered
     * */

    public static final int[] imageDimensions = {800,500}; //TODO

    public static void main(String[] args) throws IOException, ParseException {
        ApplicationWindow window = new ApplicationWindow();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]){
                case "-es": i++; window.setStatus("Loading pre-specified endsong files...\nThis may take a minute",true);
                ApplicationState.reportImportantTaskInProgress("LoadingEndsong","application loading endsong files.");
                    while(args.length > i && args[i].toCharArray()[0] != '-'){
                        window.songHistory.loadSongs(JsonConverter.convertFromEndSongFormat(Misc.readFromFile(new File(args[i]),null)));
                        i++;
                    }
                case "-sh": i++; window.setStatus("Loading pre-specified streaming history files...\nThis may take a while",true);
                ApplicationState.reportImportantTaskInProgress("LoadingStreamingHist","application loading streaming history files.");
                    while(args.length > i && args[i].toCharArray()[0] != '-'){
                        window.songHistory.loadSongs(JsonConverter.convertFromStreamingHistoryFormat(Misc.readFromFile(new File(args[i]),null)));
                        i++;
                    }

            }
        }
        ApplicationState.yieldImportantTask();
        window.refreshSongStats();
    }

}
