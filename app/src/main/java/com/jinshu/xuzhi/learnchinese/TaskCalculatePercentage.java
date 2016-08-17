package com.jinshu.xuzhi.learnchinese;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.jinshu.xuzhi.learnchinese.Utility;
import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;
import com.jinshu.xuzhi.learnchinese.data.LearnChineseDbHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xuzhi on 2016/5/20.
 */
public class TaskCalculatePercentage extends AsyncTask<String, Void, Void>{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final Context mContext;
    public TaskCalculatePercentage(Context context) {
        mContext = context;

    }

    @Override
    protected Void doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        Log.v(LOG_TAG,"doInBackground");
        String characters = params[0];
        String[]charactersArray = characters.split("");/*{"","a","b",...}*/
        String readFlag = params[1];
        LearnChineseDbHelper myDbHelper = new LearnChineseDbHelper(mContext);
        Cursor cursor = myDbHelper.getAllCustomLearningItem();
        if ((cursor == null)||(cursor.getCount() == 0))
        {
            Log.e(LOG_TAG, "invalid cursor");
            return null;
        }
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount();i++)
        {
            int idIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID);
            int id = cursor.getInt(idIndex);
            int nameIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_NAME);
            String name = cursor.getString(nameIndex);
            int contentIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
            String content = cursor.getString(contentIndex);
            int statusIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_STATUS);
            String status = cursor.getString(statusIndex);
            String pureContent = Utility.generateCharacterName(content);

            int percentageIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_PERCENTAGE);
            String percentage = cursor.getString(percentageIndex);
            String[] percentArray = percentage.split("/");
            int learnedNum = Integer.parseInt(percentArray[0]);

            int contentTagIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT_TAG);
            String contentTag = cursor.getString(contentTagIndex);
            //Log.v(LOG_TAG,"1contentTag = " + contentTag);
            String[]contentTagArray = contentTag.split("");//{"","a","b","c",...}
            int step = (readFlag == LearnChineseContract.YES) ? 1 : -1;
            int count = 0;
            /*在一条学习记录中为每个汉字计算一次*/
            for (int j = 0;j < charactersArray.length;j++) {
                if(charactersArray[j].equals("")) continue;

                Pattern p = Pattern.compile(charactersArray[j]);
                Matcher m = p.matcher(pureContent);
                Log.v(LOG_TAG,"charactersArray[" + j + "] = " + charactersArray[j]);
                count = 0;
                while (m.find()) {
                    learnedNum += step;
                    Log.v(LOG_TAG, "m.start() = " + m.start());
                    contentTagArray[m.start() + 1] = contentTagArray[m.start() + 1].equals("1") ? "0" : "1";
                    count++;
                }

            }
            if (count > 0) {
                contentTag = TextUtils.join("", contentTagArray);
                percentage = Integer.toString(learnedNum) + "/" + percentArray[1];
                Log.v(LOG_TAG,"percentage = " + percentage + ",status = "  +status);
                myDbHelper.updateCustomLearningPercentage(id, percentage, contentTag);

                if ((!status.equals(LearnChineseContract.FINISHED))&&(learnedNum == Integer.parseInt(percentArray[1])))
                {
                    myDbHelper.updateLearningStatus(id,LearnChineseContract.FINISHED);
                    /*if (status.equals(LearnChineseContract.YES))
                    {
                        Utility.setCustomLearningTag((FragmentActivity)mContext,"");
                        String[] Characters = pureContent.split("");
                        ContentValues value = new ContentValues();
                        for (int index = 0; index < Characters.length; index++) {
                            if (Characters[index].equals("")) continue;
                            value.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE, 0);
                            mContext.getContentResolver().update(LearnChineseContract.Character.buildCharacterUriByName(Characters[index]),
                                    value, null, null);
                        }
                    }*/
                }

                else if ((status.equals(LearnChineseContract.FINISHED)) && (learnedNum != Integer.parseInt(percentArray[1])))
                {
                    myDbHelper.updateLearningStatus(id,LearnChineseContract.NO);
                }

                //Log.v(LOG_TAG, "after name = " + name + ",character = " + charactersArray[j] + ",step = " + step + ",count = " + count + ",contentTag = " + contentTag);
            }
            cursor.moveToNext();

        }
        cursor.close();
        myDbHelper.close();

        return null;
    }

    }
