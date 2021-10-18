package tcking.github.com.giraffeplayer2.trackselector;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by TangChao on 2017/10/11.
 */

public class TrackInfoWrapper {
    private final ITrackInfo innerTrack;
    private final int trackType;
    private final String fingerprint;
    private int index = -1;

    public TrackInfoWrapper(String fingerprint, ITrackInfo track, int index, int trackType) {
        this.fingerprint = fingerprint;
        this.innerTrack = track;
        this.index = index;
        this.trackType = trackType;
    }

    public static TrackInfoWrapper OFF(String fingerprint, int trackType) {
        return new TrackInfoWrapper(fingerprint, null, -1, trackType);
    }

    public int getIndex() {
        return index;
    }

    public int getTrackType() {
        return trackType;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getInfo() {
        return innerTrack == null ? "OFF" : innerTrack.getInfoInline();
    }
}
