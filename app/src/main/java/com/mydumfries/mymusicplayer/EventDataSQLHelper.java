package com.mydumfries.mymusicplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Helper to the database, manages versions and creation
 */
public class EventDataSQLHelper extends SQLiteOpenHelper {
    // Table name
    public static final String TABLE = "phonesongs";
    public static final String TABLE2 = "pcsongs";
    public static final String TABLE3 = "skydrivesongs";
    public static final String TABLE4 = "playlists";
    // Columns
    public static final String ID = "id";
    public static final String FILE = "File";
    public static final String TITLE = "Title";
    public static final String ARTIST = "Artist";
    public static final String ALBUM = "Album";
    public static final String RATING = "Rating";
    public static final String NAME = "Name";
    public static final String SOURCE = "Source";
    public static final String PLAYEDCOUNT = "PlayedCount";
    private static final String DATABASE_NAME = "songs.db";
    private static final int DATABASE_VERSION = 1;

    public EventDataSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE + "( " + BaseColumns._ID
                + " integer primary key, " + FILE + " text, "
                + TITLE + " text, " + ARTIST + " text, "
                + ALBUM + " text, "
                + PLAYEDCOUNT + " integer default 0,"
                + RATING + " integer default 0);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
        sql = "create table " + TABLE2 + "( " + BaseColumns._ID
                + " integer primary key, " + FILE + " text, "
                + TITLE + " text, " + ARTIST + " text, "
                + ALBUM + " text, "
                + PLAYEDCOUNT + " integer default 0,"
                + RATING + " integer default 0);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
        sql = "create table " + TABLE3 + "( " + BaseColumns._ID
                + " integer primary key, " + FILE + " text, "
                + TITLE + " text, " + ARTIST + " text, "
                + ALBUM + " text, "
                + RATING + " integer default 0);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
        sql = "create table " + TABLE4 + "( " + NAME + " text, "
                + SOURCE + " integer default 1);";
        Log.d("EventsData", "onCreate: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion)
            return;
        String sql = null;
        if (oldVersion == 1)
            sql = "alter table " + TABLE + " add COLUMN timestamp text;";
        if (oldVersion == 2)
            sql = "";

        Log.d("EventsData", "onUpgrade	: " + sql);
        if (sql != null)
            db.execSQL(sql);
    }
}
