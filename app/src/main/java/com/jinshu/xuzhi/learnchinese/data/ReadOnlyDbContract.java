package com.jinshu.xuzhi.learnchinese.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by xuzhi on 2016/4/11.
 */
public class ReadOnlyDbContract {
    private final String LOG_TAG = this.getClass().getSimpleName();
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.jinshu.xuzhi.learnchinese";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //public static final String PATH_CHARACTER = CharacterReadOnly.TABLE_NAME;
    public static final String PATH_CHARACTER_READ_ONLY = CharacterReadOnly.TABLE_NAME;
    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String DONE = "done";

    public static final class CharacterReadOnly implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHARACTER_READ_ONLY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHARACTER_READ_ONLY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHARACTER_READ_ONLY;

        public static final String TABLE_NAME = "Characters";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CHARACTER_ID = "character_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRONUNCIATION = "pronunciation";
        public static final String COLUMN_MULTITONE = "multitone";
        public static final String COLUMN_READ = "read";
        public static final String COLUMN_DISPLAY_SEQUENCE = "display_sequence";
        public static final String COLUMN_ABILITY_TEST_SEQUENCE = "ability_test_sequence";
        public static Uri buildCharacterStatusUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCharacterStatusUriByCharacterId(int characterId) {
            return CONTENT_URI.buildUpon().appendPath(COLUMN_CHARACTER_ID).appendPath(Integer.toString(characterId)).build();
        }

        public static String getTheSecondPara(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}

