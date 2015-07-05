package klmanansala.apps.jemimasgroceries;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;


public class AddGroceryActivityFragment extends Fragment {

    private AutoCompleteTextView mNameTxt;
    private EditText mQuantityTxt;

    public AddGroceryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_grocery, container, false);

        mNameTxt = (AutoCompleteTextView) view.findViewById(R.id.text_grocery_name);
        mQuantityTxt = (EditText) view.findViewById(R.id.text_grocery_qty);

        SimpleCursorAdapter itemNamesAdapter = new SimpleCursorAdapter(getActivity()
                , android.R.layout.simple_list_item_1
                , null
                , new String[] {GroceriesContract.ItemNameEntry.COLUMN_NAME}
                , new int[] { android.R.id.text1}
                , 0);
        mNameTxt.setAdapter(itemNamesAdapter);

        itemNamesAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                return Utility.getItemNamesCursor(getActivity(), constraint);
            }
        });

        itemNamesAdapter.setCursorToStringConverter(new android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(GroceriesContract.ItemNameEntry.COLUMN_NAME);
                return cur.getString(index);
            }
        });

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

        ContentValues values = new ContentValues();
        values.put(GroceriesContract.GroceryEntry.COLUMN_NAME, name);
        values.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));
        values.put(GroceriesContract.GroceryEntry.COLUMN_STATUS, GroceriesContract.GroceryEntry.STATUS_ACTIVE);
        values.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED, GroceriesContract.GroceryEntry.UNCHECKED);

        ContentValues itemNameValues = new ContentValues();
        itemNameValues.put(GroceriesContract.ItemNameEntry.COLUMN_NAME, name);

        try {
            Uri uri = getActivity().getContentResolver().insert(GroceriesContract.GroceryEntry.CONTENT_URI, values);

            long itemId = ContentUris.parseId(uri);

            if(itemId != -1) {
                Toast.makeText(getActivity()
                        , getActivity().getString(R.string.grocery_item_saved)
                        , Toast.LENGTH_SHORT).show();

                mNameTxt.setText("");
                mQuantityTxt.setText("");

                try {
                    getActivity().getContentResolver().insert(GroceriesContract.ItemNameEntry.CONTENT_URI, itemNameValues);
                } catch (SQLException ex){
                    //just ignore, thsi is a case of the item name saved already in the db
                }
            }
        } catch (SQLException ex){
            Toast.makeText(getActivity()
                , R.string.item_already_on_the_list
                , Toast.LENGTH_SHORT).show();
            return;
        }

    }

}
