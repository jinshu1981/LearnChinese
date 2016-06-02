package com.example.xuzhi.learnchinese;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

import java.io.File;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    View mRootView;
    TextView mCurrentCharacter;
    ImageView mFlagRead;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int DEFAULT_CHARACTER_LOADER = 0;
    private static final int CUSTOM_CHARACTER_LOADER = 1;
    private static String mFileName = null;
    static int index = 0;/*待学习汉字序号*/
    static String customLearningTag = "";
    private static final String CUSTOM_SEQUENCE = "custom_sequence";
    MainActivityFragment mThis;
    static Typeface tf1;

    String mReadStatus,mWriteStatus,mDoneStatus;
    CountDownTimer mCountDownTimer;
    String CONSTANTS_RES_PREFIX = "android.resource://com.example.xuzhi.learnchinese/";
    final MediaPlayer mp  = new MediaPlayer();

    class CharacterInfo{
        int id;
        String character;
        String sounds;
        String readFlag;
       // String writeFlag;
        //String doneFlag;
        public  CharacterInfo(Cursor cursor){
            id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));
            character = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));

            sounds = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));
            readFlag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
       }
    }
    CharacterInfo[] mCharacters;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    public MainActivityFragment() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        Log.v(LOG_TAG,mFileName);
        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCurrentCharacter = (TextView) mRootView.findViewById(R.id.ChineseCharacter);
        //mRead = (ImageView) mRootView.findViewById(R.id.read);
        mFlagRead = (ImageView) mRootView.findViewById(R.id.flag_read);
        //mFlagRead.setImageResource(mCharacters[index].readFlag.equals(LearnChineseContract.NO)?R.drawable.whiteflag:R.drawable.greenflag);
        //FlagWritten = (ImageView) mRootView.findViewById(R.id.flag_write);
        //mFlagRemembered = (ImageView) mRootView.findViewById(R.id.flag_remember);
        tf1 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/simkai.ttf");
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
                CalculatePercentageTask task = new CalculatePercentageTask(getActivity());
                task.execute(mCharacters[index].character, mCharacters[index].readFlag);

            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                //Log.v(LOG_TAG, "play the sound");
                mp.start();
            }
        });
       /* cancel the recording function
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                performOnEnd();
            }

        });*/

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
                            if (mRecorder != null) stopRecording();
                            if (mPlayer != null) stopPlaying();
                            mp.reset();
                            //String string = mCursor.getString(mCursor.getColumnIndex(LearnChineseContract.Character.COLUMN_PRONUNCIATION));
                            Log.v(LOG_TAG,"String = " + mCharacters[index].sounds);
                            if (!customPronunciationExist()) {
                                int id = getActivity().getResources().getIdentifier(mCharacters[index].sounds, "raw", "com.example.xuzhi.learnchinese");
                                String uriString = CONSTANTS_RES_PREFIX + Integer.toString(id);
                                //Log.v(LOG_TAG,"uriString = " + uriString);
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
                                Log.v(LOG_TAG, "Right to Left");
                                index++;
                                //mCursor.moveToPosition(index % mCursorLength);
                                /*
                                if (mPlayer != null)        stopPlaying();
                                if (mRecorder != null)      stopRecording();
                                mRead.setImageResource(R.mipmap.listen);
                                mCountDownTimer.cancel();*/
                                UpdateDisplay(index);

                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.v(LOG_TAG, "Left to Right");
                                if (index > 0) {
                                    index--;
                                    //mCurrentCharacter.setText(Characters[index % Characters.length]);
                                    //mCursor.moveToPosition(index%mCursorLength);
                                    /*if (mPlayer != null)        stopPlaying();
                                    if (mRecorder != null)      stopRecording();
                                    mRead.setImageResource(R.mipmap.listen);
                                    mCountDownTimer.cancel();*/
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
        if (mPlayer != null)        stopPlaying();
        if (mRecorder != null)      stopRecording();
        super.onDestroy();

    }
    Boolean customPronunciationExist()
    {
        File sound = new File(Environment.getExternalStorageDirectory()+"/LearnChineseCP/" + mCharacters[index].sounds + ".3gp");
        return sound.exists();
    }
    void performOnEnd()
    {
        startRecording();
        mCountDownTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                if (mRecorder != null) {
                    stopRecording();
                    //mRead.setImageResource(R.mipmap.listen);
                    startPlaying();
                }
            }
        }.start();
    }
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "mRecorder prepare() failed");
        }
        //mRead.setImageResource(R.mipmap.read);
        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }
    private void stopPlaying() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }
    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "mPlayer prepare() failed");
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
       String customLearningTag = Utility.getCustomLearningTag(getActivity());
        Log.v(LOG_TAG, "customLearningTag =" + customLearningTag);
       /* if (customLearningTag.equals("")) {
            getLoaderManager().initLoader(DEFAULT_CHARACTER_LOADER, null, this);
        }
        else
        {
            getLoaderManager().initLoader(CUSTOM_CHARACTER_LOADER, null, this);
        }*/
        super.onActivityCreated(savedInstanceState);
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
            Log.v(LOG_TAG, "unknown cursor loader,id  =" + i);
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
            Log.v(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId());
            return;
        }
        int id = cursorLoader.getId();
        if ((DEFAULT_CHARACTER_LOADER == id)||(CUSTOM_CHARACTER_LOADER == id)) {
            Log.v(LOG_TAG,"cursor.getCount() = " + cursor.getCount());
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
        int imageResourceid = mCharacters[index].readFlag.equals(LearnChineseContract.NO) ? R.drawable.whiteflag : R.drawable.greenflag;
        mFlagRead.setImageResource(imageResourceid);

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理动作按钮的点击事件
        switch (item.getItemId()) {
            case R.id.action_parents_options_settings:
            {
                //Intent intent = new Intent(getActivity(), ParentsOptionsActivity.class);
                //startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        String customLearningTag = Utility.getCustomLearningTag(getActivity());
        Log.v(LOG_TAG, "onResume customLearningTag =" + customLearningTag);
        if (customLearningTag.equals("")) {
            getLoaderManager().restartLoader(DEFAULT_CHARACTER_LOADER, null, this);
        }
        else
        {
            getLoaderManager().restartLoader(CUSTOM_CHARACTER_LOADER, null, this);
        }
    }
}
