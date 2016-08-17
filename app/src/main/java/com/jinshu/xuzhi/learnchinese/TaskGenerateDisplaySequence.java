package com.jinshu.xuzhi.learnchinese;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * Created by xuzhi on 2016/5/19.
 */
public class TaskGenerateDisplaySequence extends AsyncTask<String, Void, Integer> {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context mContext;
    private final ImageView mView;
    private final ProgressBar mProgressBar;
    public TaskGenerateDisplaySequence(Context context, ImageView view, ProgressBar progressBar) {
        mContext = context;
        mView = view;
        mProgressBar = progressBar;
    }

    @Override
    protected Integer doInBackground(String... params) {

        if (params.length == 0) {
            return 0;
        }
        String characterSequence = params[1];
        int opType = Integer.parseInt(params[0]);
        Log.v(LOG_TAG, "doInBackground start:" + opType);
        ContentValues value = new ContentValues();
        String[] sequence = characterSequence.split(",");
        int id,displaySequenceValue;
        Log.v(LOG_TAG,"opType = " + opType + ",sequence.length = " + sequence.length);
        for (int i = 0; i < sequence.length; i++) {
            id = Integer.parseInt(sequence[i]);
            displaySequenceValue = (opType == com.jinshu.xuzhi.learnchinese.FragmentCustomLearning.CLEAR_CHARACTER_SEQUENCE_LOADER)?0:(i + 1);
            value.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE, displaySequenceValue);
            mContext.getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(id),
                    value, null, null);
        }
        Log.v(LOG_TAG, "doInBackground finished:" + opType);
        return opType;
    }

    @Override
    protected void onPostExecute(Integer result) {
        int resourceId = (result == com.jinshu.xuzhi.learnchinese.FragmentCustomLearning.CLEAR_CHARACTER_SEQUENCE_LOADER)?R.drawable.play:R.drawable.stop;
        mView.setImageResource(resourceId);
        mView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        /*immediately start to learning cards
        Intent intent = new Intent(mContext, ActivityLearningCards.class).putExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT,"静夜思");
        mContext.startActivity(intent);*/
    }
}
