package net.mega2223.readify.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.mega2223.readify.objects.SongHistory;
import net.mega2223.readify.objects.Track;

import java.text.ParseException;

public class JsonConverter {

    public static SongHistory convertFromUserHistory(String JSONData) throws ParseException {
        return convertFromUserHistory(JSONData,null);
    }
    public static SongHistory convertFromUserHistory(String JSONData, Runnable task) throws ParseException {
        return convertFromUserHistory(JSONData,task,null);
    }
    public static SongHistory convertFromUserHistory(String JSONData, Runnable task, Runnable conclusionTask) throws ParseException {

        SongHistory ret = new SongHistory();

        JsonArray parsed = JsonParser.parseString(JSONData).getAsJsonArray();

        for (int i = 0; i < parsed.size(); i++) {
            ret.loadSong(getTrackFromJSONObject((JsonObject) parsed.get(i),task));
        }

        if(conclusionTask!=null){conclusionTask.run();}
        return ret;

    }
    public static Track getTrackFromJSONObject(JsonObject object) throws ParseException {return getTrackFromJSONObject(object,null);}

    public static Track getTrackFromJSONObject(JsonObject object, Runnable task) throws ParseException {

        String trackName = object.get("trackName").getAsString();
        String artistName = object.get("artistName").getAsString();;
        String endTime = object.get("endTime").getAsString();;
        int timePlayedMilis = Integer.parseInt(object.get("msPlayed").getAsString());
        if(task!=null){task.run();}
        return new Track(trackName,artistName,endTime,timePlayedMilis);
    }

    public static SongHistory convertFromPlaylistFile(){return null;}

}
