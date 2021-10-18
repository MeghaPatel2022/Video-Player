package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model;

public class PlayList {

    String P_id, P_name;
    String date;

    public PlayList(String p_id, String p_name, String date) {
        P_id = p_id;
        P_name = p_name;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public PlayList() {
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getP_id() {
        return P_id;
    }

    public void setP_id(String p_id) {
        P_id = p_id;
    }

    public String getP_name() {
        return P_name;
    }

    public void setP_name(String p_name) {
        P_name = p_name;
    }
}
