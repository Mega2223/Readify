package net.mega2223.readify;

import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.util.JsonConverter;
import net.mega2223.readify.util.Misc;
import net.mega2223.readify.windows.ApplicationWindow;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class Application {

    public static void main(String[] args) throws IOException, ParseException {
        ApplicationWindow window = new ApplicationWindow();

        for (int i = 0; i < args.length; i++) {
            String data = Misc.readFromFile(new File(args[i]),null);
            SongHistory ac = JsonConverter.convertFromEndSongFormat(data);
            window.songHistory.loadSongs(ac);
        }
        window.refreshSongStats();
    }

}
