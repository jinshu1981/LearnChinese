package com.example.xuzhi.learnchinese;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class CustomLearningDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final int CUSTOM_LEARNING_DETAIL_LOADER = 0;
    static final int CUSTOM_LEARNING_TEXT_COLOR_LOADER = 1;

    static Cursor mCursor;
    static TextView contentView;
    View mRootView;
    public CustomLearningDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_custom_learning_detail, container, false);
        return mRootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CUSTOM_LEARNING_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        Log.d(LOG_TAG, "onCreateLoader for loader_id " + i);
        CursorLoader cursor;
        String sortOrder = LearnChineseContract.CustomLearning.COLUMN_ID + " ASC";
        if (i == CUSTOM_LEARNING_DETAIL_LOADER) {
            Uri uri = getActivity().getIntent().getData();
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
            return cursor;
        }
        else if (i == CUSTOM_LEARNING_TEXT_COLOR_LOADER){
            Uri uri = LearnChineseContract.Character.buildCharacterUriByIdList(bundle.getString(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE));
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
            return cursor;

        }
        Log.v(LOG_TAG,"onCreateLoader id = " + i );

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if ((cursor==null)||(cursor.getCount() == 0))
        {
            Log.v(LOG_TAG, " return cursorLoader.getId()" + cursorLoader.getId());
            return;
        }

        Log.v(LOG_TAG, "onLoadFinished");
        cursor.moveToFirst();
        int cursorId = cursorLoader.getId();
        if (cursorId == CUSTOM_LEARNING_DETAIL_LOADER) {

            mCursor = cursor;
            TextView name = (TextView) mRootView.findViewById(R.id.custom_learning_name);
            name.setText(cursor.getString(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_NAME)));
            Utility.setBoldTextStyle(name);

            TextView date = (TextView) mRootView.findViewById(R.id.custom_learning_date);
            date.setText(cursor.getString(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_DATE)));

            contentView =  (TextView)mRootView.findViewById(R.id.custom_learning_content);

            /*String characterSequence = cursor.getString(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE));
            Bundle bundle = new Bundle();
            bundle.putString(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE, characterSequence);
            getLoaderManager().initLoader(CUSTOM_LEARNING_TEXT_COLOR_LOADER, bundle, this);*/
            String content = mCursor.getString(mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT));
            Log.v(LOG_TAG,"content len =" + content.length());
            String contentTag = cursor.getString(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT_TAG));
            String[] contentTagArray = contentTag.split("");

            //文本内容
            SpannableString ss = new SpannableString(content);
            final String format =  "[\\u4e00-\\u9fa5]";//所有汉字
            Pattern pattern = Pattern.compile(format);
            Matcher matcher = pattern.matcher(content);
            //设置0-2的字符颜色
            for (int i = 0,j = 1;i < content.length();i++)
            {
                if (matcher.find())
                {
                    //Log.v(LOG_TAG,"i = " + i + "content = " + content.subSequence(i,i+1)+"start = " + matcher.start());
                    int color;
                    switch (contentTagArray[j])
                    {
                        case "0":
                            color = getResources().getColor(R.color.black);
                            break;
                        case "1":
                            color = getResources().getColor(R.color.green);
                            break;
                        case "2":
                            color = getResources().getColor(R.color.lightpink);
                            break;
                        default:
                            color = getResources().getColor(R.color.lightpink);
                            break;
                    }
                    //int color = contentTagArray[j].equals("1")?getResources().getColor(R.color.green):getResources().getColor(R.color.black);
                    ss.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.start()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    j++;
                    if ((j-1) > content.length()){
                        Log.v(LOG_TAG,"j-1 = " + (j-1) + "i = " + i);
                        //break;
                    }
                }
            }
            //ProgressBar progressBar =  (ProgressBar)mRootView.findViewById(R.id.marker_progress);
            //progressBar.setVisibility(View.GONE);
            contentView.setText(ss);


        }
        else if (cursorId == CUSTOM_LEARNING_TEXT_COLOR_LOADER)
        {
            int cursorCount = cursor.getCount();
            String[][] textColor = new String[cursorCount][2];
            for (int i = 0;i < cursor.getCount();i++)
            {
                textColor[i][0] = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME));
                textColor[i][1] = cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ));
                //Log.v(LOG_TAG,"i =" + i + ",textColor[0] = "+ textColor[i][0]+ ",textColor[1] = "+ textColor[i][1]);
                cursor.moveToNext();
            }
            String content = mCursor.getString(mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT));
            String []contentColor = new String[content.length()];
            for (int i = 0;i < content.length();i++)
            {
                contentColor[i] = LearnChineseContract.NO;
                for (int j = 0;j < textColor.length;j++)
                {
                    if (content.substring(i,i+ 1).equals(textColor[j][0]))
                    {
                        contentColor[i] = textColor[j][1];
                        break;
                    }
                }
            }
            //Log.v(LOG_TAG,"contentColor = " + contentColor.toString());
            //文本内容
            SpannableString ss = new SpannableString(content);
            //设置0-2的字符颜色
            for (int i = 0;i < content.length();i++)
            {
                int color = contentColor[i].equals(LearnChineseContract.YES)?getResources().getColor(R.color.green):getResources().getColor(R.color.black);
                ss.setSpan(new ForegroundColorSpan(color), i, i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            //ProgressBar progressBar =  (ProgressBar)mRootView.findViewById(R.id.marker_progress);
            //progressBar.setVisibility(View.GONE);
            contentView.setText(ss);

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}
