package ru.zeyuzh.testrssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class RSSContentProvider extends ContentProvider {

    //constants
    // BD
    static final String DB_NAME = "rssdb";
    static final int DB_VERSION = 1;

    // Table
    static final String TABLE = "rss";

    // Columns
    static final String ID = "_id";
    static final String COLUMN_NAME_TITLE = "title";
    static final String COLUMN_NAME_DESCRIPTION = "description";
    static final String COLUMN_NAME_LINK = "link";
    static final String COLUMN_NAME_PUBDATE = "pubDate";


    // Script for craate table
    static final String DB_CREATE = "create table " + TABLE + "("
            + ID + " integer primary key autoincrement, "
            + COLUMN_NAME_TITLE + " text, "
            + COLUMN_NAME_DESCRIPTION + " text, "
            + COLUMN_NAME_LINK + " text, "
            + COLUMN_NAME_PUBDATE + " text"
            + ");";

    // // Uri
    // authority
    static final String AUTHORITY = "ru.zeyuzh.providers.RSSfeed";

    // path
    static final String PATH = "rss";

    // General Uri
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    // Data types
    // Set of lines
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH;
    // Single line
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH;

    //// UriMatcher
    // General Uri
    static final int URI_RSS = 1;
    // Uri with ID
    static final int URI_RSS_ID = 2;

    // Creating UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, URI_RSS);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_RSS_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        Log.d("lg", "onCreate");
        dbHelper = new DBHelper(getContext());
        return true;
    }

    // reading
    // projection = columns, selection = condition ( if ), selectionArgs = arguments to conditions, sortOrder = sorting
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("lg", "query, " + uri.toString());
        // checking Uri
        switch (uriMatcher.match(uri)) {
            case URI_RSS: // if general Uri
                Log.d("lg", "URI_RSS");
                // sorting
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = COLUMN_NAME_TITLE + " ASC";
                }
                break;
            case URI_RSS_ID: // if Uri with ID
                String id = uri.getLastPathSegment();
                Log.d("lg", "URI_RSS_ID, " + id);
                // Adding ID
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        // ContentResolver notify the cursor of changes data in CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Log.d("lg", "insert, " + uri.toString());
        if (uriMatcher.match(uri) != URI_RSS)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(TABLE, null, values);
        Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        // notify ContentResolver about changing data in resultUri address
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d("lg", "delete, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_RSS:
                Log.d("lg", "URI_RSS");
                break;
            case URI_RSS_ID:
                String id = uri.getLastPathSegment();
                Log.d("lg", "URI_RSS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d("lg", "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_RSS:
                Log.d("lg", "URI_RSS");
                break;
            case URI_RSS_ID:
                String id = uri.getLastPathSegment();
                Log.d("lg", "URI_RSS_ID, " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = ID + " = " + id;
                } else {
                    selection = selection + " AND " + ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int countOfUpdated = db.update(TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return countOfUpdated;
    }

    public String getType(Uri uri) {
        Log.d("lg", "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_RSS:
                return CONTENT_TYPE;
            case URI_RSS_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            /*
            //test fill the table 10 rows
            ContentValues cv = new ContentValues();
            for (int i = 1; i <= 10; i++) {
                cv.put(COLUMN_NAME_TITLE, "title " + i);
                cv.put(COLUMN_NAME_DESCRIPTION, "description " + i);
                cv.put(COLUMN_NAME_LINK, "link " + i);
                cv.put(COLUMN_NAME_PUBDATE, "pubDate " + i);
                db.insert(TABLE, null, cv);
            }
            */
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
