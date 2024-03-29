package net.mega2223.readify.objects;

import net.mega2223.readify.util.Misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Track {

    protected String trackName;
    protected String artistName;
    protected String identifier; //TODO update when necessary
    protected Date endTime;
    protected int msPlayed;

    public Track(String trackName, String artistName, String endTime, int msPlayed) throws ParseException {
        this.setArtistName(artistName);
        this.setEndTime(new SimpleDateFormat().parse(Misc.adaptDate(endTime)));
        this.setMsPlayed(msPlayed);
        this.setTrackName(trackName);
        updateIdentifier();
    }

    private Track(String trackName, String artistName, Date endTime, int msPlayed) throws ParseException {
        this.setArtistName(artistName);
        this.setEndTime(endTime);
        this.setMsPlayed(msPlayed);
        this.setTrackName(trackName);
        updateIdentifier();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName; updateIdentifier();
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName; updateIdentifier();
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) throws ParseException {
        this.endTime = new SimpleDateFormat().parse(endTime);
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getMsPlayed() {
        return msPlayed;
    }

    public void setMsPlayed(int msPlayed) {
        this.msPlayed = msPlayed;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Track clone(){
        try {
            return new Track(trackName,artistName,endTime,msPlayed);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Track{" +
                "trackName='" + trackName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", endTime=" + endTime +
                ", msPlayed=" + msPlayed +
                '}';
    }

    void updateIdentifier(){
        identifier = artistName + " - " + trackName;
    }

}
