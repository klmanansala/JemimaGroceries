package klmanansala.apps.jemimasgroceries;

import java.text.SimpleDateFormat;

public class Utility {
    public static final String DATE_FORMAT = "MM/dd/yyyy";

    public static String getFormattedDate(long date){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(date);
    }
}
