package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Constant {
    public static String backFrom = "Main";
    public static String PlayURL = "";
    public static boolean isEqualizerOn = true;
    public static int pos = 0;
    public static Long size;

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getSize(long size) {
        long kilo = 1024;
        long mega = kilo * kilo;
        long giga = mega * kilo;
        long tera = giga * kilo;
        String s = "";
        double kb = (double) size / kilo;
        double mb = kb / kilo;
        double gb = mb / kilo;
        double tb = gb / kilo;
        if (size < kilo) {
            s = size + " Bytes";
        } else if (size >= kilo && size < mega) {
            s = String.format("%.2f", kb) + " KB";
        } else if (size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB";
        } else if (size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB";
        } else if (size >= tera) {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }
}
