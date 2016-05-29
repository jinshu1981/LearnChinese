package com.example.xuzhi.learnchinese.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.example.xuzhi.learnchinese.data.LearnChineseContract.Character;
import com.example.xuzhi.learnchinese.data.LearnChineseContract.CustomLearning;

import java.util.Collections;

/**
 * Created by xuzhi on 2016/3/3.
 */
public class LearnChineseDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final String DATABASE_NAME = "ChineseCharacter.db";


    public LearnChineseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold Characters' learning status and display sequence.
        Log.v(LOG_TAG, "LearnChineseDbHelper onCreate");
        final String SQL_CREATE_CHARACTERS_TABLE = "CREATE TABLE " + Character.TABLE_NAME + " (" +
                Character._ID + " INTEGER PRIMARY KEY NOT NULL, " +
               // Character.COLUMN_CHARACTER_ID + " INTEGER NOT NULL, " +
                Character.COLUMN_NAME + " TEXT    NOT NULL, " +
                Character.COLUMN_PRONUNCIATION + " TEXT    NOT NULL, " +
                Character.COLUMN_MULTITONE + " TEXT    NOT NULL, " +
                Character.COLUMN_READ + " TEXT    NOT NULL, " +
                Character.COLUMN_DISPLAY_SEQUENCE + " INTEGER    NOT NULL, " +
                Character.COLUMN_ABILITY_TEST_SEQUENCE + " INTEGER    NOT NULL);";
        final String SQL_CREATE_CUSTOM_LEARNING_TABLE = "CREATE TABLE " + CustomLearning.TABLE_NAME + " (" +
                CustomLearning._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CustomLearning.COLUMN_NAME + " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_CONTENT+ " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_DATE + " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_STATUS + " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_PERCENTAGE + " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_CONTENT_TAG + " TEXT    NOT NULL, " +
                CustomLearning.COLUMN_CHARACTER_SEQUENCE + " INTEGER    NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_CHARACTERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CUSTOM_LEARNING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Character.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CustomLearning.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Return a cursor object with all rows in the table.
     * @return A cursor suitable for use in a SimpleCursorAdapter
     */
    public Cursor getAllCustomLearningItem() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("select * from " + CustomLearning.TABLE_NAME +" order by " + CustomLearning.COLUMN_ID , null);
    }

    /**
     * Return a cursor object with all rows in the table.
     * @return A cursor suitable for use in a SimpleCursorAdapter
     */
    public Cursor getCharactersByNameList(String nameList) {
        String[] nameArray = nameList.split("");
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) {
            return null;
        }
        return db.rawQuery("select * from " + Character.TABLE_NAME +" where "+ Character.COLUMN_NAME + " IN (" + TextUtils.join(",", Collections.nCopies(nameArray.length, "?")) + ")" + " order by " + CustomLearning.COLUMN_ID , nameArray);
    }
    /**
     * Updates a row in the database table with new column values, without changing the unique id of the row.
     * For simplicity reasons, nothing happens if this operation fails.
     * @param id The unique id of the row to update
     * @param percentage The new percentage value
     * @param contentTag The new priority value
     */
    public void updateCustomLearningPercentage(int id, String percentage,String contentTag) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db == null) {
            return;
        }
        ContentValues row = new ContentValues();
        row.put(CustomLearning.COLUMN_PERCENTAGE, percentage);
        row.put(CustomLearning.COLUMN_CONTENT_TAG, contentTag);
        db.update(CustomLearning.TABLE_NAME, row, "_id = ?", new String[] { String.valueOf(id) } );
        db.close();
    }
}
