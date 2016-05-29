package com.example.xuzhi.learnchinese;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ParentsOptionsActivityFragment extends Fragment {
    private ArrayAdapter<String> mUsersListAdapter;
    View mRootView;
    public ParentsOptionsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_parents_options, container, false);
        final ListView listView = (ListView)mRootView.findViewById(R.id.parents_options_list);
        final String[] usersMenu = {"已学会的汉字","自定义学习内容","自定义发音","汉字能力测试"};
        mUsersListAdapter =
                new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, usersMenu);
        listView.setAdapter(mUsersListAdapter);
        /*calculate the height of all the items and adjust the listview*/
        Utility.setListViewHeightBasedOnChildren(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                String listItem = (String) listView.getItemAtPosition(position);
                switch (listItem) {
                    case "已学会的汉字":
                        intent = new Intent(getActivity(), LearnedCharactersActivity.class);
                        startActivity(intent);
                        break;
                    case "自定义学习内容":
                        intent = new Intent(getActivity(), CustomLearningActivity.class);
                        startActivity(intent);
                        break;
                    case "自定义发音":
                        intent = new Intent(getActivity(), CustomPronunciationActivity.class);
                        //intent = new Intent(getActivity(), CustomDefinePronunciationActivity.class);
                        startActivity(intent);
                        break;
                    case "汉字能力测试":
                        intent = new Intent(getActivity(), AbilityTestActivity.class);
                        startActivity(intent);
                        break;

                    default:
                        break;
                }
            }
        });
        return mRootView;

    }
}
