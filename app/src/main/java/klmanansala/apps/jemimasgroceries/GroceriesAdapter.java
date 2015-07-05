package klmanansala.apps.jemimasgroceries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;
import klmanansala.apps.jemimasgroceries.listeners.GroceryItemCheckboxListener;

/**
 * Created by kevin on 7/3/15.
 */
public class GroceriesAdapter extends CursorAdapter {

    public GroceriesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grocery_item_layout, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTxt = (TextView) view.findViewById(R.id.list_item_grocery);

        long id = cursor.getLong(GroceriesFragment.COL_ID);
        String name = cursor.getString(GroceriesFragment.COL_NAME);
        int qty = cursor.getInt(GroceriesFragment.COL_QTY);
        int checked = cursor.getInt(GroceriesFragment.COL_CHECKED);

        nameTxt.setText(name + " - " + qty);

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.grocery_item_checkbox);
        ContentValues values = new ContentValues();
        values.put(GroceriesContract.GroceryEntry._ID, id);
        values.put(GroceriesContract.GroceryEntry.COLUMN_NAME, name);
        values.put(GroceriesContract.GroceryEntry.COLUMN_QUANTITY, qty);
        checkbox.setOnClickListener(new GroceryItemCheckboxListener(values));

        if(checked == GroceriesContract.GroceryEntry.CHECKED){
            checkbox.setChecked(true);
        }
    }
}
