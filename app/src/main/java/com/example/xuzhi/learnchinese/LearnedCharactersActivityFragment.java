package com.example.xuzhi.learnchinese;

import android.content.ContentValues;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.xuzhi.learnchinese.data.LearnChineseContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class LearnedCharactersActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    View rootView;
    GridView learnedCharactersGridView;
    LearnedCharactersAdapter mAdapter;
    private final String LOG_TAG = this.getClass().getSimpleName();
    static final int LEARNED_CHARACTER_LOADER = 0;
    public LearnedCharactersActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_learned_characters, container, false);
        learnedCharactersGridView = (GridView)rootView.findViewById(R.id.learned_characters);
        mAdapter = new LearnedCharactersAdapter(getActivity(),null,0);
        learnedCharactersGridView.setAdapter(mAdapter);
        learnedCharactersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                    int cursorId = cursor.getInt(idIndex);
                    int readIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ);
                    String readStatus = cursor.getString(readIndex);
                    Log.v(LOG_TAG,"readStatus = " + readStatus);
                    ContentValues value = new ContentValues();
                    String newStatus = LearnChineseContract.NO;
                    value.put(LearnChineseContract.Character.COLUMN_READ, newStatus);
                    getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(cursorId), value, null, null);
                    TextView characterView = (TextView)(view.findViewById(R.id.LearnedCharacter));
                    characterView.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });


        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LEARNED_CHARACTER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        Log.d(LOG_TAG, "onCreateLoader for loader_id " + i);

        CursorLoader cursor;
        String sortOrder = LearnChineseContract.CustomLearning.COLUMN_NAME + " ASC";
        Uri uri= LearnChineseContract.Character.buildCharacterUriByRead(LearnChineseContract.YES);
        cursor = new CursorLoader(getActivity(),
                uri,
                null,
                null,
                null,
                sortOrder);

        Log.v(LOG_TAG,"onCreateLoader id = " + i );
        return cursor;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        TextView hint = (TextView)rootView.findViewById(R.id.hint);
        if ((cursor==null)||(cursor.getCount() == 0))
        {
            Log.v(LOG_TAG, " return cursorLoader.getId()" + cursorLoader.getId());
            hint.setText("还没有已学会的汉字，快去学习吧！");
            return;
        }
        hint.setText("已经学会" +cursor.getCount() + "个汉字，单击汉字将其重新放入待学习字库");
        Log.v(LOG_TAG, "onLoadFinished");
        mAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
