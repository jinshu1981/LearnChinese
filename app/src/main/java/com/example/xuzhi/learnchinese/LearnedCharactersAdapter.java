package com.example.xuzhi.learnchinese;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * Created by xuzhi on 2016/3/17.
 */
public class LearnedCharactersAdapter extends CursorAdapter{
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;
    static Typeface tf1;
    //private static final String UN_START = "unStart";
    //private static final String IN_PROGRESS = "inProgress";
    //private static final String DONE = "done";
    //private CustomLearningActivityFragment mFragment;
    //private int id;
    public LearnedCharactersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        tf1 = Typeface.createFromAsset(mContext.getAssets(), "fonts/simkai.ttf");
        //mFragment = fragment;
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.learned_characters_grid_item, parent, false);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text = (TextView)view.findViewById(R.id.LearnedCharacter);
        int textIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_NAME);
        String textString1 = cursor.getString(textIndex);
        text.setText(textString1);
        Utility.setBoldTextStyle(text);
        //Typeface tf1 = Typeface.createFromAsset(mContext.getAssets(), "fonts/simkai.ttf");//设置字体为楷体
        text.setTypeface(tf1);
        text.setTextColor(mContext.getResources().getColor(R.color.green));
    }

}
