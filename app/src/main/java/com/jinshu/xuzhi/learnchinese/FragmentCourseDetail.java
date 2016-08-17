package com.jinshu.xuzhi.learnchinese;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

import java.io.File;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentCourseDetail extends Fragment   implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG = this.getClass().getSimpleName();
    GridView courseDetailGridView;
    String CONSTANTS_RES_PREFIX = "android.resource://com.jinshu.xuzhi.learnchinese/";
    View rootView;
    static String content,contentFlag;
    class CharacterInfo{
        int id;
        String character;
        String sounds;
        String readFlag;
        public  CharacterInfo(Cursor cursor){
            id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));
            character =  cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));
            readFlag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
            sounds =  cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));
        }
    }
    CharacterInfo[] mCharacters;
    private static final int CUSTOM_CHARACTER_LOADER = 0;
    final MediaPlayer mp  = new MediaPlayer();
    public FragmentCourseDetail() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_course_detail, container, false);

        TextView name = (TextView) rootView.findViewById(R.id.custom_learning_name);
        name.setText(getActivity().getIntent().getStringExtra(LearnChineseContract.CustomLearning.COLUMN_NAME));
        com.jinshu.xuzhi.learnchinese.Utility.setBoldTextStyle(name);

        TextView date = (TextView) rootView.findViewById(R.id.custom_learning_date);
        date.setText(getActivity().getIntent().getStringExtra(LearnChineseContract.CustomLearning.COLUMN_DATE));

        courseDetailGridView = (GridView)rootView.findViewById(R.id.course_detail);
        content = getActivity().getIntent().getStringExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
        contentFlag = getActivity().getIntent().getStringExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT_TAG);
        Log.v(LOG_TAG, "content = " + content + ",contentFlag = " + contentFlag);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        courseDetailGridView.setAdapter(new com.jinshu.xuzhi.learnchinese.AdapterCourseDetail(getContext(), content, contentFlag));
        courseDetailGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String item = textView.getText().toString();
                int arrayIndex = getItemIndex(mCharacters, item);
                if (arrayIndex != -1) {
                    try {
                        mp.reset();
                        if (!customPronunciationExist(arrayIndex)) {
                            int soundId = getActivity().getResources().getIdentifier(mCharacters[arrayIndex].sounds, "raw", "com.jinshu.xuzhi.learnchinese");
                            String uriString = CONSTANTS_RES_PREFIX + Integer.toString(soundId);
                            mp.setDataSource(getActivity(), Uri.parse(uriString));
                            mp.prepareAsync();
                        } else {
                            String sound = Environment.getExternalStorageDirectory() + "/LearnChineseCP/" + mCharacters[arrayIndex].sounds + ".3gp";
                            mp.setDataSource(sound);
                            mp.prepareAsync();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        courseDetailGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = (int) view.getTag();
                if (pos != -1) {
                    Intent intent = new Intent(getActivity(), com.jinshu.xuzhi.learnchinese.ActivityLearningCards.class)
                            .putExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT, com.jinshu.xuzhi.learnchinese.Utility.generateCharacterName(content))
                            .putExtra("index", pos);
                    startActivity(intent);
                }
                return true;
            }
        });


        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = null;
        String sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
        String nameList= bundle.getString(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
        uri = LearnChineseContract.Character.buildCharacterUriByNameList(nameList);
        return new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if ((cursor == null)||(cursor.getCount() == 0)) {
            Log.e(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId());
            return;
        }
        //int id = cursorLoader.getId();
        //String[] nameListString = nameList.split("");
        int cursorCount = cursor.getCount();

        //Log.v(LOG_TAG,"cursor.getCount() = " + cursor.getCount());
        cursor.moveToFirst();
        mCharacters = new CharacterInfo[cursorCount];
        /*complete learning cards info*/
        for (int i = 0;i < cursorCount;i++)
        {
            mCharacters[i] = new CharacterInfo(cursor);
            cursor.moveToNext();
        }

        /*短暂退出软件后再次进入学习页面，保持汉字序号 待改进->写入数据库永久记录*/
       /* String currentCustomLearningTag = Utility.getCustomLearningTag(getActivity());
        if (!customLearningTag.equals(currentCustomLearningTag)) {
            index = 0;
            customLearningTag = currentCustomLearningTag;
        }*/


        Log.v(LOG_TAG, "onLoadFinished");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {    }



    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        if (getActivity().getIntent().hasExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT)) {
            content = com.jinshu.xuzhi.learnchinese.Utility.generateCharacterName(content);
            Bundle bundle = new Bundle();
            bundle.putString(LearnChineseContract.CustomLearning.COLUMN_CONTENT, content);
            getLoaderManager().restartLoader(CUSTOM_CHARACTER_LOADER, bundle, this);
        }
        else
        {
            Log.e(LOG_TAG,"error,no intent...");
        }
    }
    /*void generateCharactersInfo(CharacterInfo[] CharacterInfoString,Cursor cursor)
    {
        //String character = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));
        for (CharacterInfo info:CharacterInfoString)
        {

                info.id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));

                info.readFlag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
                info.sounds =  cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));

        }
    }*/

    int getItemIndex(CharacterInfo[] CharacterInfoString,String item)
    {   int i = 0;
        for (CharacterInfo info:CharacterInfoString)
        {

           if (info.character.equals(item)){
               return i;
           }
            i++;

        }
        return -1;
    }
    @Override
    public void onDestroy() {
        mp.release();
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();

    }
    Boolean customPronunciationExist(int arrayIndex)
    {
        File sound = new File(Environment.getExternalStorageDirectory()+"/LearnChineseCP/" + mCharacters[arrayIndex].sounds + ".3gp");
        return sound.exists();
    }
}
