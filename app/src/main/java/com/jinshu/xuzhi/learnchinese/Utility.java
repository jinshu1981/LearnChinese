package com.jinshu.xuzhi.learnchinese;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.data.ReadOnlyDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xuzhi on 2016/3/3.
 */
public class Utility {
    private final static String LOG_TAG = Utility.class.getSimpleName();

    static public Boolean judgeUpdateDb(Context c)  {
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(c));
            String md5 = obj.getString("md5");
            String sha1 = obj.getString("sha1");
            if (md5.equals(getDbMd5(c)) && sha1.equals(getDbSha1(c)))
            {
                Log.v(LOG_TAG,"same database,no need to update.");
                return false;
            }
            else
            {
                setDbMd5andSha1(c, md5, sha1);
                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    static public String loadJSONFromAsset(Context c)
    {
        String json = null;
        try {
            InputStream is = c.getAssets().open("md5.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     * */
    static public void copyDataBase(Context c) throws IOException {
        String dbname = "ChineseCharacterReadOnly.db";
        // Open your local db as the input stream
        InputStream myInput = c.getAssets().open(dbname);
        // Path to the just created empty db
        //String outFileName = getDatabasePath(dbname);
        // Open the empty db as the output stream
        final File dir = new File(c.getFilesDir() + "/data/data/com.jinshu.xuzhi.learnchinese/databases");
        dir.mkdirs(); //create folders where write files
        //final File file = new File(dir, "easyKitchen.db");

        ReadOnlyDbHelper myDbHelper = new ReadOnlyDbHelper(c);
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        db.close();

        OutputStream myOutput = new FileOutputStream("/data/data/com.jinshu.xuzhi.learnchinese/databases/ChineseCharacterReadOnly.db");

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[618496];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
            Log.v(LOG_TAG, "length = " + length + "buffer = " + buffer.toString());
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    static public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }
    static public void setBoldTextStyle(TextView text){
        TextPaint tp = text.getPaint();
        tp.setFakeBoldText(true);
    }
    static public String getCustomLearningTag(FragmentActivity a)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        String tag = settings.getString("customLearningTag", "");
        Log.v(LOG_TAG, "getCustomLearningTag Tag = " + tag);
        return tag;
    }

    static public void setCustomLearningTag(FragmentActivity a,String tag)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("customLearningTag", tag);
        Log.v(LOG_TAG,"setCustomLearningTag Tag = " + tag);
        editor.commit();
    }
    static public String getFirstUseTag(FragmentActivity a)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        String tag = settings.getString("FirstUseTag", "true");
        Log.v(LOG_TAG, "getFirstUseTag Tag = " + tag);
        return tag;
    }
    static public void setFirstUseTag(FragmentActivity a,String tag)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("FirstUseTag", tag);
        Log.v(LOG_TAG,"setFirstUseTag Tag = " + tag);
        editor.commit();
    }
    static public String getDbMd5(Context a)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        String tag = settings.getString("md5", "");
        Log.v(LOG_TAG, "md5 Tag = " + tag);
        return tag;
    }
    static public String getDbSha1(Context a)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        String tag = settings.getString("sha1", "");
        Log.v(LOG_TAG, "sha1 Tag = " + tag);
        return tag;
    }
    static public void setDbMd5andSha1(Context a,String md5,String sha1)
    {
        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("md5", md5);
        editor.putString("sha1", sha1);
        Log.v(LOG_TAG, "md5 Tag = " + md5);
        Log.v(LOG_TAG, "sha1 Tag = " + sha1);

        editor.commit();
    }

    static public String getCurrentDate()
    {
        Locale locale = Locale.getDefault();
       // System.out.println("Locale is : [" + locale + "]"); // make sure there is a default Locale
        Calendar calendar = Calendar.getInstance(locale);
        String date = Integer.toString(calendar.get(Calendar.YEAR)) + "-" +
                Integer.toString(calendar.get(Calendar.MONTH) + 1) + "-" +
                Integer.toString(calendar.get(Calendar.DATE));
        return date;
    }
    static public String generateCharacterName(String sequence) {
        final String format =  "[^\\u4e00-\\u9fa5]";//所有非汉字
        Pattern pattern = Pattern.compile(format);

        String characterName;
        Matcher matcher = pattern.matcher(sequence);
        characterName = matcher.replaceAll("");

        Log.v(LOG_TAG,"characterName = " + characterName );
        return characterName.trim();
    }

    static String getContentTag(String content){
        String pureContent = Utility.generateCharacterName(content);
        String contentTag = "";
        for (int i = 0;i < pureContent.length();i++)
        {
            contentTag += "2";/*characters not in db*/
        }
        return contentTag;
    }
}
