package com.projects.team.experimental.virtualdashboard.Editor;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.projects.team.experimental.virtualdashboard.R;

import java.util.ArrayList;

public class EditorListFragment extends ListFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    String[] EditorMenu = new String[] { "Default 1","Default 2","Custom 1","Custom 2","Custom 3"};
    ArrayList<String> editorMenuArrayList;
    ArrayAdapter<String> adapter;
    EditorMemoryState internalMemory;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        editorMenuArrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_activated_1, editorMenuArrayList);

        for (String s : EditorMenu) {
            editorMenuArrayList.add(s);
        }

        setListAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        setClickOnPos(0);

        internalMemory = new EditorMemoryState(getActivity());
        for (int i = 0; i<3; i++) {
            editorMenuArrayList.remove(i+2);
            editorMenuArrayList.add(i+2, internalMemory.getCustomName(i));
        }

        internalMemory.settingsPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        internalMemory.settingsPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void setClickOnPos(int position){
        getListView().requestFocusFromTouch();
        getListView().setSelection(position);
        getListView().performItemClick(getListView().getAdapter().getView(position, null, null), position, position);
        getListView().setSelector(android.R.color.holo_blue_dark);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        EditorViewFragment ev = (EditorViewFragment) getFragmentManager().findFragmentById(R.id.detailFragment);
        ev.showEditViewForPos(position);

        getListView().setSelector(android.R.color.holo_blue_light);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (EditorMemoryState.CUSTOM_KEY_NAME_1.equals(key)) {
            editorMenuArrayList.remove(0+2);
            editorMenuArrayList.add(0+2, internalMemory.getCustomName(0));
        } else if (EditorMemoryState.CUSTOM_KEY_NAME_2.equals(key)) {
            editorMenuArrayList.remove(1+2);
            editorMenuArrayList.add(1+2, internalMemory.getCustomName(1));
        } else if (EditorMemoryState.CUSTOM_KEY_NAME_3.equals(key)) {
            editorMenuArrayList.remove(2+2);
            editorMenuArrayList.add(2+2, internalMemory.getCustomName(2));
        }
        adapter.notifyDataSetChanged();
    }

}
