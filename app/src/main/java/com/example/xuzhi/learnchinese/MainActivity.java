package com.example.xuzhi.learnchinese;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;
import com.example.xuzhi.learnchinese.data.ReadOnlyDbContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>{
    private final String LOG_TAG = this.getClass().getSimpleName();
    private static final int GET_CHARACTER_READ_ONLY_ID_LOADER = 0;
    private static final int GET_CHARACTER_ID_LOADER = 1;
    private static  LinearLayout studyLayout,learnedLayout,coursesLayout,testLayout;
    private static  ImageView study,learned,courses,test;
    List<Integer> CharacterReadOnlyIdList = new ArrayList<Integer>();/*待升级数据库id列表*/
    List<Integer> CharacterIdList = new ArrayList<Integer>();/*本地汉字数据库id列表*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*action bar 中显示logo，隐藏action bar*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        studyLayout = (LinearLayout) ((ViewGroup) this
                .findViewById(android.R.id.content)).findViewById(R.id.study);
        learnedLayout = (LinearLayout) ((ViewGroup) this
                .findViewById(android.R.id.content)).findViewById(R.id.learned);
        coursesLayout = (LinearLayout) ((ViewGroup) this
                .findViewById(android.R.id.content)).findViewById(R.id.courses);
        testLayout = (LinearLayout) ((ViewGroup) this
                .findViewById(android.R.id.content)).findViewById(R.id.test);

        study = (ImageView)(studyLayout.findViewById(R.id.image_study));
        learned = (ImageView)(learnedLayout.findViewById(R.id.image_learned));
        courses = (ImageView)(coursesLayout.findViewById(R.id.image_courses));
        test = (ImageView)(testLayout.findViewById(R.id.image_test));

        /*学习界面加载结束后才允许点击切换页面*/
        studyLayout.setClickable(false);
        learnedLayout.setClickable(false);
        coursesLayout.setClickable(false);
        testLayout.setClickable(false);

        studyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.output);
                if (f instanceof MainActivityFragment) {
                    return;
                }
                Fragment fragment = new MainActivityFragment();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();
                study.setImageResource(R.drawable.openbook_green_24);
                learned.setImageResource(R.drawable.blackflag_24);
                courses.setImageResource(R.drawable.addlist_24);
                test.setImageResource(R.drawable.books_24);
            }
        });
        learnedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.output);
                if (f instanceof FragmentLearnedCharacters) {
                    return;
                }
                Fragment fragment = new FragmentLearnedCharacters();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();
                study.setImageResource(R.drawable.openbook_24);
                learned.setImageResource(R.drawable.greenflag_24);
                courses.setImageResource(R.drawable.addlist_24);
                test.setImageResource(R.drawable.books_24);
            }
        });
        coursesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.output);
                if (f instanceof FragmentCustomLearning) {
                    return;
                }
                Fragment fragment = new FragmentCustomLearning();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();
                study.setImageResource(R.drawable.openbook_24);
                learned.setImageResource(R.drawable.blackflag_24);
                courses.setImageResource(R.drawable.addlist_green_24);
                test.setImageResource(R.drawable.books_24);
            }
        });
        testLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.output);
                if (f instanceof FragmentAbilityTest) {
                    return;
                }
                Fragment fragment = new FragmentAbilityTest();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();
                study.setImageResource(R.drawable.openbook_24);
                learned.setImageResource(R.drawable.blackflag_24);
                courses.setImageResource(R.drawable.addlist_24);
                test.setImageResource(R.drawable.books_green_24);
            }
        });
        CharacterIdList.clear();
        CharacterReadOnlyIdList.clear();

        if(Utility.judgeUpdateDb(getBaseContext())) {
            try {
                /*load loading fragment*/
                Fragment fragment = new FragmentLoading();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();

                /*update database*/
                Utility.copyDataBase(getBaseContext());
                createCharacterStatusTable(getBaseContext());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Fragment fragment = new MainActivityFragment();
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.output, fragment);
            transaction.commit();
            studyLayout.setClickable(true);
            learnedLayout.setClickable(true);
            coursesLayout.setClickable(true);
            testLayout.setClickable(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

@Override
public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder;
        Uri uri;
        if (GET_CHARACTER_READ_ONLY_ID_LOADER == i) {
            sortOrder = ReadOnlyDbContract.CharacterReadOnly.COLUMN_ID + " ASC";
            uri = ReadOnlyDbContract.CharacterReadOnly.CONTENT_URI;
           // Log.v(LOG_TAG, "GET_CHARACTER_READ_ONLY_ID_LOADER onCreateLoader" );
        }
        else if  (GET_CHARACTER_ID_LOADER == i) {
            sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
            uri = LearnChineseContract.Character.CONTENT_URI;
           // Log.v(LOG_TAG, "GET_CHARACTER_ID_LOADER onCreateLoader" );
        }
        else{
            Log.e(LOG_TAG, "invalid loader id = " + i);
            return null;
        }

        return new CursorLoader(this,
        uri,
        null,
        null,
        null,
        sortOrder);
        }

@Override
public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (cursor == null) {
            //Log.v(LOG_TAG, " return cursorLoader.getId() = " + cursorLoader.getId() + "cursor = " + cursor.toString() );
            return;
            }
        int count = cursor.getCount();
        Log.v(LOG_TAG, " cursor.getCount().getId() = " + cursorLoader.getId() + "count = " + count);
        int id = cursorLoader.getId();
        cursor.moveToFirst();
        if (id == GET_CHARACTER_READ_ONLY_ID_LOADER) {
            //Log.v(LOG_TAG, "GET_CHARACTER_READ_ONLY_ID_LOADER onLoadFinished start" );
            /*获取只读数据库所有数据*/
            ContentValues[] cvArray = new ContentValues[count];
            for (int i = 0; i < count; i++) {
                ContentValues values = new ContentValues();
                values.put(LearnChineseContract.Character.COLUMN_ID, cursor.getInt(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_ID)));
                values.put(LearnChineseContract.Character.COLUMN_NAME, cursor.getString(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_NAME)));
                values.put(LearnChineseContract.Character.COLUMN_PRONUNCIATION, cursor.getString(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_PRONUNCIATION)));
                values.put(LearnChineseContract.Character.COLUMN_MULTITONE, cursor.getString(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_MULTITONE)));
                values.put(LearnChineseContract.Character.COLUMN_READ, cursor.getString(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_READ)));
                values.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE, 0);
                values.put(LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE, cursor.getInt(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_ABILITY_TEST_SEQUENCE)));

                cvArray[i] = values;
                /*获取基础数据库所有字符id*/
                CharacterReadOnlyIdList.add(cursor.getInt(cursor.getColumnIndex(ReadOnlyDbContract.CharacterReadOnly.COLUMN_ID)));
                cursor.moveToNext();
            }
            /*内部数据库没有值，直接插入所有数据*/
            if (CharacterIdList.size() == 0) {
                //Log.v(LOG_TAG, "CharacterIdList is empty." );
                int inserted = getContentResolver().bulkInsert(LearnChineseContract.Character.CONTENT_URI, cvArray);
            }
            else{
                //Log.v(LOG_TAG, "CharacterIdList is not empty.size =" + CharacterIdList.size());
                /*比对并更新内部数据库，仅根据column_id进行比对*/
                List<Integer> CharactersTobeAdd = new ArrayList<Integer>();
                CharactersTobeAdd.addAll(CharacterReadOnlyIdList);
               // Log.v(LOG_TAG, "CharactersTobeAdd size:" + CharactersTobeAdd.size());
                Boolean modified = CharactersTobeAdd.removeAll(CharacterIdList);


                Log.v(LOG_TAG,"CharactersTobeAdd is modified:" + modified.toString());
                if (CharactersTobeAdd.size() > 0)
                {
                    //Log.v(LOG_TAG, "CharactersTobeAdd size =" +  CharactersTobeAdd.size());
                    BulkInsertNewCharacters(CharactersTobeAdd,cvArray);
                }

                List<Integer> CharactersTobeDel = new ArrayList<Integer>();
                CharactersTobeDel.addAll(CharacterIdList);
                //Log.v(LOG_TAG, "CharactersTobeDel size:" + CharactersTobeDel.size());
                modified = CharactersTobeDel.removeAll(CharacterReadOnlyIdList);
                //Log.v(LOG_TAG,"CharactersTobeDel is modified:" + modified.toString());
                if (CharactersTobeDel.size()>0)
                {
                    //Log.v(LOG_TAG, "CharactersTobeDel size =" +  CharactersTobeDel.size());
                    DeleteCharacters(CharactersTobeDel);
                }

            }
            Log.v(LOG_TAG, "GET_CHARACTER_READ_ONLY_ID_LOADER onLoadFinished");
            //第一次使用软件，测试汉字能力，并设置学习内容;否则 转入学习界面
            String firstUseTag = Utility.getFirstUseTag(this);
            if (firstUseTag.equals("true"))
            {
                /*生成默认学习列表，结束后加载测试页面*/
                TaskGenerateDefaultLearningList task = new TaskGenerateDefaultLearningList(this);
                task.execute();
            }
            else
            {
                Fragment fragment = new MainActivityFragment();
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.output, fragment);
                transaction.commit();

                /*进入正常页面后允许切换页面*/
                studyLayout.setClickable(true);
                learnedLayout.setClickable(true);
                coursesLayout.setClickable(true);
                testLayout.setClickable(true);
            }

        }
        /*获取内部数据库所有字符id*/
        else if (id == GET_CHARACTER_ID_LOADER){
            for (int i = 0;i < count;i++)
            {
                CharacterIdList.add(cursor.getInt(cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID)));
                cursor.moveToNext();
            }
            getLoaderManager().initLoader(GET_CHARACTER_READ_ONLY_ID_LOADER, null, this);
            getLoaderManager().destroyLoader(GET_CHARACTER_ID_LOADER);
            //Log.v(LOG_TAG, "GET_CHARACTER_ID_LOADER onLoadFinished,CharacterIdList.size()=" + CharacterIdList.size());
        }
        else{
                Log.e(LOG_TAG, "invalid loader onLoadFinished id = " + id);
            }
        }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

            }
    public void createCharacterStatusTable(Context c){
            /*前提：数据库中被删除对象的ID不会被重复使用*/
            /*首先获取当前数据库所有字符ID，再获取新数据库所有字符ID进行比对*/
        //Log.v(LOG_TAG,"createCharacterStatusTable");
        getLoaderManager().initLoader(GET_CHARACTER_ID_LOADER,null,this);
    };

    public void BulkInsertNewCharacters(List<Integer> CharactersTobeAdd,ContentValues[] cvArray)
    {
        int count = CharactersTobeAdd.size();
        ContentValues[] addArray = new ContentValues[count];
        //Log.v(LOG_TAG,"BulkInsertNewCharacters cvArray.length = " + cvArray.length);
        for (int i = 0; i < count; i++) {
           // Log.v(LOG_TAG,"BulkInsertNewCharacters CharactersTobeAdd.get(i) = " + CharactersTobeAdd.get(i).toString());
            for (int j = 0;j < cvArray.length;j++)
            {
                //Log.v(LOG_TAG,"BulkInsertNewCharacters cvArray[j].getAsInteger(LearnChineseContract.Character.COLUMN_ID) = " + cvArray[j].getAsInteger(LearnChineseContract.Character.COLUMN_ID).toString());
                if (cvArray[j].getAsInteger(ReadOnlyDbContract.CharacterReadOnly.COLUMN_ID).equals(CharactersTobeAdd.get(i)))
                {
                    addArray[i] = cvArray[j];
                    //Log.v(LOG_TAG,"BulkInsertNewCharacters id = " + CharactersTobeAdd.get(i));
                    break;
                }
            }
        }
        int inserted = getContentResolver().bulkInsert(LearnChineseContract.Character.CONTENT_URI, addArray);
    }
    public void DeleteCharacters(List<Integer> CharactersTobeDel)
    {
        int count = CharactersTobeDel.size();
        for (int i = 0; i < count; i++) {
            int id = CharactersTobeDel.get(i);
            Log.v(LOG_TAG,"DeleteCharacters,id = " + id);
            getContentResolver().delete(LearnChineseContract.Character.buildCharacterStatusUriByCharacterId(id),null,null);
        }
    }


}
