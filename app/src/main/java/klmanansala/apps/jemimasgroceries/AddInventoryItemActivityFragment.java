package klmanansala.apps.jemimasgroceries;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
public class AddInventoryItemActivityFragment extends Fragment {

    private static long mEnteredDate = 0;

    private static EditText mDateText;
    private AutoCompleteTextView mNameTxt;
    private EditText mQuantityTxt;

    public AddInventoryItemActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_inventory_item, container, false);

        mDateText = (EditText) view.findViewById(R.id.text_expiration_date);
        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mNameTxt = (AutoCompleteTextView) view.findViewById(R.id.text_inventory_item_name);
        mQuantityTxt = (EditText) view.findViewById(R.id.text_inventory_item_qty);

        //for autocomplete suggestions
        mNameTxt.setAdapter(Utility.createItemNamesAdapter(getActivity()));

        Button addBtn = (Button) view.findViewById(R.id.btn_add_inventory);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInventoryItemToDb();
            }
        });

        return view;
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

    private void addInventoryItemToDb(){
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
                    , R.string.inventory_item_qunatity_is_empty
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(GroceriesContract.InventoryEntry.COLUMN_NAME, name);
        values.put(GroceriesContract.InventoryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));
        values.put(GroceriesContract.InventoryEntry.COLUMN_EXPIRATION_DATE, mEnteredDate);
        values.put(GroceriesContract.InventoryEntry.COLUMN_STATUS, GroceriesContract.InventoryEntry.STATUS_ACTIVE);

        try {
            Uri uri = getActivity().getContentResolver().insert(GroceriesContract.InventoryEntry.CONTENT_URI, values);

            long itemId = ContentUris.parseId(uri);

            if(itemId != -1) {
                Toast.makeText(getActivity()
                        , getActivity().getString(R.string.inventory_item_saved)
                        , Toast.LENGTH_SHORT).show();

                mNameTxt.setText("");
                mNameTxt.requestFocus();
                mQuantityTxt.setText("");
                mEnteredDate = 0;
                mDateText.setText("");


                Utility.addItemNameEntry(getActivity(), name);
            }
        } catch (SQLException ex){
            Toast.makeText(getActivity()
                    , R.string.item_already_on_the_list
                    , Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
