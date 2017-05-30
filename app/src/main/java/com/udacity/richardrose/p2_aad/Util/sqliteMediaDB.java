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

public class sqliteMediaDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MediaDB";
    private static final String TABLE_NAME = "p2_aad";

    private static final String TABLE_MEDIA = "p2_aad";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_RATING = "rating";
    private static final String KEY_POSTER_URI = "poster";

    private static final String[] COLUMNS = {KEY_ID, KEY_TITLE, KEY_RATING, KEY_POSTER_URI};


    public sqliteMediaDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // Create the table
        String CREATE_P2_AAD_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                "id TEXT PRIMARY_KEY NOT NULL, "  +
                "title TEXT NOT NULL,"            +
                "rating TEXT NOT NULL,"           +
                "poster TEXT NOT NULL)";

        database.execSQL(CREATE_P2_AAD_TABLE) ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        // Drop the existing table
        database.execSQL(DROP_TABLE);

        this.onCreate(database);
    }


    public void addMediaRow(String dbID, String dbTitle, String dbPoster, String dbRating) {

        // 1. get reference to writable DB
        SQLiteDatabase database = this.getWritableDatabase();

        try {
            Log.d("add media to database", dbID.toString());

            // Add the data to the database
            ContentValues values = new ContentValues();
            values.put (KEY_ID, dbID);
            values.put (KEY_TITLE, dbTitle);
            values.put (KEY_RATING, dbRating);
            values.put (KEY_POSTER_URI, dbPoster);

            // Insert the information into the database
            database.insert(TABLE_NAME, // table
                    null, //nullColumnHack
                    values); // key/value -> keys = column names/ values = column
        }
        catch (Exception ex){

                Log.e("add Media: Error", ex.toString());
                ex.printStackTrace();
        }

        database.close();
    }

    public Media getMedia(int id){

        String dbID;
        String dbTitle;
        String dbRating;
        String dbPosterURI;

        // 1. get reference to readable DB
        SQLiteDatabase database = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                database.query(TABLE_MEDIA, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        dbID        = cursor.getString(0);
        dbTitle     = cursor.getString(1);
        dbRating    = cursor.getString(2);
        dbPosterURI = cursor.getString(4);

        Log.d("getBook("+id+")", dbID.toString());

//        return media;
        return (Media)null;
    }


    public void deleteMedia(String dbID) {

        // 1. get reference to writable DB
        SQLiteDatabase database = this.getWritableDatabase();

        try {

            // 2. delete
            database.delete(TABLE_NAME, //table name
                    KEY_ID + " = ?", // selections
                    new String[]{dbID}); //selections args

            //log
            Log.d("deleteBook", dbID.toString());
        } catch (Exception ex) {

            Log.e("deleteMedia Error", ex.toString());
            ex.printStackTrace();
        }

        // 3. close
        database.close();
    }

}
