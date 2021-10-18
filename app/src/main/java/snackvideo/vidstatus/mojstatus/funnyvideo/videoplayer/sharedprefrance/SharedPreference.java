package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.sharedprefrance;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.EqualizerModel;

public class SharedPreference {

    public static String MyPREFERENCES = "Video Player";
    public static String MODEL = "MODEL";

    public static void setEqualizer(Context c1, EqualizerModel model) {
        SharedPreferences prefs = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(model);
        edit.putString(MODEL, json);
        edit.commit();
    }

    public static EqualizerModel getEqualizer(Context c1) {
        SharedPreferences prefs = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        Gson gson = new Gson();
        String json = prefs.getString(MODEL, "");
        EqualizerModel obj = gson.fromJson(json, EqualizerModel.class);
        edit.commit();
        return obj;
    }

}
