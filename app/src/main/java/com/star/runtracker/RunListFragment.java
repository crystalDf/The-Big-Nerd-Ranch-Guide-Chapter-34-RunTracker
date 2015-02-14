package com.star.runtracker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RunListFragment extends ListFragment {

    private static final int REQUEST_NEW_RUN = 0;

    private RunDatabaseHelper.RunCursor mRunCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mRunCursor = RunManager.getInstance(getActivity()).queryRuns();

        RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mRunCursor);
        setListAdapter(adapter);
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
        Intent i = new Intent(getActivity(), RunActivity.class);
        i.putExtra(RunFragment.EXTRA_RUN_ID, id);
        startActivity(i);
    }
}
