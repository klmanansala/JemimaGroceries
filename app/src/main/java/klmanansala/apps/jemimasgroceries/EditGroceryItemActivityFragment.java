package klmanansala.apps.jemimasgroceries;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class EditGroceryItemActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = EditGroceryItemActivityFragment.class.getSimpleName();

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

    private static int LOADER_ID = 1;

    private long itemId = 0;
    private AutoCompleteTextView nameTxt;
    private EditText qtyTxt;
    private CheckBox checkbox;

    public EditGroceryItemActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_grocery_item, container, false);



        Button saveBtn = (Button) view.findViewById(R.id.btn_save_grocery);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateGroceryItem();
            }
        });

        Button deleteBtn = (Button) view.findViewById(R.id.btn_delete_grocery);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroceryItem();
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
        String selection = GroceriesContract.GroceryEntry._ID + " = ? ";
        String[] selectionArgs = new String[] { Long.toString(getIdPassedToFragment()) };

        return new CursorLoader(getActivity()
                , GroceriesContract.GroceryEntry.CONTENT_URI
                , COLUMNS
                , selection
                , selectionArgs
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        View rootView = getView();

        nameTxt = (AutoCompleteTextView) rootView.findViewById(R.id.text_grocery_name);
        qtyTxt = (EditText) rootView.findViewById(R.id.text_grocery_qty);
        checkbox = (CheckBox) rootView.findViewById(R.id.grocery_item_checkbox);

        nameTxt.setText(data.getString(COL_NAME));
        qtyTxt.setText(Integer.toString(data.getInt(COL_QTY)));

        if(data.getInt(COL_CHECKED) == GroceriesContract.GroceryEntry.CHECKED){
            checkbox.setChecked(true);
        }

        itemId = data.getLong(COL_ID);

        //for autocomplete suggestions
        nameTxt.setAdapter(Utility.createItemNamesAdapter(getActivity()));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //do nothing
    }

    long getIdPassedToFragment(){
        Intent intent = getActivity().getIntent();
        if(getArguments() != null) {
            String idString = getArguments().getString("id");
            itemId = Long.parseLong(idString);
        } else if(intent != null){
            itemId = intent.getLongExtra(GroceriesContract.GroceryEntry._ID, 0);
        }

        return itemId;
    }

    private void updateGroceryItem(){
        ContentValues updatedValues = new ContentValues();

        String name = nameTxt.getText().toString();
        String qtyString = qtyTxt.getText().toString();

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

        updatedValues.put(GroceriesContract.GroceryEntry._ID, itemId);
        updatedValues.put(GroceriesContract.GroceryEntry.COLUMN_NAME, name);
        updatedValues.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, Integer.parseInt(qtyString));

        if(checkbox.isChecked()){
            Log.d(LOG_TAG, "setting checked");
            updatedValues.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED, GroceriesContract.GroceryEntry.CHECKED);
        } else {
            Log.d(LOG_TAG, "setting unchecked");
            updatedValues.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED, GroceriesContract.GroceryEntry.UNCHECKED);
        }

        String selection = GroceriesContract.GroceryEntry._ID + " = ? ";
        String args[] = new String[] { Long.toString(itemId) };
        int count = getActivity().getContentResolver().update(GroceriesContract.GroceryEntry.CONTENT_URI
                , updatedValues
                , selection
                , args);

        Utility.addItemNameEntry(getActivity(), name);

        if(count != 1){
            Log.d(LOG_TAG, "Updating grocery item with id = " + itemId + " returned a count not equal to one.");
        }

        Toast.makeText(getActivity()
            , R.string.grocery_item_updated
            , Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    private void deleteGroceryItem(){
        String selection = GroceriesContract.GroceryEntry._ID + " = ? ";
        String args[] = new String[] { Long.toString(itemId) };

        int count = getActivity().getContentResolver().delete(GroceriesContract.GroceryEntry.CONTENT_URI
            , selection
            , args);

        if(count != 1){
            Log.d(LOG_TAG, "Deleting grocery item with id = " + itemId + " returned a count not equal to one.");
        }

        Toast.makeText(getActivity()
            , R.string.grocery_item_deleted
            , Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }
}
