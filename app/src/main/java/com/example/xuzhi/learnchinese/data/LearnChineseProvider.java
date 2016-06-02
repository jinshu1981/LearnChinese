package com.example.xuzhi.learnchinese.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collections;

/**
 * Created by xuzhi on 2016/3/3.
 */
public class LearnChineseProvider extends ContentProvider {

    private final String LOG_TAG = this.getClass().getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private LearnChineseDbHelper mOpenHelper;
    private ReadOnlyDbHelper mReadOnlyOpenHelper;
    static final int LEARN_CHINESE_CHARACTER = 100;
    static final int LEARN_CHINESE_CHARACTER_WITH_NAME = 101;
    static final int LEARN_CHINESE_CHARACTER_WITH_DONE = 102;
    static final int LEARN_CHINESE_CHARACTER_WITH_ID = 103;
    static final int LEARN_CHINESE_CHARACTER_WITH_ID_LIST = 104;
    static final int LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE = 105;
    static final int LEARN_CHINESE_CHARACTER_WITH_NAME_LIST = 106;
    static final int LEARN_CHINESE_CHARACTER_WITH_READ = 107;
    static final int LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE_AND_READ = 108;
    static final int LEARN_CHINESE_CHARACTER_WITH_CHARACTER_ID = 109;
    static final int LEARN_CHINESE_CHARACTER_WITH_ABILITY_TEST_SEQUENCE_AND_READ = 110;
    static final int LEARN_CHINESE_CUSTOM_LEARNING = 200;
    static final int LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID = 201;
    static final int LEARN_CHINESE_CUSTOM_LEARNING_WITH_NAME = 202;

    static final int LEARN_CHINESE_CHARACTER_READ_ONLY = 300;
    private static final SQLiteQueryBuilder sLearnChineseQueryBuilder;
    static{
        sLearnChineseQueryBuilder = new SQLiteQueryBuilder();
    }
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LearnChineseContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER, LEARN_CHINESE_CHARACTER);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_NAME +"/*", LEARN_CHINESE_CHARACTER_WITH_NAME);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_DONE +"/*", LEARN_CHINESE_CHARACTER_WITH_DONE);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_ID +"/*", LEARN_CHINESE_CHARACTER_WITH_ID);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.PATH_CHARACTER_ID_LIST +"/*", LEARN_CHINESE_CHARACTER_WITH_ID_LIST);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE +"/*", LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.PATH_CHARACTER_NAME_LIST +"/*", LEARN_CHINESE_CHARACTER_WITH_NAME_LIST);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_READ +"/*", LEARN_CHINESE_CHARACTER_WITH_READ);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.PATH_DISPLAY_SEQUENCE_AND_READ +"/*/*", LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE_AND_READ);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.PATH_ABILITY_TEST_SEQUENCE_AND_READ +"/*/*", LEARN_CHINESE_CHARACTER_WITH_ABILITY_TEST_SEQUENCE_AND_READ);
        matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER + "/" + LearnChineseContract.Character.COLUMN_CHARACTER_ID +"/*", LEARN_CHINESE_CHARACTER_WITH_CHARACTER_ID);

        matcher.addURI(authority, LearnChineseContract.PATH_CUSTOM_LEARNING, LEARN_CHINESE_CUSTOM_LEARNING);
        matcher.addURI(authority, LearnChineseContract.PATH_CUSTOM_LEARNING+ "/" + LearnChineseContract.CustomLearning.COLUMN_ID +"/*", LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID);
        matcher.addURI(authority, LearnChineseContract.PATH_CUSTOM_LEARNING+ "/" + LearnChineseContract.CustomLearning.COLUMN_NAME +"/*", LEARN_CHINESE_CUSTOM_LEARNING_WITH_NAME);

        matcher.addURI(authority, ReadOnlyDbContract.PATH_CHARACTER_READ_ONLY, LEARN_CHINESE_CHARACTER_READ_ONLY);
        //matcher.addURI(authority, LearnChineseContract.PATH_CHARACTER_STATUS + "/" + LearnChineseContract.CharacterStatus.COLUMN_CHARACTER_ID +"/*", LEARN_CHINESE_CHARACTER_STATUS_WITH_CHARACTER_ID);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new LearnChineseDbHelper(getContext());
        mReadOnlyOpenHelper = new ReadOnlyDbHelper(getContext());
        return true;
    }
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case LEARN_CHINESE_CHARACTER:
            case LEARN_CHINESE_CHARACTER_WITH_DONE:
            case LEARN_CHINESE_CHARACTER_WITH_ID_LIST:
            case LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE:
            case LEARN_CHINESE_CHARACTER_WITH_NAME_LIST:
            case LEARN_CHINESE_CHARACTER_WITH_READ:
            case LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE_AND_READ:
            case LEARN_CHINESE_CHARACTER_WITH_ABILITY_TEST_SEQUENCE_AND_READ:
                return LearnChineseContract.Character.CONTENT_TYPE;

            case LEARN_CHINESE_CHARACTER_WITH_NAME:
            case LEARN_CHINESE_CHARACTER_WITH_ID:
            case LEARN_CHINESE_CHARACTER_WITH_CHARACTER_ID:
                return LearnChineseContract.Character.CONTENT_ITEM_TYPE;

            case LEARN_CHINESE_CUSTOM_LEARNING:
                return LearnChineseContract.CustomLearning.CONTENT_TYPE;

            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID:
            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_NAME:
                return LearnChineseContract.CustomLearning.CONTENT_ITEM_TYPE;

            case LEARN_CHINESE_CHARACTER_READ_ONLY:
                return ReadOnlyDbContract.CharacterReadOnly.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    /***********************************Character Table*********************************************/
    //Character.done = ?
    private static final String sCharacterByDoneSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_DONE + " = ? ";
    //Character.name = ?
    private static final String sCharacterByNameSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_NAME + " = ? ";
    //Character._id = ?
    private static final String sCharacterByIdSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_ID + " = ? ";
    //Character._id IN (?,?,?...?)
    private static final String sCharacterByIdListSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." + LearnChineseContract.Character.COLUMN_ID + " IN ";

    //Character.display_sequence <> ?
    private static final String sCharacterByDisplaySequenceSelection=
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE + " <> ? ";

    //Character.name IN (?,?,?...?)
    private static final String sCharacterByNameListSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." + LearnChineseContract.Character.COLUMN_NAME + " IN ";
    //Character.read = ?
    private static final String sCharacterByReadSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_READ + " = ? ";
    //Character.display_sequence <> ? AND read = ?
    private static final String sCharacterByDisplaySequenceAndReadSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE + " <> ?  AND " +
                      LearnChineseContract.Character.COLUMN_READ + " = ? ";
    //Character.ability_test_sequence > ? AND read = ?
    private static final String sCharacterByAbilityTestSequenceAndReadSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE + " > ?  AND " +
                    LearnChineseContract.Character.COLUMN_READ + " = ? ";
    //Characters._id = ?
    private static final String sCharacterByCharacterIdSelection =
            LearnChineseContract.Character.TABLE_NAME +
                    "." +  LearnChineseContract.Character.COLUMN_ID + " = ? ";
    private Cursor getAllCharacters(
            Uri uri, String[] projection, String sortOrder) {
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return mOpenHelper.getReadableDatabase().query(
                LearnChineseContract.Character.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
    }


    private Cursor getCharacterByName(
            Uri uri, String[] projection, String sortOrder) {

        String name = LearnChineseContract.Character.getNameFromUri(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_NAME + " = " + name);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByNameSelection,
                new String[]{name},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharacterByDone(
            Uri uri, String[] projection, String sortOrder) {

        String done = LearnChineseContract.Character.getTheSecondPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_DONE + " = " + done);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByDoneSelection,
                new String[]{done},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharacterByDisplaySequece(
            Uri uri, String[] projection, String sortOrder) {

        String sequence = LearnChineseContract.Character.getTheSecondPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE + " <> " + sequence);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByDisplaySequenceSelection,
                new String[]{sequence},
                null,
                null,
                sortOrder
        );
    }
    private int DeleteTheCharacterByName(Uri uri) {

        String name = LearnChineseContract.Character.getNameFromUri(uri);
        Log.v(LOG_TAG, "name = " + name);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return  db.delete(
                LearnChineseContract.Character.TABLE_NAME, sCharacterByNameSelection, new String[]{name});

    }

    private int UpdateCharacterByName(Uri uri, ContentValues values) {

        String name = LearnChineseContract.Character.getNameFromUri(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(LearnChineseContract.Character.TABLE_NAME, values, sCharacterByNameSelection,
                new String[]{name});

    }
    private int UpdateCharacterById(Uri uri, ContentValues values) {

        String id = LearnChineseContract.Character.getTheSecondPara(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(LearnChineseContract.Character.TABLE_NAME, values, sCharacterByIdSelection,
                new String[]{id});

    }

    private Cursor getCharactersByIdList(
            Uri uri, String[] projection, String sortOrder) {

        String idString = LearnChineseContract.Character.getTheSecondPara(uri).trim();
        String[] idArray = idString.split(",");
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByIdListSelection + "(" + TextUtils.join(",", Collections.nCopies(idArray.length, "?")) + ")",/*generate (?,?,?)*/
                idArray,
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharactersByNameList(
            Uri uri, String[] projection, String sortOrder) {

        String nameString = LearnChineseContract.Character.getTheSecondPara(uri).trim();
        String[] nameArray = nameString.split("");
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByNameListSelection + "(" + TextUtils.join(",", Collections.nCopies(nameArray.length, "?")) + ")",/*generate (?,?,?)*/
                nameArray,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCharacterByRead(
            Uri uri, String[] projection, String sortOrder) {

        String read = LearnChineseContract.Character.getTheSecondPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_READ + " = " + read);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByReadSelection,
                new String[]{read},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getCharacterByDisplaySequenceAndRead(
            Uri uri, String[] projection, String sortOrder) {

        String sequence = LearnChineseContract.Character.getTheSecondPara(uri);
        String read = LearnChineseContract.Character.getTheThirdPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE + " <> " + sequence);
        Log.v(LOG_TAG,"sort order = " + sortOrder);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByDisplaySequenceAndReadSelection,
                new String[]{sequence,read},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCharacterByAbilityTestSequenceAndRead(
            Uri uri, String[] projection, String sortOrder) {

        String sequence = LearnChineseContract.Character.getTheSecondPara(uri);
        String read = LearnChineseContract.Character.getTheThirdPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE + " <> " + sequence);
        Log.v(LOG_TAG,"sort order = " + sortOrder);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.Character.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCharacterByAbilityTestSequenceAndReadSelection,
                new String[]{sequence,read},
                null,
                null,
                sortOrder
        );
    }
    private int DeleteTheCharacterById(Uri uri) {

        String id = LearnChineseContract.Character.getTheSecondPara(uri);
        //Log.v(LOG_TAG, "id = " + id);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return  db.delete(
                LearnChineseContract.Character.TABLE_NAME, sCharacterByCharacterIdSelection, new String[]{id});

    }
    /***********************************Custom Learning Table*********************************************/
    //CustomLearning._id = ?
    private static final String sCustomLearningByIdSelection =
            LearnChineseContract.CustomLearning.TABLE_NAME +
                    "." +  LearnChineseContract.CustomLearning.COLUMN_ID + " = ? ";
    //CustomLearning.name = ?
    private static final String sCustomLearningByNameSelection =
            LearnChineseContract.CustomLearning.TABLE_NAME +
                    "." +  LearnChineseContract.CustomLearning.COLUMN_NAME + " = ? ";

    private Cursor getAllCustomLearnings(
            Uri uri, String[] projection, String sortOrder) {
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.CustomLearning.TABLE_NAME);
        return mOpenHelper.getReadableDatabase().query(
                LearnChineseContract.CustomLearning.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
    }
    private Cursor getCustomLearningById(
            Uri uri, String[] projection, String sortOrder) {

        String id = LearnChineseContract.CustomLearning.getTheSecondPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.CustomLearning.COLUMN_ID + " = " + id);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.CustomLearning.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCustomLearningByIdSelection,
                new String[]{id},
                null,
                null,
                sortOrder
        );
    }
    private Cursor getCustomLearningByName(
            Uri uri, String[] projection, String sortOrder) {

        String name = LearnChineseContract.CustomLearning.getTheSecondPara(uri);
        Log.v(LOG_TAG, LearnChineseContract.CustomLearning.COLUMN_NAME + " = " + name);
        sLearnChineseQueryBuilder.setTables(LearnChineseContract.CustomLearning.TABLE_NAME);
        return sLearnChineseQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCustomLearningByNameSelection,
                new String[]{name},
                null,
                null,
                sortOrder
        );
    }
    private int UpdateCustomLearningById(Uri uri, ContentValues values) {

        String id = LearnChineseContract.CustomLearning.getTheSecondPara(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(LearnChineseContract.CustomLearning.TABLE_NAME, values, sCustomLearningByIdSelection,
                new String[]{id});

    }
    private int UpdateCustomLearningByName(Uri uri, ContentValues values) {

        String name = LearnChineseContract.CustomLearning.getTheSecondPara(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return db.update(LearnChineseContract.CustomLearning.TABLE_NAME, values, sCustomLearningByNameSelection,
                new String[]{name});

    }
    private int DeleteTheCustomLearningById(Uri uri) {

        String id = LearnChineseContract.CustomLearning.getTheSecondPara(uri);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        return  db.delete(
                LearnChineseContract.CustomLearning.TABLE_NAME, sCustomLearningByIdSelection, new String[]{id});

    }
    /***************************************CharactersReadOnly******************************************************/


    private Cursor getAllCharactersFromReadOnlyDb(
            Uri uri, String[] projection, String sortOrder) {
        sLearnChineseQueryBuilder.setTables(ReadOnlyDbContract.CharacterReadOnly.TABLE_NAME);
        return mReadOnlyOpenHelper.getReadableDatabase().query(
                ReadOnlyDbContract.CharacterReadOnly.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);
    }

    /*****************************************************************************************************************/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        Log.v(LOG_TAG, "query uri = " + uri.toString());
        switch (sUriMatcher.match(uri)) {

            case LEARN_CHINESE_CHARACTER: {
                retCursor = getAllCharacters(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_NAME: {
                retCursor = getCharacterByName(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_DONE: {
                retCursor = getCharacterByDone(uri, projection, sortOrder);
                break;
                }
            case LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE: {
                retCursor = getCharacterByDisplaySequece(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_ID_LIST: {
                retCursor = getCharactersByIdList(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_NAME_LIST: {
                retCursor = getCharactersByNameList(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_READ: {
                retCursor = getCharacterByRead(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_DISPLAY_SEQUENCE_AND_READ: {
                retCursor = getCharacterByDisplaySequenceAndRead(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_WITH_ABILITY_TEST_SEQUENCE_AND_READ: {
                retCursor = getCharacterByAbilityTestSequenceAndRead(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CUSTOM_LEARNING: {
                retCursor = getAllCustomLearnings(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID: {
                retCursor = getCustomLearningById(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_NAME: {
                retCursor = getCustomLearningByName(uri, projection, sortOrder);
                break;
            }
            case LEARN_CHINESE_CHARACTER_READ_ONLY: {
                retCursor = getAllCharactersFromReadOnlyDb(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        Log.v(LOG_TAG,"insert uri = " + uri.toString());
        switch (match) {
            case LEARN_CHINESE_CHARACTER: {
                long _id = db.insert(LearnChineseContract.Character.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = LearnChineseContract.Character.buildCharacterUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LEARN_CHINESE_CUSTOM_LEARNING: {
                long _id = db.insert(LearnChineseContract.CustomLearning.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = LearnChineseContract.CustomLearning.buildCustomLearningUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case LEARN_CHINESE_CHARACTER:
                rowsDeleted = db.delete(
                        LearnChineseContract.Character.TABLE_NAME, selection, selectionArgs);
                break;

            case LEARN_CHINESE_CHARACTER_WITH_NAME:
                rowsDeleted = DeleteTheCharacterByName(uri);
                break;

            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID:
                rowsDeleted = DeleteTheCustomLearningById(uri);
                break;

            case LEARN_CHINESE_CHARACTER_WITH_ID:
                rowsDeleted = DeleteTheCharacterById(uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }



    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case LEARN_CHINESE_CHARACTER:
                rowsUpdated = db.update(LearnChineseContract.Character.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LEARN_CHINESE_CHARACTER_WITH_ID:
                rowsUpdated = UpdateCharacterById(uri, values);
                break;
            case LEARN_CHINESE_CHARACTER_WITH_NAME:
                rowsUpdated = UpdateCharacterByName(uri, values);
                break;
            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_ID:
                rowsUpdated = UpdateCustomLearningById(uri, values);
                break;
            case LEARN_CHINESE_CUSTOM_LEARNING_WITH_NAME:
                rowsUpdated = UpdateCustomLearningByName(uri, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LEARN_CHINESE_CHARACTER:{
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(LearnChineseContract.Character.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }

            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}