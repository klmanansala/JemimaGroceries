package klmanansala.apps.jemimasgroceries.listeners;

import android.content.ContentValues;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;

/**
 * Created by kevin on 7/4/15.
 */
public class GroceryItemCheckboxListener implements View.OnClickListener {

    public static final String LOG_TAG = GroceryItemCheckboxListener.class.getSimpleName();

    private ContentValues groceryItemValues;

    public GroceryItemCheckboxListener(ContentValues groceryItemValues) {
        super();
        this.groceryItemValues = groceryItemValues;
    }

    @Override
    public void onClick(View v) {
        CheckBox checkbox = (CheckBox) v;

        //note that at this point the toggle to the checkbox has not happened yet
        //so the state being checked here is before the click on the
        //checkbox finishes
        if(checkbox.isChecked()){
            groceryItemValues.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED
                    , GroceriesContract.GroceryEntry.CHECKED);
        } else {
            groceryItemValues.put(GroceriesContract.GroceryEntry.COLUMN_CHECKED
                    , GroceriesContract.GroceryEntry.UNCHECKED);
        }

        long groceryItemId = groceryItemValues.getAsLong(GroceriesContract.GroceryEntry._ID);

        int count = v.getContext().getContentResolver().update(
                GroceriesContract.GroceryEntry.CONTENT_URI
                , groceryItemValues
                , GroceriesContract.GroceryEntry._ID + " = ? "
                , new String[] {Long.toString(groceryItemId)});

        if(count != 1){
            Log.d(LOG_TAG, "Count returned when updating grocery item with id = "
                + groceryItemId + " is not one.");
        }
    }
}
