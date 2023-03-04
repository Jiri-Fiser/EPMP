package cz.ujep.ki.currency2022;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.Nullable;

public class CurrencyContentProvider extends ContentProvider {
    private final static String DB_NAME = "currencies.db";
    private final static int DB_VERSION = 1;
    private final static String TABLE_NAME = "currencies";

    public final static String AUTHORITY
            = "currency2022.ki.ujep.cz";
    public final static String CONTENT_URI
            = "content://" + AUTHORITY + "/" + TABLE_NAME;
    public final static String CONTENT_TYPE
            = "vnd.android.cursor.dir/vnd.currency2022.ki.ujep.cz";

    public final static String _ID = "_id";
    public final static String CODE = "code";
    public final static String NAME = "name";
    public final static String AMOUNT = "amount";
    public final static String RATE = "rate";
    public final static String COUNTRY = "country";

    private static final int CURRENCY_MATCH = 1;

    private OpenHelper dbHelper;
    private UriMatcher matcher;

    public CurrencyContentProvider() {
    }

    @Override
    public boolean onCreate() {
        dbHelper = new OpenHelper(getContext(), TABLE_NAME, DB_VERSION);
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, TABLE_NAME, CURRENCY_MATCH);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (matcher.match(uri)) {
            case CURRENCY_MATCH:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch(matcher.match(uri)) {
            case CURRENCY_MATCH:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        switch (matcher.match(uri)) {
            case CURRENCY_MATCH:
                id = db.insert(TABLE_NAME, CODE, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(uri, null);
            return itemUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (matcher.match(uri)) {
            case CURRENCY_MATCH:
                qb.setTables(TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown ␣ URI ␣ " + uri);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (matcher.match(uri)) {
            case CURRENCY_MATCH:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown ␣ URI ␣ " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(@Nullable Context context, @Nullable String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String command = "CREATE TABLE " + TABLE_NAME + " (" +
                            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            CODE + " VARCHAR(3), " +
                            NAME + " VARCHAR(32), " +
                            COUNTRY + " VARCHAR(32), " +
                            AMOUNT + "  INT, " +
                            RATE + " REAL" + ");";
            sqLiteDatabase.execSQL(command);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int old_ver, int new_ver) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}