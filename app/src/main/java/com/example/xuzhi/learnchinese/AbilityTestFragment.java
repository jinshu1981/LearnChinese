package com.example.xuzhi.learnchinese;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AbilityTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AbilityTestFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    static int startSequenceIndex = 1;
    static int unknownCharactersNumber = 0;
    static int knownCharactersNumber = 0;
    static int testCharactersPerPage = 0;
    static String learningTitle = "";
    static final int UNKNOWN_CHARACTERS_LIMIT = 10;
    static final int TEST_CHARACTERS_PERPAGE = 0;
    View rootView;
    GridView abilityTestCharactersGridView;
    ImageView continueImage;
    LearnedCharactersAdapter mAdapter;
    static ArrayList mLearnedCharactersList = new ArrayList();
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final int ABILITY_TEST_CHARACTER_LOADER = 0;
    static final int GET_LEARNING_CHARACTERS_LOADER = 1;
    static final int GENERATE_CHARACTERS_DISPLAY_SEQUENCE_LOADER = 2;
    static AbilityTestFragment mThis;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AbilityTestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AbilityTestFragment newInstance(String param1, String param2) {
        AbilityTestFragment fragment = new AbilityTestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AbilityTestFragment() {
        // Required empty public constructor
        mThis = this;
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
        continueImage = (ImageView)rootView.findViewById(R.id.continue_image);
        mAdapter = new LearnedCharactersAdapter(getActivity(),null,0);
        abilityTestCharactersGridView.setAdapter(mAdapter);
        abilityTestCharactersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                    int characterId = cursor.getInt(idIndex);
                    TextView characterView = (TextView) (view.findViewById(R.id.LearnedCharacter));
                    if (mLearnedCharactersList.contains(characterId)) {
                        mLearnedCharactersList.remove((Integer)characterId);
                        characterView.setTextColor(getResources().getColor(R.color.black));
                    } else {
                        mLearnedCharactersList.add(characterId);
                        characterView.setTextColor(getResources().getColor(R.color.green));
                    }
                }
            }
        });
        continueImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unknownCharactersNumber = unknownCharactersNumber  + (testCharactersPerPage - mLearnedCharactersList.size());
                knownCharactersNumber = knownCharactersNumber + mLearnedCharactersList.size();
                Log.v(LOG_TAG,"unknownCharactersNumber = " + unknownCharactersNumber);
                Log.v(LOG_TAG,"unknownCharactersNumber = " + unknownCharactersNumber);
                ContentValues value = new ContentValues();
                value.put(LearnChineseContract.Character.COLUMN_READ, LearnChineseContract.YES);
                int charactersId = 0;
                for(int i = 0;i < mLearnedCharactersList.size();i++) {
                    charactersId = (int)mLearnedCharactersList.get(i);
                    getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(charactersId), value, null, null);
                }
                if (unknownCharactersNumber < UNKNOWN_CHARACTERS_LIMIT)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt("startIndex",startSequenceIndex);
                    getLoaderManager().restartLoader(ABILITY_TEST_CHARACTER_LOADER, bundle, mThis);
                }
                else/*end the test*/
                {

                    new TestResultDialogFragment().show(getFragmentManager(), "TestResultDialogFragment");
                }
            }
        });

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

            String sortOrder = LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE + " ASC" + " LIMIT 100";
            Uri uri = LearnChineseContract.Character.buildCharacterUriByAbilityTestSequenceAndRead(Integer.toString(startIndex), LearnChineseContract.NO);
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }
        else if (i == GET_LEARNING_CHARACTERS_LOADER){
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
        }
        /*else if (i == GENERATE_CHARACTERS_DISPLAY_SEQUENCE_LOADER){
            String title = bundle.getString("title");
            Log.v(LOG_TAG, "title = " + title);
            String sortOrder = LearnChineseContract.CustomLearning.COLUMN_ID + " ASC" ;
            Uri uri = LearnChineseContract.CustomLearning.buildCustomLearningByUriName(title);
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }*/
        else{ cursor = null;}

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
            cursor.moveToFirst();
            testCharactersPerPage = cursor.getCount();
            for (int i = 0; i < cursor.getCount(); i++) {
                int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                int id = cursor.getInt(idIndex);
                mLearnedCharactersList.add(id);
                cursor.moveToNext();
                if (cursor.isLast()) {
                    int sequenceIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ABILITY_TEST_SEQUENCE);
                    int sequence = cursor.getInt(sequenceIndex);
                    startSequenceIndex = sequence;
                }
            }
            cursor.moveToFirst();
            mAdapter.swapCursor(cursor);
        }
        else if (cursorId == GET_LEARNING_CHARACTERS_LOADER)
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

        }

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
            int percent = knownCharactersNumber *100 /(knownCharactersNumber + unknownCharactersNumber);
            builder.setMessage("测试结束！您的认字率达到了" + Integer.toString(percent) +"%");
            builder.setNegativeButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Intent intent = new Intent(getActivity(), CustomLearningActivity.class);
                    //startActivity(intent);
                    new SelectLearningDialogFragment().show(getFragmentManager(), "SelectLearningDialogFragment");
                    //getActivity().finish();
                }
            });


            return builder.create();
        }
    }

    public  static class SelectLearningDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Log.v("DIALOG","SelectLearningDialogFragment");
            builder.setTitle(R.string.dialog_select_learning_title);
            final CharSequence learningList[] = { "小学一年级常用汉字生字表", "小学二年级常用汉字生字表", "小学三年级常用汉字生字表",
                    "小学四年级常用汉字生字表", "小学五年级常用汉字生字表", "小学六年级常用汉字生字表"};
            builder.setSingleChoiceItems(learningList,-1,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                   // ContentValues value = new ContentValues();
                    learningTitle = (String)learningList[arg1];
                    //ContentValues value = new ContentValues();
                   /* value.put(LearnChineseContract.CustomLearning.COLUMN_STATUS, LearnChineseContract.YES);
                    getActivity().getContentResolver().update(LearnChineseContract.CustomLearning.buildCustomLearningUriByName(title), value, null, null);
                    Utility.setCustomLearningTag(getActivity(), title);
                    /*get all characters and generate display sequence*/
                   /* Bundle bundle = new Bundle();
                    bundle.putString("title", title);
                    getLoaderManager().initLoader(GET_LEARNING_CHARACTERS_LOADER, bundle, mThis);*/
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
                    Bundle bundle = new Bundle();
                    bundle.putString("title", learningTitle);
                    getLoaderManager().initLoader(GET_LEARNING_CHARACTERS_LOADER, bundle, mThis);

                    /*jump to mainActivity*/
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });


            return builder.create();
        }
    }
}
