package com.jinshu.xuzhi.learnchinese;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xuzhi on 2016/6/12.
 */
public class AdapterCourseDetail extends BaseAdapter {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private String mContent,mPureContent,mContentFlag;
    private Context mContext ;
    private String[] mContentFlagArray;
    public AdapterCourseDetail(Context context, String content,String contentFlag) {
        super();
        mContext = context;
        mContent = content;
        mContentFlag = contentFlag;
        mPureContent = Utility.generateCharacterName(content);
        mContentFlagArray = mContentFlag.split("");
        Log.v(LOG_TAG,"mContent = " + mContent);
        Log.v(LOG_TAG,"mContentFlag = " + mContentFlag);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView; // 声明ImageView的对象
        if (convertView == null) {
            textView = new TextView(mContext); // 实例化ImageView的对象
            //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // 设置缩放方式
            textView.setPadding(5, 0, 5, 0); // 设置ImageView的内边距
        } else {
            textView = (TextView) convertView;
        }
        String currentText = mContent.substring(position, (position + 1));
        textView.setText(currentText);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
        
        String flag = getColorFlag(currentText,mPureContent,mContentFlagArray);
        Log.v(LOG_TAG, "text = " + currentText + ",flag = " + flag);
        int color;
        switch (flag)
        {
            case "0":
                color = mContext.getResources().getColor(R.color.white);
                break;
            case "1":
                color = mContext.getResources().getColor(R.color.lime);
                break;
            case "2":
                color = mContext.getResources().getColor(R.color.violet);
                break;
            default:
                color = mContext.getResources().getColor(R.color.violet);
                break;
        }
        textView.setTextColor(color);
        int pureContentPosition = getPureContentPosition(position, mContent,mPureContent, currentText);
        textView.setTag(pureContentPosition);

        /*textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = (int) v.getTag();
                if (position != -1) {
                    Intent intent = new Intent(mContext, ActivityLearningCards.class)
                            .putExtra(LearnChineseContract.CustomLearning.COLUMN_CONTENT, mPureContent)
                            .putExtra("index", position);
                    mContext.startActivity(intent);
                }
                return true;
            }
        });*/
        return textView; // 返回ImageView
    }

    /*
     * 功能：获得当前选项的ID
     *
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        //System.out.println("getItemId = " + position);
        return position;
    }

    /*
     * 功能：获得当前选项
     *
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /*
     * 获得数量
     *
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        Log.v(LOG_TAG,"LEN = " + mContent.length());
        return mContent.length();
    }

    public String getColorFlag(String currentText,String mPureContent,String[] contentFlagArray)
    {
        String format =  "[\\u4e00-\\u9fa5]";//所有汉字
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(currentText);
        if (!matcher.find()) return "0";//非汉字显示黑色

        pattern = Pattern.compile(currentText);
        matcher = pattern.matcher(mPureContent);
        if (matcher.find())
        {
            return contentFlagArray[matcher.start() + 1];
        }
        else {
            Log.e(LOG_TAG,"unfind character :" + currentText);
            return "2";
        }
    }

    public int getPureContentPosition(int position,String content,String pureContent,String currentText)
    {
        int pPosition = -1,textNum = 0,pureTextNum = 0;
        //String format =  "[\\u4e00-\\u9fa5]";//所有汉字
        Pattern pattern = Pattern.compile(currentText);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find())
        {
            if (matcher.start() > position) break;

            textNum++;
        }
        matcher = pattern.matcher(pureContent);
        while(matcher.find())
        {
            pureTextNum++;
            if (pureTextNum == textNum)
            {
                pPosition = matcher.start();
                break;
            }
        }
        Log.v(LOG_TAG,"currentText = " + currentText+ ",position = " + position +",pPosition = " + pPosition + ",textNum = " + textNum);
        return pPosition;

    }
}
