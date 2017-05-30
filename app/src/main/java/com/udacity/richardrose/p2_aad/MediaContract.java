package com.udacity.richardrose.p2_aad;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by richardrose on 30/05/17.
 */

public class MediaContract {

    public static final String AUTHORITY = "com.udacity.richardrose.p2.aad.provider";
    public static final String BASE_PATH = "media";

    public static final Uri CONTENT_URI = Uri.parse("content://" +
            AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE +
                    "/vnd.com.mediamanager.provider.table";

    /**
     * The mime type of a single item.
     */
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE +
                    "/vnd.com.mediamanager.provider.table_item";

    /**
     * A projection of all columns
     * in the items table.
     */
    public static final String[] PROJECTION_ALL = { "_id",
            "mediaID",
            "mediaTitle",
            "mediaRating",
            "mediaPoster" };

    //public static final String SORT_ORDER_DEFAULT = NAME + " ASC";
    public static final class Columns {
        public static String TABLE_ROW_ID = "mediaID";
        public static String TABLE_ROW_TITLE = "mediaTitle";
        public static String TABLE_ROW_RATING = "mediaRating";
        public static String TABLE_ROW_POSTER = "mediaPoster";
    }


}
