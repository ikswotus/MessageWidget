package com.example.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;


/**
 * This class will hold simple functions that can be used in various locations.
 */
public class CommonUtils
{

    /**
     * Obtains a contact display name from a phone number by searching the contacts database.
     * If no contact is found for the provided number, we simply return the number itself so
     * the client doesn't have to check a return value.
     *
     * @param p_number The target phone number
     * @return The contact name associated with the supplied number, or the phone number
     *     if the name could not be retrieved from the contacts.
     */
    public static String getContactNameFromNumber(String p_number, Context p_context)
    {
    	String contact = p_number;
    	// Resolve Contact
    	ContentResolver contactResolver = p_context.getContentResolver();
    	Cursor contactLookupCursor =  
    			contactResolver.query(
    	            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
    	            Uri.encode(p_number)), 
    	            new String[] {PhoneLookup.DISPLAY_NAME}, 
    	            null, 
    	            null, 
    	            null);
    	if(contactLookupCursor.moveToFirst())
    	{
    		contact = contactLookupCursor.getString(0);
    	}
    	contactLookupCursor.close();
    
    	return contact;
    }

    /**
     * Stolen from packages/apps/Mms/src/com/android/mms/messageutils.java
     * Converts a 'long' date into a timestamp string
     * 
     * @param p_timestamp The date from the messsage
     * @param p_fullFormat True if we should include the full date
     */
    public static String formatTimeStampString(long p_timestamp, boolean fullFormat, Context p_context)
    {
        Time then = new Time();
        then.set(p_timestamp);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                           DateUtils.FORMAT_ABBREV_ALL |
                           DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(p_context, p_timestamp, format_flags);
    }
}
