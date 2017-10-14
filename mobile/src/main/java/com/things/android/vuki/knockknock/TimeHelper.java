package com.things.android.vuki.knockknock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mvukosav
 */
public class TimeHelper {

    public static String getDateFromTimestamp( long timestamp ) {
        SimpleDateFormat formatter = new SimpleDateFormat( "dd/MM/yyyy", Locale.getDefault() );
        return formatter.format( new Date( timestamp ) );
    }
}
