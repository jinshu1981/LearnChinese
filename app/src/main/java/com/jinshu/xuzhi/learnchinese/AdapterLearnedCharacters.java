package com.jinshu.xuzhi.learnchinese;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * Created by xuzhi on 2016/3/17.
 */
public class AdapterLearnedCharacters extends CursorAdapter{
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context mContext;
    private String mTag = "";
    static Typeface tf1;

    public AdapterLearnedCharacters(Context context, Cursor c, int flags, String tag) {
        super(context, c, flags);
        mContext = context;
        mTag = tag;
        tf1 = Typeface.createFromAsset(mContext.getAssets(), "fonts/simkai.ttf");
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
        com.jinshu.xuzhi.learnchinese.Utility.setBoldTextStyle(text);
        text.setTypeface(tf1);
        if (mTag.equals("LearnedCharactersActivityFragment")){
            text.setTextColor(mContext.getResources().getColor(R.color.green));
        }
        else
        {
            text.setTextColor(mContext.getResources().getColor(R.color.black));
        }
    }

}
