package com.example.xuzhi.learnchinese;

import android.content.ClipData;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
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
    static LearnedCharactersActivityFragment mThis;
    public LearnedCharactersActivityFragment() {
        mThis = this;
    }
    ImageView delete;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_learned_characters, container, false);
        learnedCharactersGridView = (GridView)rootView.findViewById(R.id.learned_characters);
        mAdapter = new LearnedCharactersAdapter(getActivity(),null,0);
        learnedCharactersGridView.setAdapter(mAdapter);
        delete = (ImageView)rootView.findViewById(R.id.delete);
        /*learnedCharactersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                    int cursorId = cursor.getInt(idIndex);
                    int readIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_READ);
                    String readStatus = cursor.getString(readIndex);
                    Log.v(LOG_TAG, "readStatus = " + readStatus);
                    ContentValues value = new ContentValues();
                    String newStatus = LearnChineseContract.NO;
                    value.put(LearnChineseContract.Character.COLUMN_READ, newStatus);
                    getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(cursorId), value, null, null);
                    TextView characterView = (TextView) (view.findViewById(R.id.LearnedCharacter));
                    characterView.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });*/
        learnedCharactersGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    int idIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_ID);
                    int cursorId = cursor.getInt(idIndex);
                    String idString = Integer.toString(cursorId);
                    String positionString = Integer.toString(position);
                    int nameIndex = cursor.getColumnIndex(LearnChineseContract.Character.COLUMN_NAME);
                    String name = cursor.getString(nameIndex);
                    ClipData.Item item = new ClipData.Item((CharSequence) idString);


                    String[] mimeTypes = {positionString};
                    ClipData dragData = new ClipData(idString,
                            mimeTypes, item);
                    ClipData.Item item1 = new ClipData.Item((CharSequence)name);
                    dragData.addItem(item1);
                    // Instantiates the drag shadow builder.
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                    // Starts the drag
                    view.startDrag(dragData,  // the data to be dragged
                            myShadow,  // the drag shadow builder
                            null,      // no need to use local data
                            0          // flags (not currently used, set to 0)
                    );
                    delete.setVisibility(View.VISIBLE);


                }
                return true;
            }
        });


        delete.setBackgroundColor(getResources().getColor(R.color.highlightpink));
        delete.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                String positionString;
                int position;
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                       /* positionString =  event.getClipDescription().getMimeType(0);
                        Log.v(LOG_TAG,"positionString = " +  positionString);
                        position = Integer.parseInt(positionString);
                        learnedCharactersGridView.getChildAt(position).setVisibility(View.INVISIBLE);
                        Log.v(LOG_TAG, "childNumber = " + learnedCharactersGridView.getChildCount());*/
                        Log.v(LOG_TAG, "Action is DragEvent.ACTION_DRAG_STARTED");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(getResources().getColor(R.color.red));
                        Log.v(LOG_TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundColor(getResources().getColor(R.color.highlightpink));
                        Log.v(LOG_TAG, "Action is DragEvent.ACTION_DRAG_EXITED");
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        Log.v(LOG_TAG, "Action is DragEvent.ACTION_DRAG_LOCATION");
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        delete.setVisibility(View.GONE);
                        // if (!event.getResult())/*drop failed*/
                       /* {
                            ClipDescription temp = event.getClipDescription();
                            Log.v(LOG_TAG,"ClipDescription = " +  temp);/*temp == null ,here seems to be a android bug*/
                            /*positionString =  event.getClipDescription().getMimeType(0);
                            Log.v(LOG_TAG,"positionString = " +  positionString);
                            position = Integer.parseInt(positionString);
                            learnedCharactersGridView.getChildAt(position).setVisibility(View.VISIBLE);;
                        }*/
                        Log.v(LOG_TAG, "Action is DragEvent.ACTION_DRAG_ENDED");
                        break;
                    case DragEvent.ACTION_DROP:
                        Log.v(LOG_TAG, "ACTION_DROP event");
                        String idString = event.getClipData().getItemAt(0).getText().toString();
                        int id = Integer.parseInt(idString);
                        Log.v(LOG_TAG, "id = " + idString);
                        ContentValues value = new ContentValues();
                        String newStatus = LearnChineseContract.NO;
                        value.put(LearnChineseContract.Character.COLUMN_READ, newStatus);
                        getActivity().getContentResolver().update(LearnChineseContract.Character.buildCharacterUriById(id), value, null, null);

                        String name = event.getClipData().getItemAt(1).getText().toString();
                        CalculatePercentageTask task = new CalculatePercentageTask(getActivity());
                        Log.v(LOG_TAG,"name = " + name);
                        task.execute(name,LearnChineseContract.NO);
                        v.setBackgroundColor(getResources().getColor(R.color.highlightpink));
                        getLoaderManager().restartLoader(LEARNED_CHARACTER_LOADER, null, mThis);

                        break;
                    default:
                        break;
                }
                return true;
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
            hint.setText("0");
            return;
        }
        hint.setText(Integer.toString(cursor.getCount()));
        Log.v(LOG_TAG, "onLoadFinished");
        mAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
