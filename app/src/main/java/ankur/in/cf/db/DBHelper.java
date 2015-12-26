package ankur.in.cf.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ankur on 22/12/15.
 */
public class DBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dresser.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String BLOB_TYPE = " BLOB";
    private static final String INTEGER_TYPE = " INTEGER";


    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_IMAGES=
            "CREATE TABLE " + DresserContract.Images.TABLE_NAME + " (" +
                    DresserContract.Images._ID + " INTEGER PRIMARY KEY," +
                    DresserContract.Images.IMAGE_DATA + TEXT_TYPE + COMMA_SEP +
                    DresserContract.Images.FRAGMENT_COMPONENT_ID + TEXT_TYPE +
            " )";
    private static final String SQL_CREATE_FAVOURIT =
            "CREATE TABLE " + DresserContract.Favourit.TABLE_NAME + " (" +
                    DresserContract.Favourit._ID + " INTEGER PRIMARY KEY," +
                    DresserContract.Favourit.FAV_SHIRT + INTEGER_TYPE + COMMA_SEP +
                    DresserContract.Favourit.FAV_PANT + INTEGER_TYPE +
                    " )";

    private static final String SQL_DELETE_IMAGES =
            "DROP TABLE IF EXISTS " + DresserContract.Images.TABLE_NAME;
    private static final String SQL_DELETE_FAVOURIT =
            "DROP TABLE IF EXISTS " + DresserContract.Favourit.TABLE_NAME;

    public DBHelper(Context pContext){
        super(pContext, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_IMAGES);
        db.execSQL(SQL_CREATE_FAVOURIT);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_IMAGES);
        db.execSQL(SQL_DELETE_FAVOURIT);
        onCreate(db);
    }
}
