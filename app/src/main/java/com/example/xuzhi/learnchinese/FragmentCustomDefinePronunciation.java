package com.example.xuzhi.learnchinese;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentCustomDefinePronunciation extends Fragment {
    View rootView;
    final static String TEMP_CUSTOM_PRONUNCIATION_SUFFIX = "_c.3gp";
    final static String CUSTOM_PRONUNCIATION_SUFFIX = ".3gp";
    static EditText mPinyin;
    static ImageView mRecord;
    static ImageView mListen;
    static Button    mConfirmButton;
    private static String mDirName = null;
    private static String mFileName = null;
    private final String LOG_TAG = this.getClass().getSimpleName();
    static String pinYinName;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    CountDownTimer mCountDownTimer;
    public FragmentCustomDefinePronunciation() {
    }
    @Override
    public void onDestroy() {
        if (mPlayer != null)        stopPlaying();
        if (mRecorder != null)      stopRecording();
        super.onDestroy();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_custom_define_pronounciation, container, false);

        mPinyin = (EditText)rootView.findViewById(R.id.pinyin);
        mRecord = (ImageView)rootView.findViewById(R.id.record);
        mListen = (ImageView)rootView.findViewById(R.id.listen);
        mConfirmButton =  (Button)rootView.findViewById(R.id.confirm);

        mDirName = getActivity().getFilesDir().getAbsolutePath();
        Log.v(LOG_TAG, "mDirName = " + mDirName);

         /*编辑自定义学习内容时，intent中传入数据*/
        if (getActivity().getIntent().hasExtra(Intent.EXTRA_TEXT))
        {
            mPinyin.setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
        }
        /*record pronunciation*/
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pinYinName = getPinYinName();
                if (pinYinName.equals(""))
                {
                    Toast.makeText(getActivity(),"拼音名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                mListen.setClickable(false);
                File pinYin = new File(mDirName,pinYinName + TEMP_CUSTOM_PRONUNCIATION_SUFFIX);
                if (pinYin.exists())
                {
                    pinYin.delete();
                }
                mFileName = pinYin.toString();;
                Log.v(LOG_TAG, "mFileName = " + mFileName);

                recordPronunciation();
            }
        });
        /*listen trail*/
        mListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) stopPlaying();
                mRecord.setClickable(false);
                startPlaying();
            }
        });



        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pinYin = new File(mDirName,pinYinName + TEMP_CUSTOM_PRONUNCIATION_SUFFIX);
                if (!pinYin.exists())
                {
                    Toast.makeText(getActivity(),"没有对应的录音文件",Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    File finalPinYin = new File(mDirName,pinYinName + CUSTOM_PRONUNCIATION_SUFFIX);
                    if (finalPinYin.exists())
                    {
                        finalPinYin.delete();
                    }
                    pinYin.renameTo(finalPinYin);
                }
                /*back to previous activity*/
                getActivity().finish();

            }
        });
        return rootView;
    }
    public String getPinYinName()
    {
        String pinYin = mPinyin.getText().toString().trim();
        if (pinYin.equals(""))
            return "";

        String pinYinName = pinYin/* + Integer.toString(getToneTypeNum(toneType))*/;
        Log.v(LOG_TAG, pinYinName);
        return pinYinName;
    }
    void recordPronunciation()
    {
        startRecording();
        mCountDownTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                if (mRecorder != null) {
                    stopRecording();
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
        mRecorder.start();
        mRecord.setImageResource(R.drawable.recording);
    }
    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
        mListen.setClickable(true);
        mRecord.setImageResource(R.drawable.record);
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
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mRecord.setClickable(true);
                    mListen.setImageResource(R.drawable.listen);
                }

            });
            mListen.setImageResource(R.drawable.listening);
        } catch (IOException e) {
            Log.e(LOG_TAG, "mPlayer prepare() failed");
        }
    }
}
