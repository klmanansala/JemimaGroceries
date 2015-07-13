package klmanansala.apps.jemimasgroceries;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;
import klmanansala.apps.jemimasgroceries.data.GroceriesProvider;

public class InventoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String[] COLUMNS = {
            GroceriesContract.InventoryEntry._ID
            , GroceriesContract.InventoryEntry.COLUMN_NAME
            , GroceriesContract.InventoryEntry.COLUMN_QUANTITY
            , GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE
    };

    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_QTY = 2;
    static final int COL_DATE = 3;

    private InventoryAdapter mInventoryAdapter;

    private static final int INVENTORY_LOADER_ID = 1;

    public InventoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        mInventoryAdapter = new InventoryAdapter(getActivity(), null, 0);

        ListView inventoryList = (ListView) view.findViewById(R.id.listview_inventory);
        inventoryList.setAdapter(mInventoryAdapter);

        View emptyView = view.findViewById(R.id.empty_inventory_list);
        inventoryList.setEmptyView(emptyView);

        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if (cursor != null) {
                    long itemId = cursor.getLong(COL_ID);

                    Intent intent = new Intent(getActivity(), EditInventoryItemActivity.class);
                    intent.putExtra(GroceriesContract.InventoryEntry._ID, itemId);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_inventory){
            Intent addInventoryIntent = new Intent(getActivity(), AddInventoryItemActivity.class);
            startActivity(addInventoryIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(INVENTORY_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GroceriesContract.InventoryEntry.CONTENT_URI
                , COLUMNS, GroceriesProvider.sActiveInventorySelection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mInventoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryAdapter.swapCursor(null);
    }
}
