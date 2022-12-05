package com.flintcore.chat_app_android_22.utilities.dates;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static DateUtils dateUtils;
    private SimpleDateFormat format;

    private DateUtils(String pattern){
        this.format = new SimpleDateFormat(pattern, Locale.getDefault());
    }

    public static DateUtils getDateUtils(String pattern){
        return new DateUtils(pattern);
    }

    public String getReadableDate(Date date){
        return this.format.format(date);
    }

}
