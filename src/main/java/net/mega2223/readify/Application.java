package net.mega2223.readify;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;
import net.mega2223.readify.windows.ApplicationWindow;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Application {
    /**
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
     * */
    public static void main(String[] args) throws IOException, ParseException {
        ApplicationWindow window = new ApplicationWindow();
        if (args.length > 0){
            window.statusLabel.setText(Misc.HTMLize("Loading pre-specified endsong files...\nThis may take a minute"));
            window.pack();
        }
        for (String arg : args) {
            String data = Misc.readFromFile(new File(arg), null);
            SongHistory ac = JsonConverter.convertFromEndSongFormat(data);
            window.songHistory.loadSongs(ac);
        }
        window.refreshSongStats();
    }

}
