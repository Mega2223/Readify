package net.mega2223.readify.objects;

import java.time.Instant;
import java.util.*;
import java.util.List;

public class SongHistory implements Iterable{

    ArrayList<Track> tracks = new ArrayList<>();

    public SongHistory(){

    }
    //risks an IntegerOverflow
    public int getTotalTimeListenedMilis(){
        int count = 0;
        for (int i = 0; i < tracks.size(); i++) {
            count += tracks.get(i).getMsPlayed();
        }
        return count;
    }

    public int getTotalTimeListenedSeconds(){
        int count = 0;
        for (int i = 0; i < tracks.size(); i++) {
            count += tracks.get(i).getMsPlayed()/1000;
        }
        return count;
    }

    public void clearSongs(){
        tracks.clear();
    }

    public List<String> getArtists(){
        List<String> ret = new ArrayList<>();

        for (int i = 0; i < tracks.size(); i++) {
            Track trackAct = tracks.get(i);
            if(!ret.contains(trackAct.artistName)){
                ret.add(trackAct.artistName);
            }


        }
        return ret;
    }

    public int getTotalAmountOfTimeForThisTrackInMilis(String track, String artist){

        int count = 0;
        for (int i = 0; i < tracks.size(); i++) {
            Track trackAct = tracks.get(i);
            if(trackAct.getTrackName().equals(track) && trackAct.getArtistName().equals(artist)){
                count += trackAct.msPlayed;
            }
        }
        return count;
    }
    /**Get a list of NON-REPEATING songs for this specific artist*/
    public List<String> getSongsForArtist(String artist, boolean putArtistName){
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            Track trackAct = tracks.get(i);
            String artistName = trackAct.artistName;
            String trackName = trackAct.trackName;
            if(putArtistName){trackName = artistName + " - " + trackName;}
            if(artistName.equals(artist)&&!ret.contains(trackName)){
                ret.add(trackName);
            }
        } //TODO: make a good and expandable string search panel
        return ret;
    }

    public List<String> getSongsForArtist(String artist){
        return getSongsForArtist(artist,false);
    }

    public void loadSongs(List<Track> tracks){this.tracks.addAll(tracks);}

    public void loadSongs(SongHistory tracks){this.tracks.addAll(tracks.getListens());}

    public void loadSong(Track track){tracks.add(track);}

    public void removeSong(Track track){tracks.remove(track);}

    public List<Track> getListens(){return (List<Track>) tracks.clone();}

    public List<Track> getListensForThisSong(String trackName){
        List<Track> ret = new ArrayList<>();
        for(Track ac : tracks){
            if(trackName.equals(ac.getTrackName())){
                ret.add(ac.clone());
            }
        }
        return ret;
    }

    public List<Track> getListensForThisSong(String trackName, String trackAuthor){
        List<Track> ret = new ArrayList<>();
        for(Track ac : tracks){
            if(trackName.equals(ac.getTrackName())&&trackAuthor.equals(ac.getArtistName())){
                ret.add(ac.clone());
            }
        }
        return ret;
    }
    /**Does not repeat the same song twice, song duration equals the sum of all milis listened to this song*/
    public List<Track> getSongs(){
        List<Track> out = new ArrayList<>();
        List<String> buf = new ArrayList<>();
        for (Track act : tracks) {
            if (!buf.contains(act.trackName)) {
                Track newTrack = act.clone();
                newTrack.setMsPlayed((int) getTotalMilisPlayedForSong(act.trackName));
                out.add(newTrack);
                buf.add(act.trackName);
            }
        }

        return out;
    }

    public SongHistory getSongsFromArtist(String artistName){
        SongHistory ret = new SongHistory();

        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            if(act.artistName.equals(artistName)){ret.loadSong(act);}

        }

        return ret;
    }

    @Override
    public Iterator iterator() {
        return tracks.iterator();
    }

    @Override
    public String toString(){
        String ret = "";

        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            ret += "[" + act.trackName + "|" + act.getArtistName() + "|" + act.getEndTime() + "|" + act.getMsPlayed() + "]\n";
        }

        return ret;
    }

    public List<Track> getCloseToDate(Date date, double rangeInDays){
        ArrayList ret = new ArrayList();
        for (Track act: tracks) {
            if(isInDateRange(date,act.getEndTime(),rangeInDays)){
                ret.add(act);
            }
        }
        return ret;
    }

    public Track getOldest(){
        Track r = tracks.get(0);
        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            if(act.getEndTime().before(r.getEndTime())){
                r = act;
            }
        }
        return r;
    }

    public Track getLatest(){
        Track r = tracks.get(0);
        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            if(act.getEndTime().after(r.getEndTime())){
                r = act;
            }
        }
        return r;
    }

    public long getTotalMilisPlayedForSong(String songTitle){
        long ret = 0;
        for (Track act : tracks){
            if(act.getTrackName().equals(songTitle)){ret+=act.getMsPlayed();}
        }
        return ret;
    }

    public long getTimeListenedForArtistInSeconds(String artist){
        List<Track> songs = getSongsFromArtist(artist).getSongs();
        long count = 0;
        for (Track act : songs){
            count += act.msPlayed/1000;
        }
        return count;
    }

    public List<String> sortBasedOnArtistPopularity(List<String> artists){
        return sortBasedOnArtistPopularity(artists,false);
    }
    public List<String> sortBasedOnArtistPopularity(List<String> artists, boolean ascending){
        class ComparableArtist implements Comparable{
            public final int sortingCriteria;
            public final String artist;
            public ComparableArtist(String artist, int sortingCriteria){this.artist = artist; this.sortingCriteria = sortingCriteria;}
            public int compareTo(Object o) {
                if(o instanceof ComparableArtist){
                    return sortingCriteria - ((ComparableArtist) o).sortingCriteria;
                }
                return 0;
            }
            @Override
            public String toString() {
                return "ComparableArtist{" +
                        "sortingCriteria=" + sortingCriteria +
                        ", artist='" + artist + '\'' +
                        '}';
            }
        }

        List<ComparableArtist> comparableArtists = new ArrayList<>();

        for (int i = 0; i < artists.size(); i++) {
            String artist = artists.get(i);
            long listened = getTimeListenedForArtistInSeconds(artist);
            comparableArtists.add(new ComparableArtist(artist, (int) listened));
        }

        Collections.sort(comparableArtists);
        if(!ascending){Collections.reverse(comparableArtists);}
        artists.clear();
        for (ComparableArtist act : comparableArtists){
            artists.add(act.artist);
        }

        return artists;
    }

    public static boolean isInDateRange(Date date, Date date2, double rangeInDays){

        Instant endt = date2.toInstant();
        Instant before = endt.minusSeconds((long) (86400*rangeInDays));
        Instant after = endt.plusSeconds((long) (86400*rangeInDays));

        return (date.after(Date.from(before))&&date.before(Date.from(after)));

    }

    public static boolean isInDateRange(Track track, long instantMilis, long rangeMilis){
        long songMilis = track.getEndTime().getTime();
        return isInMilisRange(songMilis,instantMilis,rangeMilis);
    }

    public static boolean isInMilisRange (long m1, long m2, long r){
        return Math.abs(m1-m2) <= r;
    }

    public List<Track> getAllSongsInRange (long timeMilis, long rangeMilis){
        List<Track> songs = new ArrayList<>();
        for (Track ac : this.tracks){
            if(isInDateRange(ac,timeMilis,rangeMilis)){songs.add(ac);}
        }
        return songs;
    }


}
