package com.jinshu.xuzhi.learnchinese;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jinshu.xuzhi.learnchinese.AdapterLearnedCharacters;
import com.jinshu.xuzhi.learnchinese.data.LearnChineseContract;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAbilityTest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAbilityTest extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static int startSequenceIndex = 1;
    static int testCharactersPerPage = 0;
    static String learningTitle = "";
    View rootView;
    GridView abilityTestCharactersGridView;
    ImageView finishImageView;
    static TextView learnedCharacterTextView;
    static int learnedCharacterNum = 0;
    AdapterLearnedCharacters mAdapter;
    static ArrayList mLearnedCharactersList = new ArrayList();
    static ArrayList mLearnedCharactersNameList = new ArrayList();
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final int ABILITY_TEST_CHARACTER_LOADER = 0;
    //static final int GET_LEARNING_CHARACTERS_LOADER = 1;
    static FragmentAbilityTest mThis;
    static boolean showTag = true;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAbilityTest.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAbilityTest newInstance(String param1, String param2) {
        FragmentAbilityTest fragment = new FragmentAbilityTest();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentAbilityTest() {
        // Required empty public constructor
        mThis = this;
    }
    @Override
    public void onDestroy()
    {
        learnedCharacterNum = 0;
        super.onDestroy();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_ability_test, container, false);
        abilityTestCharactersGridView = (GridView)rootView.findViewById(R.id.ability_test_characters);
        finishImageView = (ImageView)rootView.findViewById(R.id.finish);
        final ImageView footerView = (ImageView)rootView.findViewById(R.id.nextPage);
        mAdapter = new AdapterLearnedCharacters(getActivity(),null,0,LOG_TAG);
        abilityTestCharactersGridView.setAdapter(mAdapter);
        learnedCharacterTextView = (TextView)rootView.findViewById(R.id.learned_number);
        abilityTestCharactersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                    int characterId = cursor.getInt(idIndex);
                    int nameIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME);
                    String characterName = cursor.getString(nameIndex);
                    TextView characterView = (TextView) (view.findViewById(R.id.LearnedCharacter));

                    if (mLearnedCharactersList.contains(characterId)) {
                        mLearnedCharactersList.remove((Integer) characterId);
                        mLearnedCharactersNameList.remove(characterName);
                        characterView.setTextColor(getResources().getColor(R.color.black));
                        Log.v(LOG_TAG, "set black " + characterView.getText().toString());
                        learnedCharacterNum--;
                    } else {
                        mLearnedCharactersList.add(characterId);
                        mLearnedCharactersNameList.add(characterName);
                        characterView.setTextColor(getResources().getColor(R.color.green));
                        Log.v(LOG_TAG,"set green " + characterView.getText().toString());
                        learnedCharacterNum++;
                    }
                    learnedCharacterTextView.setText(Integer.toString(learnedCharacterNum));
                }
            }
        });
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.Character.COLUMN_READ, LearnChineseContract.YES);
                int charactersId = 0;
                /*update learned characters db table*/
                for (int i = 0; i < mLearnedCharactersList.size(); i++) {
                    charactersId = (int) mLearnedCharactersList.get(i);
                    getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(charactersId), value, null, null);
                }

                /*calculate percentage and generate display color*/
                String totalCharactersName = TextUtils.join("", mLearnedCharactersNameList.toArray());
                Log.v(LOG_TAG, "totalCharactersName = " + totalCharactersName);
                com.jinshu.xuzhi.learnchinese.TaskCalculatePercentage task = new com.jinshu.xuzhi.learnchinese.TaskCalculatePercentage(getActivity());
                task.execute(totalCharactersName, LearnChineseContract.YES);

                /*load next page*/
                Bundle bundle = new Bundle();
                bundle.putInt("startIndex", startSequenceIndex);
                getLoaderManager().restartLoader(ABILITY_TEST_CHARACTER_LOADER, bundle, mThis);

            }
        });
        finishImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.Character.COLUMN_READ, LearnChineseContract.YES);
                int charactersId = 0;
                /*update learned characters db table*/
                for (int i = 0; i < mLearnedCharactersList.size(); i++) {
                    charactersId = (int) mLearnedCharactersList.get(i);
                    getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(charactersId), value, null, null);
                }

                /*calculate percentage and generate display color*/
                String totalCharactersName = TextUtils.join("", mLearnedCharactersNameList.toArray());
                Log.v(LOG_TAG, "totalCharactersName = " + totalCharactersName);
                com.jinshu.xuzhi.learnchinese.TaskCalculatePercentage task = new com.jinshu.xuzhi.learnchinese.TaskCalculatePercentage(getActivity());
                task.execute(totalCharactersName, LearnChineseContract.YES);

                /*finish the test*/
                new TestResultDialogFragment().show(getFragmentManager(), "TestResultDialogFragment");

            }
        });

        abilityTestCharactersGridView.setOnScrollListener(new GridView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0)
                    return;
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    // last item in grid is on the screen, show footer:
                    footerView.setVisibility(View.VISIBLE);

                } else if (footerView.getVisibility() != View.GONE) {
                    // last item in grid not on the screen, hide footer:
                    footerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view,
                                             int scrollState) {
                Log.v(LOG_TAG, "onScrollStateChanged ");
            }
        });
       /* if (showTag){
        new HintDialogFragment().show(getFragmentManager(),"hint");}*/
        return rootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putInt("startIndex",0);
        getLoaderManager().initLoader(ABILITY_TEST_CHARACTER_LOADER, bundle, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        Log.d(LOG_TAG, "onCreateLoader for loader_id " + i);
        CursorLoader cursor;
        if (i == ABILITY_TEST_CHARACTER_LOADER) {
            int startIndex = bundle.getInt("startIndex");
            Log.v(LOG_TAG, "startIndex = " + Integer.toString(startIndex));

            String sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC" + " LIMIT 50";
            Uri uri = LearnChineseContract.Character.buildCharacterUriByIdAndRead(Integer.toString(startIndex), LearnChineseContract.NO);
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }
        /*else if (i == GET_LEARNING_CHARACTERS_LOADER){
            String title = bundle.getString("title");
            Log.v(LOG_TAG, "title = " + title);
            String sortOrder = LearnChineseContract.CustomLearning.COLUMN_ID + " ASC" ;
            Uri uri = LearnChineseContract.CustomLearning.buildCustomLearningUriByName(title);
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }*/
        else
        { cursor = null;}

        Log.v(LOG_TAG,"onCreateLoader id = " + i );
        return cursor;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        //TextView hint = (TextView)rootView.findViewById(R.id.hint);
        if ((cursor==null)||(cursor.getCount() == 0))
        {
            Log.v(LOG_TAG, " return cursorLoader.getId()" + cursorLoader.getId());
            return;
        }

        int cursorId = cursorLoader.getId();
        Log.v(LOG_TAG, "onLoadFinished,cursorId = " + cursorId);
        if (cursorId == ABILITY_TEST_CHARACTER_LOADER) {
            mLearnedCharactersList.clear();
            mLearnedCharactersNameList.clear();
            testCharactersPerPage = cursor.getCount();
            Log.v(LOG_TAG,"cursor.getCount() = " + cursor.getCount());

            /*获取下一页面起始id*/
            cursor.moveToLast();
            int sequenceIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE);
            int sequence = cursor.getInt(sequenceIndex);
            startSequenceIndex = sequence;

            cursor.moveToFirst();
            mAdapter.swapCursor(cursor);

            abilityTestCharactersGridView.smoothScrollToPosition(0);
        }
        /*else if (cursorId == GET_LEARNING_CHARACTERS_LOADER)
        {
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE);
            Log.v(LOG_TAG,"idIndex = " + idIndex);
            String idSequence = cursor.getString(idIndex);
            ContentValues value = new ContentValues();
            String []sequence = idSequence.split(",");

            for (int i = 0;i < sequence.length;i++) {
                int id = Integer.parseInt(sequence[i]);
                value.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE, i + 1);
                getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(id),
                        value, null, null);
            }

        }*/

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
    public static class TestResultDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_test_result_title);
            builder.setMessage("本次您新增了"+ learnedCharacterNum +"个认识的汉字");
            builder.setNegativeButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Log.v("builder", "SimpleName = " + getActivity().getClass().getSimpleName() + ",MainActivity = " + com.jinshu.xuzhi.learnchinese.MainActivity.class);

                    if (com.jinshu.xuzhi.learnchinese.Utility.getFirstUseTag(getActivity()).equals("true")) {
                        //new SelectLearningDialogFragment().show(getFragmentManager(), "SelectLearningDialogFragment");
                        com.jinshu.xuzhi.learnchinese.Utility.setFirstUseTag(getActivity(), "false");
                    } /*else {*/
                        /*返回学习界面*/
                        Fragment fragment = new com.jinshu.xuzhi.learnchinese.FragmentCustomLearning();
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.output, fragment);
                        transaction.commit();

                        /*ImageView study = (ImageView) ((ViewGroup) getActivity()
                                .findViewById(android.R.id.content)).findViewById(R.id.image_study);*/
                        ImageView learned = (ImageView) ((ViewGroup) getActivity()
                                .findViewById(android.R.id.content)).findViewById(R.id.image_learned);
                        ImageView courses = (ImageView) ((ViewGroup) getActivity()
                                .findViewById(android.R.id.content)).findViewById(R.id.image_courses);
                        ImageView test = (ImageView) ((ViewGroup) getActivity()
                                .findViewById(android.R.id.content)).findViewById(R.id.image_test);
                        //study.setImageResource(R.drawable.openbook_green_24);
                        learned.setImageResource(R.drawable.blackflag_24);
                        courses.setImageResource(R.drawable.addlist_24);
                        test.setImageResource(R.drawable.books_24);
                   /* }*/

                }
            });


            return builder.create();
        }
    }

   /* public  static class SelectLearningDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Log.v("DIALOG", "SelectLearningDialogFragment");
            builder.setTitle(R.string.dialog_select_learning_title);
            final CharSequence learningList[] = { "小学一年级常用汉字生字表", "小学二年级常用汉字生字表", "小学三年级常用汉字生字表",
                    "小学四年级常用汉字生字表", "小学五年级常用汉字生字表", "小学六年级常用汉字生字表"};
            builder.setSingleChoiceItems(learningList, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    learningTitle = (String) learningList[arg1];

                }
            });
            builder.setNegativeButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ContentValues value = new ContentValues();
                    value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS, LearnChineseContract.YES);
                    getActivity().getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriByName(learningTitle), value, null, null);
                    Utility.setCustomLearningTag(getActivity(), learningTitle);
                    /*get all characters and generate display sequence*/
                  /*  Bundle bundle = new Bundle();
                    bundle.putString("title", learningTitle);
                    getLoaderManager().initLoader(GET_LEARNING_CHARACTERS_LOADER, bundle, mThis);

                    /*jump to mainActivity*/
                   /* Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });


            return builder.create();
        }
    }*/

    /*public static class HintDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_notice_title);
            //builder.setView(R.id.ability_test_hint);
            builder.setMessage("单击汉字\n绿色字代表认识；\n黑色字代表不认识；\n向下箭头代表刷新页面；\n停止键代表结束测试。");

            builder.setNegativeButton(R.string.dialog_not_show, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showTag = false;
                }
            });
            builder.setPositiveButton(R.string.dialog_I_known, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            return builder.create();
        }
    }*/
}
