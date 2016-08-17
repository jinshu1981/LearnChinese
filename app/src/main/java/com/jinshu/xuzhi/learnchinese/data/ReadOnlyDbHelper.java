package com.jinshu.xuzhi.learnchinese.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by xuzhi on 2016/4/11.
 */
public class ReadOnlyDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final String DATABASE_NAME = "ChineseCharacterReadOnly.db";


    public ReadOnlyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // Create a table to hold Characters' learning status and display sequence.
        Log.v(LOG_TAG, "ReadOnlyDbHelper onCreate");



        //sqLiteDatabase.execSQL(SQL_CREATE_CHARACTERS_TABLE);
        //sqLiteDatabase.execSQL(SQL_CREATE_CUSTOM_LEARNING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
       // sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LearnChineseContract.Character.TABLE_NAME);
       // sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LearnChineseContract.CustomLearning.TABLE_NAME);
       // onCreate(sqLiteDatabase);
    }
}
