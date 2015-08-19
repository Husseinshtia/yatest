package com.nyaschenko.categories;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nyaschenko.categories.provider.CategoryContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class CategoryFragment extends ListFragment implements
        AbsListView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_PARENT_ID = "arg_parent_id";
    public static final String ARG_TITLE = "arg_title";
    private static final int LOADER_CATEGORIES = 1;

    private long parentId;
    private String title;

    private OnFragmentInteractionListener mListener;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        return view;
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

        Log.d("CATEGORY", "onActivityCreated");
        if (savedInstanceState != null) {
            parentId = savedInstanceState.getLong(ARG_PARENT_ID, -1);
            title = savedInstanceState.getString(ARG_TITLE, getString(R.string.app_name));
        }
        getActivity().setTitle(title);

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

        getLoaderManager().initLoader(LOADER_CATEGORIES, Bundle.EMPTY, this);

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

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long id, String title);
    }

}
