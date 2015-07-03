package klmanansala.apps.jemimasgroceries;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class AddGroceryActivityFragment extends Fragment {

    private EditText mNameTxt;
    private EditText mQuantityTxt;

    public AddGroceryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_grocery, container, false);

        mNameTxt = (EditText) view.findViewById(R.id.text_grocery_name);
        mQuantityTxt = (EditText) view.findViewById(R.id.text_grocery_qty);

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
            //name is blank
        }

        if(qtyString == null || qtyString.length() == 0){
            //quantity is blank
        }

        ContentValues values = new ContentValues();
        values.put(GroceriesContract.GroceryEntry.COLUMN_NAME, name);
        values.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));
        values.put(GroceriesContract.GroceryEntry.COLUMN_STATUS, 1);

        Uri uri = getActivity().getContentResolver().insert(GroceriesContract.GroceryEntry.CONTENT_URI, values);

        long itemId = ContentUris.parseId(uri);

        if(itemId != -1) {
            Toast.makeText(getActivity()
                    , getActivity().getString(R.string.grocery_item_saved)
                    , Toast.LENGTH_SHORT).show();
        }
    }
}
