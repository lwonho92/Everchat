package com.lwonho92.everchat.data;

import android.content.Context;
import android.net.Uri;

import com.lwonho92.everchat.R;

import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Utils {
    public static String getMillisToStr(Long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        String ampm = "";
        switch(cal.get(Calendar.AM_PM)) {
            case Calendar.AM:
                ampm = "am";
                break;
            case Calendar.PM:
                ampm = "pm";
                break;
        }
        return String.format("%s %d : %02d", ampm, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    public static Uri convertSourceMessageToUri(String sourceMessage, String sourceLanguage, String targetLanguage) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("translate.google.com")
                .appendPath("m")
                .appendEncodedPath("translate#" + sourceLanguage)
                .appendPath(targetLanguage)
                .appendPath(sourceMessage);

        return builder.build();
    }

    public static void setCalligraphyConfig(Context context) {
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(context.getString(R.string.font_style))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
