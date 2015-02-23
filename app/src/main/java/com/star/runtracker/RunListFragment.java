package com.star.runtracker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RunListFragment extends ListFragment {

    private static final int REQUEST_NEW_RUN = 0;

    private RunDatabaseHelper.RunCursor mRunCursor;

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        setHasOptionsMenu(true);

        mRunCursor = RunManager.getInstance(getActivity()).queryRuns();

        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mRunCursor);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        ListView listView = (ListView) v.findViewById(android.R.id.list);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                getListView().setItemChecked(position, true);

                ((ActionBarActivity)getActivity()).startSupportActionMode(new ActionMode.Callback() {

                    @Override
                    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                        MenuInflater inflater = actionMode.getMenuInflater();
                        inflater.inflate(R.menu.menu_run_delete_context, menu);
                        mActionMode = actionMode;
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_item_delete_run:
                                RunCursorAdapter adapter = (RunCursorAdapter) getListAdapter();
                                RunManager runManager = RunManager.getInstance(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        RunDatabaseHelper.RunCursor runCursor = (RunDatabaseHelper.RunCursor) adapter.getItem(i);
                                        runManager.removeRun(runCursor.getRun().getId());
                                    }
                                }
                                actionMode.finish();
                                mRunCursor.requery();
                                adapter.notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode actionMode) {
                        getListView().clearChoices();

                        for (int i = 0; i < getListView().getChildCount(); i++) {
                            getListView().getChildAt(i).getBackground().setState(new int[] {0});
                        }

                        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
                        mActionMode = null;
                    }
                });

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    if (getListView().isItemChecked(position)) {
                        getListView().setItemChecked(position, false);
                    } else {
                        getListView().setItemChecked(position, true);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        mRunCursor.close();
        super.onDestroy();
    }

    private static class RunCursorAdapter extends CursorAdapter {

        private RunDatabaseHelper.RunCursor mRunCursor;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor runCursor) {
            super(context, runCursor, 0);
            mRunCursor = runCursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Run run = mRunCursor.getRun();

            TextView startDateTextView = (TextView) view;
            String cellText = context.getString(R.string.cell_text, run.getFormattedDate());
            startDateTextView.setText(cellText);

            startDateTextView.setBackgroundResource(R.drawable.background_activated);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_run_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_run:
                Intent i = new Intent(getActivity(), RunActivity.class);
                startActivityForResult(i, REQUEST_NEW_RUN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_NEW_RUN == requestCode) {
            mRunCursor.requery();
            ((RunCursorAdapter)getListAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mActionMode == null) {
            Intent i = new Intent(getActivity(), RunActivity.class);
            i.putExtra(RunFragment.EXTRA_RUN_ID, id);
            startActivity(i);
        }
    }
}
