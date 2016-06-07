package com.example.xuzhi.learnchinese;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentCustomPronunciation extends Fragment {
    View mRootView;
    static ArrayAdapter mAdapter;
    static TextView hint,tone_hint;
    private final String LOG_TAG = this.getClass().getSimpleName();
    ListView customPronunciationListView;
    MediaPlayer mp  = new MediaPlayer();
    static String currentItemString = "";
    static ArrayList mPronunciationList = new ArrayList();
    static String root_sd = "";
    public FragmentCustomPronunciation() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_custom_pronunciation, container, false);
        customPronunciationListView = (ListView)mRootView.findViewById(R.id.custom_pronunciation);
        hint = (TextView)mRootView.findViewById(R.id.hint);
        tone_hint = (TextView)mRootView.findViewById(R.id.tone_hint);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                Log.v(LOG_TAG, "play the sound");
                mp.start();
            }
        });
        root_sd = getActivity().getFilesDir().getAbsolutePath();
        Log.v(LOG_TAG, "root_sd = " + root_sd);

        UpdateAdapterData();

        mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mPronunciationList);
        customPronunciationListView.setAdapter(mAdapter);

        customPronunciationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mp.reset();
                TextView pinYinView = (TextView)(view.findViewById(android.R.id.text1));
                String pinYin =  pinYinView.getText().toString();
                String sound = root_sd + "/" + pinYin + FragmentCustomDefinePronunciation.CUSTOM_PRONUNCIATION_SUFFIX;
                Log.v(LOG_TAG,"sound = " + sound);
                try {
                    mp.setDataSource(sound);
                    mp.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        //delete or edit custom item
        customPronunciationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                TextView pinYinView = (TextView)(view.findViewById(android.R.id.text1));
                String pinYin =  pinYinView.getText().toString();
                EditOrDeleteTheItem(pinYin);
                return true;
            }

        });
        return mRootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }
    @Override
    public void onResume() {
        Log.v(LOG_TAG,"onResume");
        UpdateAdapterData();
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理动作按钮的点击事件
        switch (item.getItemId()) {
            case R.id.action_custom_define_pronunciation_settings:
            {
                Intent intent = new Intent(getActivity(), ActivityCustomDefinePronunciation.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    public void onDestroy() {
        mp.release();
        super.onDestroy();

    }

    private void EditOrDeleteTheItem(String currentPinYin)
    {
        currentItemString = currentPinYin;
        new EditOrDeleteDialogFragment().show(getFragmentManager(), "EditOrDeleteDialog");
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
                    //get old pin yin info
                    Intent intent = new Intent(getActivity(), ActivityCustomDefinePronunciation.class).putExtra(Intent.EXTRA_TEXT, currentItemString);
                    startActivity(intent);
                }
            });
            builder.setPositiveButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //delete the corresponding file and update the list view
                    File pinYin = new File(root_sd,currentItemString + FragmentCustomDefinePronunciation.CUSTOM_PRONUNCIATION_SUFFIX);
                    Log.v("DELETE", "FILE = " + pinYin.toString());
                    if (pinYin.exists())
                    {
                        pinYin.delete();
                    }
                    //update the adapter data
                    UpdateAdapterData();
                    mAdapter.notifyDataSetChanged();
                }
            });
            return builder.create();
        }
    }
    static void UpdateAdapterData()
    {
        mPronunciationList.clear();
        File file = new File(root_sd);
        File list[] = file.listFiles();
        for (int i = 0; i < list.length; i++) {
            String fileName = list[i].getName();
            if (!fileName.contains(FragmentCustomDefinePronunciation.CUSTOM_PRONUNCIATION_SUFFIX)) {
                continue;}

            mPronunciationList.add(fileName.split("\\.")[0]);
        }
        if (mPronunciationList.size() == 0){
            hint.setVisibility(View.VISIBLE);
            tone_hint.setVisibility(View.GONE);
        }
        else{
            hint.setVisibility(View.GONE);
            tone_hint.setVisibility(View.VISIBLE);
        }
       // Log.v("UpdateAdapterData", "pronunciationList =" + mPronunciationList.toString());
    }

}
