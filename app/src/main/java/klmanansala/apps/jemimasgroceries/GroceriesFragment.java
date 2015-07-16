package klmanansala.apps.jemimasgroceries;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;
import klmanansala.apps.jemimasgroceries.data.GroceriesProvider;


public class GroceriesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = GroceriesFragment.class.getSimpleName();

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

    private static final String[] INVENTORY_COLUMNS = {
            GroceriesContract.InventoryEntry._ID
            ,GroceriesContract.InventoryEntry.COLUMN_NAME
            , GroceriesContract.InventoryEntry.COLUMN_QUANTITY
            , GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE
    };

    static final int INVENTORY_COL_ID = 0;
    static final int INVENTORY_COL_NAME = 1;
    static final int INVENTORY_COL_QTY = 2;
    static final int INVENTORY_COL_EXP_DATE = 3;

    private static final int GROCERY_LOADER_ID = 1;

    private static final String DIALOG_TAG = "ConfirmStoringDialogTag";

    private GroceriesAdapter mGroceriesAdapter;
    private ListView mGroceryList;

    public GroceriesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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

                if (cursor != null) {
                    long itemId = cursor.getLong(COL_ID);

                    Intent intent = new Intent(getActivity(), EditGroceryItemActivity.class);
                    intent.putExtra(GroceriesContract.GroceryEntry._ID, itemId);
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_groceries_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_grocery){
            Intent addGroceryIntent = new Intent(getActivity(), AddGroceryActivity.class);
            startActivity(addGroceryIntent);
            return true;
        } else if(id == R.id.action_put_to_inventory){
            putGroceryItemsToInventory();
        }
        return super.onOptionsItemSelected(item);
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

    private void putGroceryItemsToInventory(){
        if(checkIfThereAreCheckedGroceryItems()){
            ConfirmStoringDialogFragment dialog = new ConfirmStoringDialogFragment();
            dialog.show(getFragmentManager(), DIALOG_TAG);
        } else {
            Toast.makeText(getActivity()
                , R.string.nothing_to_move_to_inventory
                , Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkIfThereAreCheckedGroceryItems(){
        Cursor cursor = getActivity().getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI
                , COLUMNS
                , null
                , null
                , null );

        if(cursor.moveToFirst()){
            do {
                int check = cursor.getInt(COL_CHECKED);
                if (check == GroceriesContract.GroceryEntry.CHECKED) {
                    return true;
                }
            } while(cursor.moveToNext());
        }

        return false;
    }

    public static class ConfirmStoringDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = getActivity().getString(R.string.ready_to_store);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    storeItemsIntoInventory(getActivity());
                }
            });
            builder.setNegativeButton(R.string.not_yet, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                }
            });

            return builder.create();
        }
    }

    private static void storeItemsIntoInventory(Context context){
        int countOfGroceryItems = 0;

        Cursor cursor = context.getContentResolver().query(
                GroceriesContract.GroceryEntry.CONTENT_URI
                , COLUMNS
                , GroceriesContract.GroceryEntry.COLUMN_CHECKED + " = ?"
                , new String[] {Integer.toString(GroceriesContract.GroceryEntry.CHECKED)}
                , null );

        List<ContentValues> inventoryItemsList = new ArrayList<ContentValues>();

        if(cursor.moveToFirst()){
            do {
                countOfGroceryItems++;

                String itemName = cursor.getString(COL_NAME);
                ContentValues inventoryEntry = new ContentValues();
                inventoryEntry.put(GroceriesContract.InventoryEntry.COLUMN_NAME
                        , itemName);
                inventoryEntry.put(GroceriesContract.InventoryEntry.COLUMN_QUANTITY
                        , cursor.getInt(COL_QTY));
                inventoryEntry.put(GroceriesContract.InventoryEntry.COLUMN_STATUS
                        , GroceriesContract.InventoryEntry.STATUS_ACTIVE);
                inventoryEntry.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE
                        , 0);

                Uri queryUri = GroceriesContract.InventoryEntry.buildInventoryWithNameUri(itemName);

                Cursor inventoryCursor = context.getContentResolver().query(queryUri
                        , INVENTORY_COLUMNS
                        , null
                        , null
                        , null);

                if(inventoryCursor.moveToFirst()){
                    // item is already saved in inventory
                    long existingInventoryItemId = inventoryCursor.getLong(INVENTORY_COL_ID);
                    int updatedQty = cursor.getInt(COL_QTY) + inventoryCursor.getInt(INVENTORY_COL_QTY);

                    ContentValues updatedInventoryItem = new ContentValues();
                    updatedInventoryItem.put(GroceriesContract.InventoryEntry._ID
                            , existingInventoryItemId);
                    updatedInventoryItem.put(GroceriesContract.InventoryEntry.COLUMN_NAME
                            , itemName);
                    updatedInventoryItem.put(GroceriesContract.InventoryEntry.COLUMN_QUANTITY
                            , updatedQty);
                    updatedInventoryItem.put(GroceriesContract.InventoryEntry.COLUMN_STATUS
                            , GroceriesContract.InventoryEntry.STATUS_ACTIVE);
                    updatedInventoryItem.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE
                            , inventoryCursor.getLong(INVENTORY_COL_EXP_DATE));

                    int updatedCount = context.getContentResolver().update(
                            GroceriesContract.InventoryEntry.CONTENT_URI
                            , updatedInventoryItem
                            , GroceriesContract.InventoryEntry._ID + " = ?"
                            , new String[]{Long.toString(existingInventoryItemId)} );

                    // verify that existing item has been updated
                    if(updatedCount != 1){
                        Log.d(LOG_TAG, "Error with updating inventory item with id = "
                            + existingInventoryItemId + ". Updated count returned is: " + updatedCount);
                    }

                } else {
                    // new inventory item, add to list for bulk insert call
                    inventoryItemsList.add(inventoryEntry);
                }

            } while(cursor.moveToNext());
        }

        ContentValues valuesArray[] = new ContentValues[inventoryItemsList.size()];
        inventoryItemsList.toArray(valuesArray);

        int count = context.getContentResolver().bulkInsert(GroceriesContract.InventoryEntry.CONTENT_URI
            , valuesArray);

        // verify that all items have been saved
        if(count != inventoryItemsList.size()){
            Log.d(LOG_TAG, "Count returned by bulk insert is not equal to number of grocery items"
                    + " to be saved");
        }

        // delete grocery items added to inventory
        int deletedCount = context.getContentResolver().delete(GroceriesContract.GroceryEntry.CONTENT_URI
                , GroceriesContract.GroceryEntry.COLUMN_CHECKED + " = ?"
                , new String[] {Integer.toString(GroceriesContract.GroceryEntry.CHECKED)});

        if(deletedCount != countOfGroceryItems){
            Log.d(LOG_TAG, "Error with deleting grocery entries.  Expected number of deleted entries" +
                    " = " + countOfGroceryItems + ". Actual count of deleted entries = "
                    + deletedCount);
        }

        Toast.makeText(context, R.string.grocery_items_saved_to_inventory
            , Toast.LENGTH_LONG).show();
    }
}
