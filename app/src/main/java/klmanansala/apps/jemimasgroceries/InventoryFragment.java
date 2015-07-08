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
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        mInventoryAdapter = new InventoryAdapter(getActivity(), null, 0);

        ListView groceryList = (ListView) view.findViewById(R.id.listview_inventory);
        groceryList.setAdapter(mInventoryAdapter);

        groceryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if(cursor != null){
                    long itemId = cursor.getLong(COL_ID);

                    Intent intent = new Intent(getActivity(), EditInventoryItemActivity.class);
                    intent.putExtra(GroceriesContract.InventoryEntry._ID, itemId);
                    startActivity(intent);
                }
            }
        });

        ImageButton addButton = (ImageButton) view.findViewById(R.id.btn_add_inventory);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addInventoryIntent = new Intent(getActivity(), AddInventoryItemActivity.class);
                startActivity(addInventoryIntent);
            }
        });

        return view;
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
