package com.jinshu.xuzhi.learnchinese;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

import java.io.File;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentLearningCards extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{
    View mRootView;
    TextView mCurrentCharacter;
    ImageView mFlagRead;
    private final String LOG_TAG = this.getClass().getSimpleName();
    //private static final int DEFAULT_CHARACTER_LOADER = 0;
    private static final int CUSTOM_CHARACTER_LOADER = 1;
    static int index = 0;/*待学习汉字序号*/
    static String customLearningTag = "";
    MainActivityFragment mThis;
    static Typeface tf1;
    private static String nameList;
    String CONSTANTS_RES_PREFIX = "android.resource://com.jinshu.xuzhi.learnchinese/";
    final MediaPlayer mp  = new MediaPlayer();

    class CharacterInfo{
        int id;
        String character;
        String sounds;
        String readFlag;
        public  CharacterInfo(String name){
            id = 0;
            character = name;
            sounds = "";
            readFlag =LearnChineseContract.NO;
        }
    }
    CharacterInfo[] mCharacters;

    public FragmentLearningCards() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_learning_cards, container, false);
        mCurrentCharacter = (TextView) mRootView.findViewById(R.id.ChineseCharacter);
        mFlagRead = (ImageView) mRootView.findViewById(R.id.flag_read);
        tf1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/simkai.ttf");//设置卡片字体为楷体
        mCurrentCharacter.setTypeface(tf1);

        mFlagRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCharacters[index].readFlag.equals(LearnChineseContract.NO)) {
                    mFlagRead.setImageResource(R.drawable.greenflag);
                    mCharacters[index].readFlag = LearnChineseContract.YES;
                    /*一份学习内容中相同的汉字全部更新学习状态*/
                    UpdateSameCharactersReadFlag(mCharacters,mCharacters[index].character,LearnChineseContract.YES);
                } else {
                    mFlagRead.setImageResource(R.drawable.whiteflag);
                    mCharacters[index].readFlag = LearnChineseContract.NO;
                    UpdateSameCharactersReadFlag(mCharacters,mCharacters[index].character,LearnChineseContract.NO);
                }

                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.Character.COLUMN_READ, mCharacters[index].readFlag);
                getActivity().getContentResolver().update(
                        LearnChineseContract.Character.buildCharacterUriById(mCharacters[index].id),
                        value, null, null);
                /*更新学习条目中的已学习汉字数*/
                TaskCalculatePercentage task = new TaskCalculatePercentage(getActivity());
                task.execute(mCharacters[index].character, mCharacters[index].readFlag);

            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }
                    @Override
                    public boolean onSingleTapUp(MotionEvent ev) {
                        try {
                            mp.reset();
                            if (!customPronunciationExist()) {
                                int id = getActivity().getResources().getIdentifier(mCharacters[index].sounds, "raw", "com.jinshu.xuzhi.learnchinese");
                                String uriString = CONSTANTS_RES_PREFIX + Integer.toString(id);
                                mp.setDataSource(getActivity(), Uri.parse(uriString));
                                mp.prepareAsync();
                            }
                            else
                            {
                                String sound = Environment.getExternalStorageDirectory()+"/LearnChineseCP/" + mCharacters[index].sounds + ".3gp";
                                mp.setDataSource(sound);
                                mp.prepareAsync();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {

                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;

                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if (index < mCharacters.length - 1) {
                                    index++;
                                    Log.v(LOG_TAG,"fling index = " + index);
                                    UpdateDisplay(index);
                                }

                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if (index > 0) {
                                    index--;
                                    UpdateDisplay(index);

                                }
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });

        mCurrentCharacter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });

        return mRootView;

    }
    @Override
    public void onDestroy() {
        mp.release();
        Log.v(LOG_TAG, "onDestroy");
        super.onDestroy();

    }
    Boolean customPronunciationExist()
    {
        File sound = new File(Environment.getExternalStorageDirectory()+"/LearnChineseCP/" + mCharacters[index].sounds + ".3gp");
        return sound.exists();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = null;
        String sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
        String nameList= bundle.getString(LearnChineseContract.CustomLearning.COLUMN_CONTENT);

        //nameList = Utility.generateCharacterName(nameList);
        /*initiate cards info by name*/
        String[] nameListString = nameList.split("");
        Log.v(LOG_TAG,"nameListString len = " + nameListString.length);
        mCharacters = new CharacterInfo[nameListString.length -1];//first item is ""
        int j = 0;
        for (int index = 0; (index < nameListString.length)&&(j<mCharacters.length); index++) {
            if(nameListString[index].equals("")) continue;
            mCharacters[j++] = new CharacterInfo(nameListString[index]);
            Log.v(LOG_TAG,"onCreateLoader" + mCharacters[j -1].character);
        }

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

        /*complete learning cards info*/
        for (int i = 0;i < cursorCount;i++)
        {
            generateCharactersInfo(mCharacters,cursor);
            cursor.moveToNext();
        }

        /*短暂退出软件后再次进入学习页面，保持汉字序号 待改进->写入数据库永久记录*/
       /* String currentCustomLearningTag = Utility.getCustomLearningTag(getActivity());
        if (!customLearningTag.equals(currentCustomLearningTag)) {
            index = 0;
            customLearningTag = currentCustomLearningTag;
        }*/

        UpdateDisplay(index);
        Log.v(LOG_TAG, "onLoadFinished");

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {    }

    void UpdateDisplay(int index) {
        mCurrentCharacter.setText(mCharacters[index].character);
        int imageResourceId = mCharacters[index].readFlag.equals(LearnChineseContract.NO) ? R.drawable.whiteflag : R.drawable.greenflag;
        mFlagRead.setImageResource(imageResourceId);
    }


    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        if (getActivity().getIntent().hasExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT)) {
            String name = getActivity().getIntent().getStringExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
            index = getActivity().getIntent().getIntExtra("index",0);
            name = Utility.generateCharacterName(name);
            Bundle bundle = new Bundle();
            bundle.putString(LearnChineseContract.CustomLearning.COLUMN_CONTENT, name);
            getLoaderManager().restartLoader(CUSTOM_CHARACTER_LOADER, bundle, this);
        }
        else
        {
            Log.e(LOG_TAG,"error,no intent...");
        }
    }
    void generateCharactersInfo(CharacterInfo[] CharacterInfoString,Cursor cursor)
    {
        String character = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));
        for (CharacterInfo info:CharacterInfoString)
        {
            if (info.character.equals(character))
            {
                info.id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));
                info.readFlag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
                info.sounds =  cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));
            }
        }
    }
    void UpdateSameCharactersReadFlag(CharacterInfo[] Characters,String character,String readFlag)
    {
        for (CharacterInfo info:Characters)
        {
            if (info.character.equals(character))
            {
                info.readFlag = readFlag;
            }
        }
    }
}
