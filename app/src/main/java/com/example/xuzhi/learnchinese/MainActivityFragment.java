package com.example.xuzhi.learnchinese;

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

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

import java.io.File;
import java.io.IOException;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    View mRootView;
    TextView mCurrentCharacter;
    ImageView mFlagRead;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int DEFAULT_CHARACTER_LOADER = 0;
    private static final int CUSTOM_CHARACTER_LOADER = 1;
    static int index = 0;/*待学习汉字序号*/
    static String customLearningTag = "";
    MainActivityFragment mThis;
    static Typeface tf1;

    String CONSTANTS_RES_PREFIX = "android.resource://com.example.xuzhi.learnchinese/";
    final MediaPlayer mp  = new MediaPlayer();

    class CharacterInfo{
        int id;
        String character;
        String sounds;
        String readFlag;
        public  CharacterInfo(Cursor cursor){
            id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));
            character = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));

            sounds = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));
            readFlag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
       }
    }
    CharacterInfo[] mCharacters;

    public MainActivityFragment() {
        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCurrentCharacter = (TextView) mRootView.findViewById(R.id.ChineseCharacter);
        mFlagRead = (ImageView) mRootView.findViewById(R.id.flag_read);
        tf1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/simkai.ttf");//设置卡片字体为楷体
        mCurrentCharacter.setTypeface(tf1);

        mFlagRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.v(LOG_TAG,"mFlagRead.setOnClickListener");
                if (mCharacters[index].readFlag.equals(LearnChineseContract.NO)) {
                    mFlagRead.setImageResource(R.drawable.greenflag);
                    mCharacters[index].readFlag = LearnChineseContract.YES;
                } else {
                    mFlagRead.setImageResource(R.drawable.whiteflag);
                    mCharacters[index].readFlag = LearnChineseContract.NO;
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
                //Log.v(LOG_TAG, "play the sound");
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
                        //Log.v(LOG_TAG, "mCurrentCharacter onclick");
                        try {
                            mp.reset();
                            //Log.v(LOG_TAG,"String = " + mCharacters[index].sounds);
                            if (!customPronunciationExist()) {
                                int id = getActivity().getResources().getIdentifier(mCharacters[index].sounds, "raw", "com.example.xuzhi.learnchinese");
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

                                index++;
                                UpdateDisplay(index);

                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.v(LOG_TAG, "Left to Right");
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
        String sortOrder = "";
        /*默认字库*/
        if (DEFAULT_CHARACTER_LOADER == i){
            sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
            uri = LearnChineseContract.Character.buildCharacterUriByRead(LearnChineseContract.NO);
        }
        else if(CUSTOM_CHARACTER_LOADER == i)/*自定义字库，所有汉字均显示*/
        {
            sortOrder = LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE + " ASC";
            uri = LearnChineseContract.Character.buildCharacterUriByDisplaySequence("0");
        }
        else
        {
            Log.e(LOG_TAG, "unknown cursor loader,id  =" + i);
        }

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
            //Log.v(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId());
            return;
        }
        int id = cursorLoader.getId();
        if ((DEFAULT_CHARACTER_LOADER == id)||(CUSTOM_CHARACTER_LOADER == id)) {
            //Log.v(LOG_TAG,"cursor.getCount() = " + cursor.getCount());
            mCharacters = new CharacterInfo[cursor.getCount()];
            cursor.moveToFirst();
            for (int i = 0; i < mCharacters.length; i++) {
                mCharacters[i] = new CharacterInfo(cursor);
                cursor.moveToNext();
            }
            /*短暂退出软件后再次进入学习页面，保持汉字序号 待改进，写入数据库永久记录*/
            String currentCustomLearningTag = Utility.getCustomLearningTag(getActivity());
            if (!customLearningTag.equals(currentCustomLearningTag)) {
                index = 0;
                customLearningTag = currentCustomLearningTag;
            }

            UpdateDisplay(index);
        }

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
        String customLearningTag = Utility.getCustomLearningTag(getActivity());
        //Log.v(LOG_TAG, "onResume customLearningTag =" + customLearningTag);
        if (customLearningTag.equals("")) {
            getLoaderManager().restartLoader(DEFAULT_CHARACTER_LOADER, null, this);
        }
        else
        {
            getLoaderManager().restartLoader(CUSTOM_CHARACTER_LOADER, null, this);
        }
    }
}
