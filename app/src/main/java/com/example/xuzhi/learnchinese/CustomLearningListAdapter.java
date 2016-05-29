package com.example.xuzhi.learnchinese;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * Created by xuzhi on 2016/3/6.
 */
public class CustomLearningListAdapter extends CursorAdapter {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;
    //private static String learning_status;
    //private static final String UN_START = "未学习";
    //private static final String IN_PROGRESS = "学习中";
    //private static final String DONE = "已学完";
    private CustomLearningActivityFragment mFragment;
    //private int id;
    public CustomLearningListAdapter(Context context, Cursor c, int flags,CustomLearningActivityFragment fragment) {
        super(context, c, flags);
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.custom_learning_list_item, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView text = (TextView)view.findViewById(R.id.invisible_info);
        int idIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID);
        int sequenceIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE);
        String textString = Integer.toString(cursor.getInt(idIndex)) +
                "/" + cursor.getString(sequenceIndex);
        text.setText(textString);

        text = (TextView)view.findViewById(R.id.name);
        int textIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_NAME);
        textString = cursor.getString(textIndex);
        text.setText(textString);
        Utility.setBoldTextStyle(text);

        TextView text1 = (TextView)view.findViewById(R.id.date);
        int textIndex1 = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_DATE);
        String textString1 = cursor.getString(textIndex1);
        text1.setText(textString1);

        TextView percentage =  (TextView)view.findViewById(R.id.percentage);
        int percentageIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_PERCENTAGE);
        percentage.setText(cursor.getString(percentageIndex));

        ImageView learningImage = (ImageView)view.findViewById(R.id.learning_image);
        String learning_status = cursor.getString(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_STATUS));
        switch (learning_status)
        {
            case LearnChineseContract.NO:
                //Log.v(LOG_TAG,"set learning_status NO");
                learningImage.setImageResource(R.drawable.circle_waiting);
                learningImage.setTag(LearnChineseContract.NO);
                //learningStatus.setTextColor(mContext.getResources().getColor(R.color.black));
                break;
            case LearnChineseContract.YES:
                //Log.v(LOG_TAG,"set learning_status YES");
                learningImage.setImageResource(R.drawable.circle_learning);
                learningImage.setTag(LearnChineseContract.YES);
                //learningStatus.setText(IN_PROGRESS);
                //learningStatus.setTextColor(mContext.getResources().getColor(R.color.green));
                break;
            /*case LearnChineseContract.DONE:
                learningStatus.setText(DONE);
                learningStatus.setTextColor(mContext.getResources().getColor(R.color.black));
                break;*/
            default:
                break;
        }
        learningImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view =  (View)v.getParent();
                View grandpaView =  (View)view.getParent();
                //ImageView learningImageView = (ImageView)grandpaView.findViewById(R.id.learning_image);
                ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.marker_progress);
                TextView infoString = (TextView)grandpaView.findViewById(R.id.invisible_info);
                //Log.v(LOG_TAG,"status = " + toString()+ ",infoString = "+ infoString.getText().toString());
                updateLearningStatus((ImageView)v,(ProgressBar)progressBar,infoString.getText().toString());
            }
        });


    }
    void updateLearningStatus(ImageView view,ProgressBar progressBar,String infoString)
    {
        //String status = view.getText().toString();
        String resourceTag = (String)(view.getTag());
        //Log.v(LOG_TAG,"resource = " + resourceTag);
        String customLearningTag = Utility.getCustomLearningTag((FragmentActivity) mFragment.getActivity());
        String[] info = infoString.split("/");
        String idString = info[0];
        String characterSequence = info[1];
        Log.v(LOG_TAG,"customLearningTag = " + customLearningTag);
        switch (resourceTag){
            case LearnChineseContract.NO:
                if (customLearningTag.equals("")){
                    //view.setImageResource(R.drawable.circle_learning);
                    view.setTag(LearnChineseContract.YES);
                    ContentValues value = new ContentValues();
                    value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS,LearnChineseContract.YES);
                    mContext.getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriById(idString), value, null, null);
                    Utility.setCustomLearningTag((FragmentActivity)mFragment.getActivity(),idString);
                    updateCharacterSequence(view,progressBar,mFragment.GENERATE_CHARACTER_SEQUENCE_LOADER, characterSequence);
                }else
                {
                    //do nothing except show the hint
                    Log.v(LOG_TAG,"do nothing but show the hint");
                }
                break;
            case LearnChineseContract.YES:
            {
                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS, LearnChineseContract.NO);
                mContext.getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriById(idString), value, null, null);

                Utility.setCustomLearningTag((FragmentActivity) mFragment.getActivity(), "");
                updateCharacterSequence(view,progressBar,mFragment.CLEAR_CHARACTER_SEQUENCE_LOADER, characterSequence);
                //view.setImageResource(R.drawable.circle_waiting);
                view.setTag(LearnChineseContract.NO);
                break;
            }
/*
            case DONE:
            {
                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS,LearnChineseContract.NO);
                mContext.getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriById(idString), value, null, null);
                view.setText(UN_START);
                break;
            }*/

            default:
                break;
        }
    }


    void updateCharacterSequence(ImageView view,ProgressBar progressBar,int loaderId,String characterSequence)
    {
            view.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            GenerateDisplaySequenceTask task = new GenerateDisplaySequenceTask(mContext,view,progressBar);
            Log.v(LOG_TAG,"characterSequence = " + characterSequence);
            task.execute(Integer.toString(loaderId), characterSequence);
    }
}
