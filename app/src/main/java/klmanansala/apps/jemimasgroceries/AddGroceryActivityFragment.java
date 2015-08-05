package klmanansala.apps.jemimasgroceries;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;


public class AddGroceryActivityFragment extends Fragment {

    private static final String[] INVENTORY_COLUMNS = {
            GroceriesContract.InventoryEntry._ID
            ,GroceriesContract.InventoryEntry.COLUMN_NAME
            , GroceriesContract.InventoryEntry.COLUMN_QUANTITY
    };

    static final int INVENTORY_COL_ID = 0;
    static final int INVENTORY_COL_NAME = 1;
    static final int INVENTORY_COL_QTY = 2;

    private static AutoCompleteTextView mNameTxt;
    private static EditText mQuantityTxt;

    private static String mInventoryItemName;
    private static String mInventoryQty;
    private static ContentValues mValuesToAdd;

    private static boolean mProceedWithAdd = true;

    private static final String DIALOG_TAG = "ItemInInventoryDialogTag";

    public AddGroceryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_grocery, container, false);

        mNameTxt = (AutoCompleteTextView) view.findViewById(R.id.text_grocery_name);
        mQuantityTxt = (EditText) view.findViewById(R.id.text_grocery_qty);

        //for autocomplete suggestions
        mNameTxt.setAdapter(Utility.createItemNamesAdapter(getActivity()));

        Button addButton = (Button) view.findViewById(R.id.btn_add_grocery);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGroceryItemToDb();
            }
        });
        return view;
    }

    private void addGroceryItemToDb(){
        String name = mNameTxt.getText().toString();
        String qtyString = mQuantityTxt.getText().toString();

        if(name == null || name.length() == 0){
            Toast.makeText(getActivity()
                    , R.string.item_name_is_empty
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(qtyString == null || qtyString.length() == 0){
            Toast.makeText(getActivity()
                    , R.string.item_qunatity_is_empty
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        mValuesToAdd = new ContentValues();
        mValuesToAdd.put(GroceriesContract.GroceryEntry.COLUMN_NAME, name);
        mValuesToAdd.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));
        mValuesToAdd.put(GroceriesContract.GroceryEntry.COLUMN_STATUS, GroceriesContract.GroceryEntry.STATUS_ACTIVE);
        mValuesToAdd.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED, GroceriesContract.GroceryEntry.UNCHECKED);

        mProceedWithAdd = true;
        checkIfItemIsInInventory(name);

        if(mProceedWithAdd){
            addGroceryItemRecord(getActivity(), name);
        }

    }

    private static void addGroceryItemRecord(Context context, String name){
        try {
            Uri uri = context.getContentResolver().insert(GroceriesContract.GroceryEntry.CONTENT_URI, mValuesToAdd);

            long itemId = ContentUris.parseId(uri);

            if(itemId != -1) {
                Toast.makeText(context
                        , context.getString(R.string.grocery_item_saved)
                        , Toast.LENGTH_SHORT).show();

                mQuantityTxt.setText("");
                mNameTxt.setText("");
                mNameTxt.requestFocus();

                Utility.addItemNameEntry(context, name);
            }
        } catch (SQLException ex){
            Toast.makeText(context
                    , R.string.item_already_on_the_list
                    , Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void checkIfItemIsInInventory(String name){
        Uri queryUri = GroceriesContract.InventoryEntry.buildInventoryWithNameUri(name);
        Cursor cursor = getActivity().getContentResolver().query(queryUri
                            , INVENTORY_COLUMNS
                            , null
                            , null
                            , null);

        if(cursor.moveToFirst()){
            mProceedWithAdd = false;
            mInventoryItemName = cursor.getString(INVENTORY_COL_NAME);
            mInventoryQty = Integer.toString(cursor.getInt(INVENTORY_COL_QTY));

            ItemInInventoryDialogFragment dialog = new ItemInInventoryDialogFragment();
            dialog.show(getFragmentManager(), DIALOG_TAG);
        }
    }

    public static class ItemInInventoryDialogFragment extends DialogFragment{

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String message = getActivity().getString(R.string.format_available_inventory_list_item
                , mInventoryQty, mInventoryItemName);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.add_anyway, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addGroceryItemRecord(getActivity(), mInventoryItemName);
                }
            });
            builder.setNegativeButton(R.string.dont_add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                }
            });

            return builder.create();
        }
    }
}
