package net.mega2223.readify.objects;

import java.time.Instant;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

    public List<String> getSongsForArtist(String artist){
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            Track trackAct = tracks.get(i);
            String artistName = trackAct.artistName;
            if(artistName.equals(artist)){ret.add(artistName);}
        }
        return ret;
    }

    public void loadSongs(List<Track> tracks){this.tracks.addAll(tracks);}

    public void loadSongs(SongHistory tracks){this.tracks.addAll(tracks.getListens());}

    public void loadSong(Track track){tracks.add(track);}

    public void removeSong(Track track){tracks.remove(track);}

    public List<Track> getListens(){return (List<Track>) tracks.clone();}

    public List<Track> getSongs(){
        List<Track> out = new ArrayList<>();
        List<String> buf = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);

            if(!buf.contains(act.trackName)){out.add(act);}

            buf.add(act.trackName);
        }

        return out;
    }

    public SongHistory getSongsFromArtist(String artistName){
        SongHistory ret = new SongHistory();

        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            System.out.println(act.artistName + "==" + artistName);
            if(act.artistName.equals(artistName)){ret.loadSong(act);System.out.println("adicionando");}

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

    public Track getNewest(){
        Track r = tracks.get(0);
        for (int i = 0; i < tracks.size(); i++) {
            Track act = tracks.get(i);
            if(act.getEndTime().after(r.getEndTime())){
                r = act;
            }
        }
        return r;
    }

    public static boolean isInDateRange(Date date, Date date2, double rangeInDays){

        Date endTime = date2;
        Instant endt = endTime.toInstant();
        Instant before = endt.minusSeconds((long) (86400*rangeInDays));
        Instant after = endt.plusSeconds((long) (86400*rangeInDays));

        return (date.after(Date.from(before))&&date.before(Date.from(after)));

    }


}
