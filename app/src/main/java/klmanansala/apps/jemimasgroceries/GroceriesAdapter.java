package klmanansala.apps.jemimasgroceries;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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

        String name = cursor.getString(GroceriesFragment.COL_NAME);
        nameTxt.setText(name);
    }
}
