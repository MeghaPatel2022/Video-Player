package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.databasehelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.BaseModel;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.model.PlayList;

public class DBHelper extends SQLiteOpenHelper {

    // Database Name
    public static final String DATABASE_NAME = "VideoPlayer.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_HISTORY_RECENT_PLAY = "Table_History_Recent_Play";
    private static final String TABLE_PLAYLIST = "Table_Playlist";
    private static final String TABLE_PLAYLIST_ITEMS = "Table_Playlist_Items";

    // Table_History_Recent_Play TableColumns names
    private static final String _ID = "id";
    private static final String BUCKET_ID = "BucketId";
    private static final String BUCKET_NAME = "BucketName";
    private static final String BUCKET_PATH = "BucketPath";
    private static final String NAME = "Name";

    // Table_Playlist TableColumns names
    private static final String P_ID = "P_id";
    private static final String P_NAME = "P_name";
    private static final String P_DATE = "P_date";

    // Table_Playlist_Items TableColumns names
    private static final String PI_ID = "_id";
    private static final String PL_ID = "id";
    private static final String PI_NAME = "PlayListName";
    private static final String PI_PATH = "FilePath";
    private static final String PI_FILE_NAME = "FileName";

    Context context;

    //2nd table for appointment
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DATABASE_TABLE_HISTORY_RECENT_PLAY = "CREATE TABLE " + TABLE_HISTORY_RECENT_PLAY + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BUCKET_ID + " TEXT," +
                BUCKET_NAME + " TEXT," +
                BUCKET_PATH + " TEXT," +
                NAME + " TEXT);";

        String CREATE_DATABASE_TABLE_PLAYLIST = "CREATE TABLE " + TABLE_PLAYLIST + "(" +
                P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                P_NAME + " TEXT," +
                P_DATE + " TEXT);";

        String CREATE_DATABASE_TABLE_PLAYLIST_ITEMS = "CREATE TABLE " + TABLE_PLAYLIST_ITEMS + "(" +
                PI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PL_ID + " TEXT," +
                PI_NAME + " TEXT," +
                PI_PATH + " TEXT," +
                PI_FILE_NAME + " TEXT);";

        db.execSQL(CREATE_DATABASE_TABLE_HISTORY_RECENT_PLAY);
        db.execSQL(CREATE_DATABASE_TABLE_PLAYLIST);
        db.execSQL(CREATE_DATABASE_TABLE_PLAYLIST_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY_RECENT_PLAY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_ITEMS);
    }

    // Methods For Table_History_Recent_Play
    public boolean insertPlayHistory(BaseModel data) {

        getHistoryCheck(data.getBucketPath());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BUCKET_ID, data.getBucketId());
        values.put(BUCKET_NAME, data.getBucketName());
        values.put(BUCKET_PATH, data.getBucketPath());
        values.put(NAME, data.getName());

        long result = db.insert(TABLE_HISTORY_RECENT_PLAY, null, values);

        db.close();
        return result != -1;
    }

    public ArrayList<BaseModel> getHistoryData() {

        ArrayList<BaseModel> data = new ArrayList<BaseModel>();
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_HISTORY_RECENT_PLAY;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    BaseModel data_model = new BaseModel(cursor.getString(1),
                            cursor.getString(2), cursor.getString(3),
                            cursor.getString(4));
                    data.add(data_model);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
        return data;
    }

    public void renameHistory(String from, String to) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_HISTORY_RECENT_PLAY;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(3).equals(from)) {
                        updateHistory(from, to);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
    }

    public void updateHistory(String from, String to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        File file = new File(to);
        values.put(NAME, file.getName());
        values.put(BUCKET_PATH, to);
        db.update(TABLE_HISTORY_RECENT_PLAY, values, BUCKET_PATH + " = ?", new String[]{String.valueOf(from)});
        db.close();
    }

    public void getHistoryCheck(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_HISTORY_RECENT_PLAY;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(3).equals(path)) {
                        deleteOldHistory(path);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
    }

    public void deleteOldHistory(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY_RECENT_PLAY, BUCKET_PATH + " = ?", new String[]{String.valueOf(path)});
        db.close();
    }


    // Methods for Table_Playlist
    public boolean insertPlayList(String playListName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(P_NAME, playListName);
        values.put(P_DATE, String.valueOf(System.currentTimeMillis()));

        long result = db.insert(TABLE_PLAYLIST, null, values);

        db.close();
        return result != -1;
    }

    public ArrayList<PlayList> getPlayList() {

        ArrayList<PlayList> data = new ArrayList<PlayList>();
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_PLAYLIST;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    PlayList data_model = new PlayList(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2));
                    data.add(data_model);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
        return data;
    }

    public void deletePlayList(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST, P_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_PLAYLIST_ITEMS, PL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean checkIfExistPlayList(String name) {
        boolean isExist = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_PLAYLIST;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(1).equals(name)) {
                        isExist = true;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
        return isExist;
    }

    // Methods for Table_Playlist_Items
    public boolean insertPlayListSong(BaseModel data) {

        deleteOldPlayListItem(data.getBucketPath());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PL_ID, data.getP_id());
        values.put(PI_NAME, data.getBucketName());
        values.put(PI_PATH, data.getBucketPath());
        values.put(PI_FILE_NAME, data.getName());

        long result = db.insert(TABLE_PLAYLIST_ITEMS, null, values);

        db.close();
        return result != -1;
    }

    public boolean ifPlayListItemExist(String path) {
        boolean isExist = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_PLAYLIST_ITEMS;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(3).equals(path)) {
                        isExist = true;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
        return isExist;
    }

    public void deleteOldPlayListItem(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYLIST_ITEMS, PI_PATH + " = ?", new String[]{String.valueOf(path)});
        db.close();
    }

    public void removePlayListItems(String pl_id, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Order of deletions is important when foreign key relationships exist.
            String deleteQuery = "DELETE FROM " + TABLE_PLAYLIST_ITEMS + " WHERE " + PL_ID +
                    "='" + pl_id + "' AND " + PI_PATH +
                    "='" + path + "'";
            Log.e("LLL_DELQUERY: ", deleteQuery);
            db.execSQL(deleteQuery);

        } catch (Exception e) {
            Log.e("LLLLL_DB_DELETE", "deleteAllChats: " + e.getMessage());
        }

        db.close();
    }

    public ArrayList<BaseModel> getPlayListItems(String id) {

        ArrayList<BaseModel> data = new ArrayList<BaseModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        String count = "SELECT count(*) FROM " + TABLE_PLAYLIST_ITEMS;
        Cursor mCursor = db.rawQuery(count, null);
        mCursor.moveToFirst();
        int iCount = mCursor.getInt(0);
        if (iCount > 0) {
            String select_query = "Select * from " + TABLE_PLAYLIST_ITEMS + " WHERE " + PL_ID +
                    "='" + id + "'";

            Cursor cursor = db.rawQuery(select_query, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        BaseModel baseModel = new BaseModel();
                        baseModel.setP_id(cursor.getString(1));
                        baseModel.setBucketName(cursor.getString(2));
                        baseModel.setBucketPath(cursor.getString(3));
                        baseModel.setName(cursor.getString(4));

                        data.add(baseModel);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        mCursor.close();
        db.close();

        return data;
    }

    public void renamePlayList(String from, String to) {
        updatePlaylist1(from, to);
        updatePlay(from, to);
    }

    public void renamePlaylist(String from, String to) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select_query = "Select * from " + TABLE_PLAYLIST_ITEMS;

        Cursor cursor = db.rawQuery(select_query, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getString(3).equals(from)) {
                        updatePlaylist(from, to);
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        db.close();
    }

    public void updatePlaylist(String from, String to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        File file = new File(to);
        values.put(PI_FILE_NAME, file.getName());
        values.put(PI_PATH, to);
        db.update(TABLE_PLAYLIST_ITEMS, values, PI_PATH + " = ?", new String[]{String.valueOf(from)});
        db.close();
    }

    public void updatePlaylist1(String from, String to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PI_NAME, to);
        db.update(TABLE_PLAYLIST_ITEMS, values, PI_NAME + " = ?", new String[]{String.valueOf(from)});
        db.close();
    }

    public void updatePlay(String from, String to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(P_NAME, to);
        db.update(TABLE_PLAYLIST, values, P_NAME + " = ?", new String[]{String.valueOf(from)});
        db.close();
    }
}
