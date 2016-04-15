package com.example.xuzhi.learnchinese;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddCustomLearningActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    View rootView;
    EditText name,content;
    Button confirmButton;
    CustomLearningInfo currentInfo;
    static String mCharacterSequence = null;
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int GET_CHARACTER_ID_LOADER = 0;

    static final int GENERATE_CHARACTER_SEQUENCE_LOADER = 2;
    AddCustomLearningActivityFragment mThis;
    Boolean isNewCustom = true;
    int mCustomId = 0;
    class CustomLearningInfo{
        String name;
        String content;
        String date;
        String characterNames;
        public CustomLearningInfo(String n,String c,String d,String cn)
        {
            name = n;
            content = c;
            date = d;
            characterNames = cn;
        }
    }

    public AddCustomLearningActivityFragment() {
        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_custom_learning, container, false);
        name = (EditText)rootView.findViewById(R.id.name);
        content = (EditText)rootView.findViewById(R.id.content);
        confirmButton = (Button)rootView.findViewById(R.id.add_custom_learning);

         /*编辑自定义学习内容时，intent中传入数据*/
        if (getActivity().getIntent().hasExtra(Intent.EXTRA_TEXT))
        {
            isNewCustom = false;
            String[] customInfo = getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

            Log.v(LOG_TAG, "custom.id =" + customInfo[0]+","+
                    "custom.Name = " + customInfo[1] +","+
                    "custom.Content = " + customInfo[2] );
            mCustomId = Integer.parseInt(customInfo[0]);
            //String[] recipes = recipe.split("@@");
            name.setText(customInfo[1]);
            content.setText(customInfo[2]);
        }
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String theName = name.getText().toString().trim();
                String theContent = content.getText().toString().trim();
                if (theContent.isEmpty() || theName.isEmpty()) {
                    Toast.makeText(getActivity(), "名称和内容都不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkChinese(theContent)) {
                    Toast.makeText(getActivity(), "内容必须包含汉字", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentInfo = new CustomLearningInfo(theName, theContent, getCurrentDate(), generateCharacterName(theContent));
                Log.v(LOG_TAG, "theContent = " + theContent);
                getLoaderManager().initLoader(GET_CHARACTER_ID_LOADER, null, mThis);
            }
        });

        return rootView;
    }
    public boolean checkChinese(String sequence) {
        final String format =  "[\\u4e00-\\u9fa5]";
        boolean result;
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(sequence);
        result = matcher.find();
        return result;
    }

    public String generateCharacterName(String sequence) {
        final String format =  "[^\\u4e00-\\u9fa5]";//所有非汉字
        Pattern pattern = Pattern.compile(format);

        String characterName;
        Matcher matcher = pattern.matcher(sequence);
        characterName = matcher.replaceAll("");
        // Log.v(LOG_TAG," sequence  = " + sequence );
       // Log.v(LOG_TAG," matcher.groupCount()  = " +  matcher.groupCount() );
       /* for (int i = 0;i < matcher.groupCount();i++)
        {
            characterName = characterName + matcher.group(i).toString() +",";
        }*/
        Log.v(LOG_TAG,"characterName = " + characterName );
        return characterName.trim();
    }
    public String getCurrentDate()
    {
        Locale locale = Locale.getDefault();
        System.out.println("Locale is : [" + locale + "]"); // make sure there is a default Locale
        Calendar calendar = Calendar.getInstance(locale);
        String date = Integer.toString(calendar.get(Calendar.YEAR)) + "-" +
                Integer.toString(calendar.get(Calendar.MONTH) + 1) + "-" +
                Integer.toString(calendar.get(Calendar.DATE));
        return date;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri uri = null;
        String sortOrder = "";
        if (i == GET_CHARACTER_ID_LOADER) {
            sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
            uri = LearnChineseContract.Character.buildCharacterUriByNameList(currentInfo.characterNames);
            return new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }
        else if (i == GENERATE_CHARACTER_SEQUENCE_LOADER){
            if (bundle == null) return null;
            sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
            mCharacterSequence = bundle.getString("characterSequence");
            Log.v(LOG_TAG,"mCharacterSequence = " + mCharacterSequence);
            uri = LearnChineseContract.Character.buildCharacterUriByIdList(mCharacterSequence);
            return new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);

        }
        else{
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        int cursorCount = cursor.getCount();
        if ((cursor == null)||(cursorCount == 0)) {
            Log.v(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId());
            return;
        }
        int id = cursorLoader.getId();
        if (id == GET_CHARACTER_ID_LOADER) {
            String characterIdSequence = "";
            cursor.moveToFirst();
            String[] charactersNameArray = currentInfo.characterNames.trim().split("");//转换结果会多出一个""
            for (int i = 0; i < charactersNameArray.length; i++) {
                int j = 0;
                Log.v(LOG_TAG, "i = " + i + ",charactersNameArray[i] = " + charactersNameArray[i]);
                if (charactersNameArray[i].isEmpty()) continue;
                for (j = 0; j < cursorCount; j++) {
                    if (cursor.getString(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME)).equals(charactersNameArray[i])) {

                        characterIdSequence = characterIdSequence + Integer.toString(cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID))) + ",";
                        cursor.moveToFirst();
                        break;
                    } else {
                        cursor.moveToNext();
                    }
                }
                if (j == cursorCount) {
                    characterIdSequence = characterIdSequence + "0" + ",";
                    cursor.moveToFirst();
                }
            }
            characterIdSequence = characterIdSequence.substring(0, characterIdSequence.length() - 1);//去除尾部逗号
            Log.v(LOG_TAG, "characterIdSequence = " + characterIdSequence);

            ContentValues value = new ContentValues();
            value.put(LearnChineseContract.CustomLearning.COLUMN_NAME, currentInfo.name);
            value.put(LearnChineseContract.CustomLearning.COLUMN_DATE, currentInfo.date);
            value.put(LearnChineseContract.CustomLearning.COLUMN_CONTENT, currentInfo.content);
            value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS, LearnChineseContract.NO);
            value.put(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE, characterIdSequence);
            if (isNewCustom == true) {
                getActivity().getContentResolver().insert(LearnChineseContract.CustomLearning.CONTENT_URI, value);
            } else {
                //getActivity().getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriById(mCustomId), value, null, null);
                /*更新自定义学习的汉字顺序*/
                if (Integer.toString(mCustomId).equals(Utility.getCustomLearningTag(getActivity()))) {
                    value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS, LearnChineseContract.YES);
                    Bundle bundle = new Bundle();
                    bundle.putString("characterSequence", characterIdSequence);
                    getLoaderManager().restartLoader(GENERATE_CHARACTER_SEQUENCE_LOADER, bundle, this);
                }
                getActivity().getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriById(mCustomId), value, null, null);
            }
            Log.v(LOG_TAG, "onLoadFinished id = " + id);
            //cursor.requery();
            /*back to previous activity*/
            getActivity().finish();
        }
        else if (id == GENERATE_CHARACTER_SEQUENCE_LOADER)
        {
            Log.v(LOG_TAG,"GENERATE_CHARACTER_SEQUENCE_LOADER cursorCount = " + cursorCount);
            cursor.moveToFirst();
            ContentValues value = new ContentValues();
            String []sequence = mCharacterSequence.split(",");

            for (int i = 0;i < cursorCount;i++) {
                int cursorId = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID));
                int sequenceNum = getSequenceNumber(sequence, cursorId);
                Log.v(LOG_TAG,"cursorId = " + cursorId + "sequenceNum = " + sequenceNum);
                value.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE,sequenceNum);
                getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(cursorId),
                        value, null, null);
                cursor.moveToNext();
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    /*return the position of the id ,start from 1,because 0 belong to the characters not in this custom learning*/
    public int getSequenceNumber(String []sequence,int id)
    {
        int i = 1;
        for (String eachId:sequence) {

            if (eachId.equals(Integer.toString(id)))
            {
                Log.v(LOG_TAG,"getSequenceNumber i = " + i);
                return i;
            }
            i++;
        }
        Log.v(LOG_TAG,"getSequenceNumber failed ");
        return 0;
    }

}