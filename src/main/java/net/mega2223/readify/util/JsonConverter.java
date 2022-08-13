package net.mega2223.readify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;

import java.text.ParseException;

public class JsonConverter {

    /*as it turns out StreamingHistory files and EndSong Files follow very different guidelines
    StreamingHistory files are simply a list that can be initialized, and you can just get the values
    we need with the trackName, artistName and endTime locations

    EndSong files however are much more technical and large, and the locators are a bit different, the variables
    that are important to us currently are
    "master_metadata_track_name"
    “master_metadata_album_artist_name"
    "ms_played" which has an underline
    and "ts" which is the time the song stopped
    */

    public static SongHistory convertFromStreamingHistoryFormat(String JSONData) throws ParseException {
        return convertFromStreamingHistoryFormat(JSONData, null);
    }

    public static SongHistory convertFromStreamingHistoryFormat(String JSONData, Runnable task) throws ParseException {
        return convertFromStreamingHistoryFormat(JSONData, task, null);
    }

    public static SongHistory convertFromStreamingHistoryFormat(String JSONData, Runnable task, Runnable conclusionTask) throws ParseException {

        SongHistory ret = new SongHistory();

        JsonArray parsed = JsonParser.parseString(JSONData).getAsJsonArray();

        for (int i = 0; i < parsed.size(); i++) {
            ret.loadSong(getTrackFromStreamingHistoryJSONObject((JsonObject) parsed.get(i), task));
        }

        if (conclusionTask != null) {
            conclusionTask.run();
        }
        return ret;

    }

    public static SongHistory convertFromEndSongFormat(String JSONData) throws ParseException {
        return convertFromEndSongFormat(JSONData, null);
    }

    public static SongHistory convertFromEndSongFormat(String JSONData, Runnable task) throws ParseException {
        return convertFromEndSongFormat(JSONData, task, null);
    }

    public static SongHistory convertFromEndSongFormat(String JSONData, Runnable task, Runnable conclusionTask) throws ParseException {

        SongHistory ret = new SongHistory();

        JsonArray parsed = JsonParser.parseString(JSONData).getAsJsonArray();

        for (int i = 0; i < parsed.size(); i++) {
            try {
                ret.loadSong(getTrackFromEndSongJSonObject((JsonObject) parsed.get(i), task));
            } catch (UnsupportedOperationException jSonNull){}
        }

        if (conclusionTask != null) {
            conclusionTask.run();
        }
        return ret;

    }

    public static Track getTrackFromStreamingHistoryJSONObject(JsonObject object) throws ParseException {
        return getTrackFromStreamingHistoryJSONObject(object, null);
    }

    public static Track getTrackFromStreamingHistoryJSONObject(JsonObject object, Runnable task) throws ParseException {
        String trackName = object.get("trackName").getAsString();
        String artistName = object.get("artistName").getAsString();
        String endTime = object.get("endTime").getAsString();
        ;
        int timePlayedMilis = Integer.parseInt(object.get("msPlayed").getAsString());
        if (task != null) {
            task.run();
        }
        return new Track(trackName, artistName, endTime, timePlayedMilis);
    }

    public static Track getTrackFromEndSongJSonObject(JsonObject object) throws ParseException {
        return getTrackFromEndSongJSonObject(object, null);
    }

    public static Track getTrackFromEndSongJSonObject(JsonObject object, Runnable task) throws ParseException {

        String trackName = object.get("master_metadata_track_name").getAsString();
        String artistName = object.get("master_metadata_album_artist_name").getAsString();
        String endTime = object.get("ts").getAsString();
        int timePlayedMilis = Integer.parseInt(object.get("ms_played").getAsString());

        if (task != null) {
            task.run();
        }
        return new Track(trackName, artistName, endTime, timePlayedMilis);
    }

    public static SongHistory convertFromPlaylistFile() {
        return null;
    }

}
