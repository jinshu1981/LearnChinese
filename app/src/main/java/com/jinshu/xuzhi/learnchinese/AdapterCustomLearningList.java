package com.jinshu.xuzhi.learnchinese;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.FragmentCustomLearning;
import com.jinshu.xuzhi.learnchinese.Utility;
import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * Created by xuzhi on 2016/3/6.
 */
public class AdapterCustomLearningList extends CursorAdapter {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;
    private FragmentCustomLearning mFragment;
    public AdapterCustomLearningList(Context context, Cursor c, int flags, FragmentCustomLearning fragment) {
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
        int contentIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
        String textString = Integer.toString(cursor.getInt(idIndex)) +
                "/" + cursor.getString(sequenceIndex) + "/" + cursor.getString(contentIndex);
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
               // learningImage.setImageResource(R.drawable.play);
                learningImage.setTag(LearnChineseContract.NO);
                break;
            /*case LearnChineseContract.YES:
                //Log.v(LOG_TAG,"set learning_status YES");
                learningImage.setImageResource(R.drawable.stop);
                learningImage.setTag(LearnChineseContract.YES);
                break;*/
            case LearnChineseContract.FINISHED:
                //learningImage.setImageResource(R.drawable.checkdrawable_mark);
                learningImage.setTag(LearnChineseContract.FINISHED);
                learningImage.setClickable(false);
                break;
            default:
                break;
        }
        learningImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view =  (View)v.getParent();
                View grandpaView =  (View)view.getParent();
                ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.marker_progress);
                TextView infoString = (TextView)grandpaView.findViewById(R.id.invisible_info);
                //Log.v(LOG_TAG,"status = " + toString()+ ",infoString = "+ infoString.getText().toString());
                updateLearningStatus((ImageView)v,(ProgressBar)progressBar,infoString.getText().toString());
            }
        });


    }
    void updateLearningStatus(ImageView view,ProgressBar progressBar,String infoString)
    {
        String[] info = infoString.split("/");
        String content = info[2];
        if (view.getTag().equals(LearnChineseContract.FINISHED))return;
        Intent intent = new Intent(mContext, com.jinshu.xuzhi.learnchinese.ActivityLearningCards.class).putExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT,Utility.generateCharacterName(content));
        mContext.startActivity(intent);
    }


  /*  void updateCharacterSequence(ImageView view,ProgressBar progressBar,int loaderId,String characterSequence)
    {
            view.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            TaskGenerateDisplaySequence task = new TaskGenerateDisplaySequence(mContext,view,progressBar);
            //Log.v(LOG_TAG,"characterSequence = " + characterSequence);
            task.execute(Integer.toString(loaderId), characterSequence);
    }*/
}
