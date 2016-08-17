package com.jinshu.xuzhi.learnchinese.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by xuzhi on 2016/3/3.
 */
public class LearnChineseContract {
    private final String LOG_TAG = this.getClass().getSimpleName();
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.jinshu.xuzhi.learnchinese";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CHARACTER = Character.TABLE_NAME;
    //public static final String PATH_CHARACTER_READ_ONLY = CharacterReadOnly.TABLE_NAME;
    public static final String PATH_CUSTOM_LEARNING = CustomLearning.TABLE_NAME;
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String FINISHED = "finished";
    //public static final String DONE = "done";

        public static final class Character implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHARACTER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHARACTER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHARACTER;


        public static final String TABLE_NAME = "Character";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CHARACTER_ID = "character_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRONUNCIATION = "pronunciation";
        public static final String COLUMN_MULTITONE = "multitone";
        public static final String COLUMN_READ = "read";
        public static final String COLUMN_WRITE = "write";
        public static final String COLUMN_DONE = "done";
        public static final String COLUMN_DISPLAY_SEQUENCE = "display_sequence";
        public static final String COLUMN_ABILITY_TEST_SEQUENCE = "ability_test_sequence";

        public static final String PATH_CHARACTER_ID_LIST = "idList";
        public static final String PATH_CHARACTER_NAME_LIST = "nameList";
        public static final String PATH_DISPLAY_SEQUENCE_AND_READ = "display_sequence_and_read";
            public static final String PATH_ID_AND_READ = "id_and_read";
        public static final String PATH_ABILITY_TEST_SEQUENCE_AND_READ = "ability_test_sequence_and_read";
        public static Uri buildCharacterUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildCharacterUriByDone(String done) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_DONE).appendPath(done).build();
        }
        public static Uri buildCharacterUriByRead(String read) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_READ).appendPath(read).build();
        }
        public static Uri buildCharacterUriByName(String name) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_NAME).appendPath(name).build();
        }
        public static Uri buildCharacterUriByDisplaySequence(String sequence) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_DISPLAY_SEQUENCE).appendPath(sequence).build();
        }
        public static Uri buildCharacterUriByDisplaySequenceAndRead(String sequence,String read) {
            return CONTENT_URI.buildUpon().appendPath(PATH_DISPLAY_SEQUENCE_AND_READ).appendPath(sequence).appendPath(read).build();
        }
        public static Uri buildCharacterUriByIdAndRead(String sequence,String read) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ID_AND_READ).appendPath(sequence).appendPath(read).build();
        }
        public static Uri buildCharacterUriByAbilityTestSequenceAndRead(String sequence,String read) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ABILITY_TEST_SEQUENCE_AND_READ).appendPath(sequence).appendPath(read).build();
        }
        public static Uri buildCharacterUriById(int id) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_ID).appendPath(Integer.toString(id)).build();
        }

        public static Uri buildCharacterUriByIdList(String idListString)
        {
            Log.v("Id", "idString = " + idListString);
            return CONTENT_URI.buildUpon().appendPath(PATH_CHARACTER_ID_LIST).appendPath(idListString).build();
        }
        public static Uri buildCharacterUriByNameList(String nameListString)
        {
            Log.v("nameList", "nameListString = " + nameListString);
            return CONTENT_URI.buildUpon().appendPath(PATH_CHARACTER_NAME_LIST).appendPath(nameListString).build();
        }
        public static Uri buildCharacterStatusUriByCharacterId(int characterId) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_ID).appendPath(Integer.toString(characterId)).build();
        }

        public static String getTheSecondPara(Uri uri) {
            return uri.getPathSegments().get(2);
        }
        public static String getTheThirdPara(Uri uri) {
            return uri.getPathSegments().get(3);
        }
        public static String getNameFromUri(Uri uri) {
            return getTheSecondPara(uri);
        }
    }


    public static final class CustomLearning implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUSTOM_LEARNING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUSTOM_LEARNING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUSTOM_LEARNING;


        public static final String TABLE_NAME = "CustomLearning";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_STATUS = "learning";
        public static final String COLUMN_CHARACTER_SEQUENCE = "characterSequence";
        public static final String COLUMN_PERCENTAGE = "percentage";/*a/A--- learned/all*/
        public static final String COLUMN_CONTENT_TAG = "content_tag";/*11001000110---------1:learned,0:unlearned*/
        public static Uri buildCustomLearningUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildCustomLearningUriByName(String name) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_NAME).appendPath(name).build();
        }
        public static Uri buildCustomLearningUriById(int id) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_ID).appendPath(Integer.toString(id)).build();
        }
        public static Uri buildCustomLearningUriById(String idString) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_ID).appendPath(idString).build();
        }
        public static String getTheSecondPara(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}
