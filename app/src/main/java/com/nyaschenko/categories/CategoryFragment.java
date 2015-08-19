package com.nyaschenko.categories;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nyaschenko.categories.provider.CategoryContract;


public class CategoryFragment extends ListFragment implements
        AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_PARENT_ID = "arg_parent_id";
    public static final String ARG_TITLE = "arg_title";
    private static final int LOADER_CATEGORIES = 1;
    private static final String PREF_FIRST_LAUNCH = "first_launch";

    private long parentId;
    private String title;

    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver receiver;

    private AbsListView mListView;
    private CursorAdapter mAdapter;

    public static CategoryFragment newInstance(long parentId, String title) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARENT_ID, parentId);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        if (getArguments() != null) {
            parentId = getArguments().getLong(ARG_PARENT_ID, -1);
            title = getArguments().getString(ARG_TITLE, getString(R.string.app_name));
        }

        checkFirstLaunch();
    }

    private void checkFirstLaunch() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (preferences.getBoolean(PREF_FIRST_LAUNCH, true)) {
            LoadService.startActionLoadCategories(getActivity());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH, false);
            editor.apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = getListView();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            parentId = savedInstanceState.getLong(ARG_PARENT_ID, -1);
            title = savedInstanceState.getString(ARG_TITLE, getString(R.string.app_name));
        }
        getActivity().setTitle(title);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)  {
                Log.d("CATEGORY", "Got broadcast update");
                if (isAdded()) {
                    getLoaderManager().restartLoader(LOADER_CATEGORIES, Bundle.EMPTY, CategoryFragment.this).forceLoad();
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, new IntentFilter("update"));

        mAdapter = new CursorAdapter(getActivity(), null, false) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(
                        android.R.layout.simple_list_item_activated_2, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final String title = cursor.getString(cursor.getColumnIndex(CategoryContract.CategoryColumns.CATEGORY_TITLE));
                final TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setText(title);
                if (cursor.getInt(cursor.getColumnIndex(CategoryContract.CategoryColumns.CATEGORY_HAS_SUBCATEGORIES)) == 1) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_more, 0);
                }

                final String id = cursor.getString(cursor.getColumnIndex(CategoryContract.CategoryColumns.CATEGORY_ID));
                final TextView textView2 = (TextView) view.findViewById(android.R.id.text2);
                textView2.setText(id);
            }
        };

        setListAdapter(mAdapter);
        registerForContextMenu(getListView());


        getLoaderManager().initLoader(LOADER_CATEGORIES, Bundle.EMPTY, this).forceLoad();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ARG_TITLE, title);
        outState.putLong(ARG_PARENT_ID, parentId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            final String title = cursor.getString(cursor.getColumnIndex(CategoryContract.CategoryColumns.CATEGORY_TITLE));
            if (cursor.getInt(cursor.getColumnIndex(CategoryContract.CategoryColumns.CATEGORY_HAS_SUBCATEGORIES)) == 1) {
                mListener.onFragmentInteraction(id, title);
            }
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("CATEGORY", "onCreateLoader");
        switch (id) {
            case LOADER_CATEGORIES:
                Log.d("CATEGORY", "parentdId: " + parentId);
                final String selection = CategoryContract.CategoryColumns.CATEGORY_PARENT +
                        (parentId == -1 ? " IS NULL" : " = ?");
                final String[] selectionArgs = parentId == -1 ? null : new String[]{ Long.toString(parentId) };
                return new CursorLoader(
                        getActivity(),
                        CategoryContract.Category.CONTENT_URI,
                        CategoryContract.Category.ALL_COLUMNS,
                        selection,
                        selectionArgs,
                        null);
            default:
                throw new IllegalArgumentException("Illegal loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("CATEGORY", "onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(long id, String title);
    }

}
