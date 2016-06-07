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
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentCustomLearning extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    View mRootView;
    AdapterCustomLearningList mAdapter;
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final int CUSTOM_LEARNING_LOADER = 0;
    static final int CLEAR_CHARACTER_SEQUENCE_LOADER = 1;
    static final int GENERATE_CHARACTER_SEQUENCE_LOADER = 2;
    static Cursor mCursor;
    static FragmentCustomLearning mThis;
    public FragmentCustomLearning() {
        mThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_custom_learning, container, false);
        mAdapter = new AdapterCustomLearningList(getActivity(),null,0,this);

        ListView listView = (ListView) mRootView.findViewById(R.id.custom_learning_list);
        listView.setAdapter(mAdapter);
        Utility.setListViewHeightBasedOnChildren(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int custom_id = cursor.getInt(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID));
                    Log.v(LOG_TAG, "custom detail id = " + custom_id);
                    Intent intent = new Intent(getActivity(), ActivityCustomLearningDetail.class).setData(LearnChineseContract.CustomLearning.buildCustomLearningUriById(custom_id));
                    startActivity(intent);
                }
            }
        });

        //delete or edit custom item
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                EditOrDeleteTheItem(cursor);
                return true;
            }

        });

        //add new learning item
        LinearLayout add = (LinearLayout)mRootView.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ActivityAddCustomLearning.class);
                startActivity(intent);
            }
        });
        return mRootView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CUSTOM_LEARNING_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        Log.d(LOG_TAG, "onCreateLoader for loader_id " + i);

        CursorLoader cursor = null;
        if (i == CUSTOM_LEARNING_LOADER) {
            String sortOrder = LearnChineseContract.CustomLearning.COLUMN_ID + " ASC";
            Uri uri = LearnChineseContract.CustomLearning.CONTENT_URI;
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }
        else if (i == CLEAR_CHARACTER_SEQUENCE_LOADER)
        {
            String sortOrder = LearnChineseContract.Character.COLUMN_ID + " ASC";
            Uri uri = LearnChineseContract.Character.buildCharacterUriByDisplaySequence("0");
            cursor = new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    sortOrder);
        }

        return cursor;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        int cursorCount = cursor.getCount();
        if ((cursor==null)||(cursorCount == 0))
        {
            Log.v(LOG_TAG, " return cursorLoader.getId()" + cursorLoader.getId());
            return;
        }
        int id = cursorLoader.getId();
        if (id == CUSTOM_LEARNING_LOADER) {
            mAdapter.swapCursor(cursor);
        }
        else if (id == CLEAR_CHARACTER_SEQUENCE_LOADER){
            cursor.moveToFirst();
            ContentValues value = new ContentValues();
            value.put(LearnChineseContract.Character.COLUMN_DISPLAY_SEQUENCE,0);
            Log.v(LOG_TAG,"CLEAR_CHARACTER_SEQUENCE_LOADER cursorCount = " + cursorCount);
            for (int i = 0;i < cursorCount;i++)
            {
                getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(cursor.getInt(cursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID))),
                        value,null,null);
                cursor.moveToNext();
            }

        }
        else{

        }
        Log.v(LOG_TAG, "onLoadFinished id = " + id);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        int id = cursorLoader.getId();
        if (id == CUSTOM_LEARNING_LOADER)
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }


    private void EditOrDeleteTheItem(Cursor cursor)
    {
        mCursor = cursor;
        new EditOrDeleteDialogFragment().show(getFragmentManager(),"EditOrDeleteDialog");
    }
    public static class EditOrDeleteDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_edit_or_delete_title);
            builder.setNegativeButton(R.string.dialog_edit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    int idIndex = mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID);
                    int id = mCursor.getInt(idIndex);
                    int nameIndex = mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_NAME);
                    String name = mCursor.getString(nameIndex);
                    int contentIndex = mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CONTENT);
                    String content = mCursor.getString(contentIndex);

                    /*如果被修改的内容正在被学习，清除相关内容*/
                    if (Integer.toString(id).equals(Utility.getCustomLearningTag(getActivity())))  {
                        Bundle bundle = new Bundle();
                        bundle.putString("characterSequence",mCursor.getString(mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE)));
                        getLoaderManager().restartLoader(CLEAR_CHARACTER_SEQUENCE_LOADER, bundle, mThis);
                    }

                    String[] para = new String[3];
                    para[0] = Integer.toString(id);
                    para[1] = name;
                    para[2] = content;
                    Intent intent = new Intent(getActivity(), ActivityAddCustomLearning.class).putExtra(Intent.EXTRA_TEXT, para);

                    startActivity(intent);


                }
            });
            builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int idIndex = mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_ID);
                    int id = mCursor.getInt(idIndex);
                    /*如果被删除的内容正在被学习，清楚相关内容*/
                    if (Integer.toString(id).equals(Utility.getCustomLearningTag(getActivity())))  {
                        Utility.setCustomLearningTag(getActivity(),"");
                        Bundle bundle = new Bundle();
                        bundle.putString("characterSequence",mCursor.getString(mCursor.getColumnIndex(LearnChineseContract.CustomLearning.COLUMN_CHARACTER_SEQUENCE)));
                        getLoaderManager().restartLoader(CLEAR_CHARACTER_SEQUENCE_LOADER, bundle, mThis);
                    }
                    getActivity().getContentResolver().delete(LearnChineseContract.CustomLearning.buildCustomLearningUriById(id), null, null);
                    //getLoaderManager().restartLoader(RECIPE_LOADER_CUSTOM, null, mThis);
                    mCursor.requery();

                }
            });
            return builder.create();
        }
    }
}
