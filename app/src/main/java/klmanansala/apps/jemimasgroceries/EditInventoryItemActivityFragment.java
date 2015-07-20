package klmanansala.apps.jemimasgroceries;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class EditInventoryItemActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = EditInventoryItemActivityFragment.class.getSimpleName();

    private static final String[] COLUMNS = {
            GroceriesContract.InventoryEntry._ID
            ,GroceriesContract.InventoryEntry.COLUMN_NAME
            , GroceriesContract.InventoryEntry.COLUMN_QUANTITY
            , GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE
    };

    static final int COL_ID = 0;
    static final int COL_NAME = 1;
    static final int COL_QTY = 2;
    static final int COL_EXP_DATE = 3;

    private static int LOADER_ID = 1;

    private static long mEnteredDate = 0;

    private static EditText mDateText;
    private AutoCompleteTextView mNameTxt;
    private EditText mQtyTxt;

    private long itemId;

    public EditInventoryItemActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_inventory_item, container, false);

        mDateText = (EditText) view.findViewById(R.id.text_expiration_date);
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        Button saveBtn = (Button) view.findViewById(R.id.btn_save_inventory);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInventoryItem();
            }
        });

        Button deleteBtn = (Button) view.findViewById(R.id.btn_delete_inventory);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteInventoryItem();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = GroceriesContract.InventoryEntry._ID + " = ? ";
        String[] selectionArgs = new String[] { Long.toString(getIdPassedToFragment()) };

        return new CursorLoader(getActivity()
                , GroceriesContract.InventoryEntry.CONTENT_URI
                , COLUMNS
                , selection
                , selectionArgs
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        View rootView = getView();

        mNameTxt = (AutoCompleteTextView) rootView.findViewById(R.id.text_inventory_item_name);
        mQtyTxt = (EditText) rootView.findViewById(R.id.text_inventory_item_qty);

        mNameTxt.setText(data.getString(COL_NAME));
        mQtyTxt.setText(Integer.toString(data.getInt(COL_QTY)));

        long date = data.getLong(COL_EXP_DATE);
        if(date > 0) {
            mDateText.setText(Utility.getFormattedDate(date));
        }
        mEnteredDate = date;

        itemId = data.getLong(COL_ID);

        //for autocomplete suggestions
        mNameTxt.setAdapter(Utility.createItemNamesAdapter(getActivity()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Long date = c.getTimeInMillis();
            date = GroceriesContract.normalizeDate(date);

            mDateText.setText(Utility.getFormattedDate(date));
            mEnteredDate = date;
        }
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    long getIdPassedToFragment(){
        Intent intent = getActivity().getIntent();
        if(getArguments() != null) {
            String idString = getArguments().getString("id");
            itemId = Long.parseLong(idString);
        } else if(intent != null){
            itemId = intent.getLongExtra(GroceriesContract.InventoryEntry._ID, 0);
        }

        return itemId;
    }

    private void updateInventoryItem(){
        ContentValues updatedValues = new ContentValues();

        String name = mNameTxt.getText().toString();
        String qtyString = mQtyTxt.getText().toString();

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

        updatedValues.put(GroceriesContract.InventoryEntry._ID, itemId);
        updatedValues.put(GroceriesContract.InventoryEntry.COLUMN_NAME, name);
        updatedValues.put(GroceriesContract.InventoryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));
        updatedValues.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE, mEnteredDate);

        String selection = GroceriesContract.InventoryEntry._ID + " = ? ";
        String args[] = new String[] { Long.toString(itemId) };
        int count = getActivity().getContentResolver().update(GroceriesContract.InventoryEntry.CONTENT_URI
                , updatedValues
                , selection
                , args);

        Utility.addItemNameEntry(getActivity(), name);

        if(count != 1){
            Log.d(LOG_TAG, "Updating inventory item with id = " + itemId + " returned a count not equal to one.");
        }

        Toast.makeText(getActivity()
                , R.string.inventory_item_updated
                , Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    private void deleteInventoryItem(){
        String selection = GroceriesContract.InventoryEntry._ID + " = ? ";
        String args[] = new String[] { Long.toString(itemId) };

        int count = getActivity().getContentResolver().delete(GroceriesContract.InventoryEntry.CONTENT_URI
                , selection
                , args);

        if(count != 1){
            Log.d(LOG_TAG, "Deleting inventory item with id = " + itemId + " returned a count not equal to one.");
        }

        Toast.makeText(getActivity()
            , R.string.inventory_item_deleted
            , Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }
}
