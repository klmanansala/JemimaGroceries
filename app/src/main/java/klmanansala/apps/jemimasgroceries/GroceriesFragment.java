package klmanansala.apps.jemimasgroceries;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;
import klmanansala.apps.jemimasgroceries.data.GroceriesProvider;


public class GroceriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String[] COLUMNS = {
            GroceriesContract.GroceryEntry._ID
            ,GroceriesContract.GroceryEntry.COLUMN_NAME
            , GroceriesContract.GroceryEntry.COLUMN_QUANTITY
            , GroceriesContract.GroceryEntry.COLUMN_CHECKED
    };

    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_QTY = 2;
    static final int COL_CHECKED = 3;

    private static final int GROCERY_LOADER_ID = 1;

    private GroceriesAdapter mGroceriesAdapter;

    public GroceriesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groceries, container, false);

        mGroceriesAdapter = new GroceriesAdapter(getActivity(), null, 0);

        ListView groceryList = (ListView) view.findViewById(R.id.listview_groceries);
        groceryList.setAdapter(mGroceriesAdapter);

        View emptyView = view.findViewById(R.id.empty_grocery_list);
        groceryList.setEmptyView(emptyView);

        groceryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if(cursor != null){
                    long itemId = cursor.getLong(COL_ID);

                    Intent intent = new Intent(getActivity(), EditGroceryItemActivity.class);
                    intent.putExtra(GroceriesContract.GroceryEntry._ID, itemId);
                    startActivity(intent);
                }
            }
        });

        ImageButton addButton = (ImageButton) view.findViewById(R.id.btn_add_groceries);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addGroceryIntent = new Intent(getActivity(), AddGroceryActivity.class);
                startActivity(addGroceryIntent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(GROCERY_LOADER_ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        String sortOrder = GroceriesContract.GroceryEntry.COLUMN_CHECKED + " ASC";

        return new CursorLoader(getActivity(), GroceriesContract.GroceryEntry.CONTENT_URI
                , COLUMNS, GroceriesProvider.sActiveGrocerySelection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGroceriesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGroceriesAdapter.swapCursor(null);
    }
}
