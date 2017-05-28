package com.udacity.richardrose.p2_aad.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;

import com.udacity.richardrose.p2_aad.DataHandler.Media;

/**
 * Created by richardrose on 27/05/17.
 */

public class sqlitedb extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MediaDB";

    public sqlitedb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String CREATE_P2_AAD_TABLE = "CREATE TABLE p2_aad (" +
                "id INTEGER PRIMARY_KEY, " +
                "title TEXT) ";

        database.execSQL(CREATE_P2_AAD_TABLE) ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS p2_aad");

        this.onCreate(database);
    }

    private static final String TABLE_MEDIA = "p2_aad";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";


    private static final String[] COLUMNS = {KEY_ID, KEY_TITLE};

    public void addMedia(Media media) {
        Log.d("add media to database", media.getID());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, media.getTitle()); // get title

        // 3. insert
        db.insert(TABLE_MEDIA, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column

        // 4. close
        db.close();
    }

    public Media getMedia(int id){
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        // 2. build query
        Cursor cursor =
                db.query(TABLE_MEDIA, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
        // 4. build book object
//        Media media = new Media();
//        Media.setId(Integer.parseInt(cursor.getString(
//                0)));
//        Media.setTitle(cursor.getString(1));
//        Media.setAuthor(cursor.getString(2));
//        Log.d("getBook("+id+")", media.toString());

//        return media;
        return (Media)null;
    }


    

}
