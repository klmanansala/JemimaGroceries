package klmanansala.apps.jemimasgroceries;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;

import klmanansala.apps.jemimasgroceries.data.GroceriesContract;

public class Utility {
    public static final String DATE_FORMAT = "MM/dd/yyyy";

    public static String getFormattedDate(long date){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(date);
    }

    public static Cursor getItemNamesCursor(Context context, CharSequence name){
        String select = GroceriesContract.ItemNameEntry.COLUMN_NAME + " LIKE ? ";
        String nameArg = null;
        if(name != null){
            nameArg = name.toString().toLowerCase();
        }
        String[]  selectArgs = { "%" + nameArg + "%"};
        String[] contactsProjection = new String[] {
                GroceriesContract.ItemNameEntry._ID,
                GroceriesContract.ItemNameEntry.COLUMN_NAME };

        return context.getContentResolver().query(GroceriesContract.ItemNameEntry.CONTENT_URI
                , contactsProjection
                , select
                , selectArgs
                , null);
    }
}
