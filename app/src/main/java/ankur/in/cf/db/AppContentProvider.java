package ankur.in.cf.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by ankur on 23/12/15.
 */
public class AppContentProvider extends ContentProvider {
    static final String PROVIDER_NAME = "ankur.in.cf";
    public static final String AUTHORITY_STRING = "content://" + PROVIDER_NAME;
    public static final Uri IMAGE_TABLE_URI = Uri.parse(AUTHORITY_STRING+ "/"+DresserContract.Images.TABLE_NAME);
    public static final Uri FAV_TABLE_URI = Uri.parse(AUTHORITY_STRING+ "/"+DresserContract.Favourit.TABLE_NAME);
    static final int IMAGES = 1;
    static final int IMAGES_ID = 2;
    static final int FAVOURIT = 3;
    static final int FAVOURIT_ID = 4;
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, DresserContract.Images.TABLE_NAME, IMAGES);
        uriMatcher.addURI(PROVIDER_NAME, DresserContract.Images.TABLE_NAME +"/#", IMAGES_ID);
        uriMatcher.addURI(PROVIDER_NAME, DresserContract.Favourit.TABLE_NAME, FAVOURIT);
        uriMatcher.addURI(PROVIDER_NAME, DresserContract.Favourit.TABLE_NAME +"/#", FAVOURIT_ID);
    }
    private SQLiteDatabase db;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DBHelper dbHelper = new DBHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case IMAGES:
                qb.setTables(DresserContract.Images.TABLE_NAME);
                break;
            case IMAGES_ID:
                qb.setTables(DresserContract.Images.TABLE_NAME);
                qb.appendWhere( DresserContract.Images._ID + "=" + uri.getPathSegments().get(1));
                break;
            case FAVOURIT:
                qb.setTables(DresserContract.Favourit.TABLE_NAME);
                break;
            case FAVOURIT_ID:
                qb.setTables(DresserContract.Favourit.TABLE_NAME);
                qb.appendWhere( DresserContract.Favourit._ID + "=" + uri.getPathSegments().get(1));
                break;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri _uri = null;
        long rowID= -1;
        switch (uriMatcher.match(uri)) {
            case IMAGES:
                 rowID= db.insert(	DresserContract.Images.TABLE_NAME, "", values);
                if (rowID > 0)
                {
                     _uri = ContentUris.withAppendedId(IMAGE_TABLE_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                }
                break;
            case FAVOURIT:
                 rowID= db.insert(	DresserContract.Favourit.TABLE_NAME, "", values);
                if (rowID > 0)
                {
                    _uri = ContentUris.withAppendedId(FAV_TABLE_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                }
                break;
        }
        return _uri;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case IMAGES:
                count = db.delete(DresserContract.Images.TABLE_NAME, selection, selectionArgs);
                break;
            case IMAGES_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( DresserContract.Images.TABLE_NAME, DresserContract.Images._ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

        }
        return count;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case IMAGES:
                break;
        }
        return 0;
    }
}
