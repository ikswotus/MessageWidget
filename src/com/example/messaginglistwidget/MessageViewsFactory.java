package com.example.messaginglistwidget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.example.util.SmsHolder;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;


public class MessageViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	// TODO: Replace this with sms list
	private static List<SmsHolder> m_smsList = new ArrayList<SmsHolder>();

	
	private Context m_context;
	private int m_appWidgetID;

	/** HACK to get new messages to display immediately without having to hit refresh */
	private boolean m_syncing;
	
	public MessageViewsFactory(Context p_context, Intent p_intent)
	{
		this.m_appWidgetID = p_intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                  AppWidgetManager.INVALID_APPWIDGET_ID);
		this.m_context = p_context;
		
	}
	
	/**
	 * Return the size of the list
	 * 
	 */
	@Override
	public int getCount()
	{
		return m_smsList.size();
	}

	@Override
	public long getItemId(int p_position)
	{
		return p_position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		Log.d("MVF:", " Getting loading view now ");

		if(!m_syncing)
		{
			return null;
		}
		RemoteViews emptyView = new RemoteViews(m_context.getPackageName(),
                								R.layout.empty_row);

		//TODO: This fills in every item in the listview :(
		emptyView.setTextViewText(R.id.empty_row_item, "Syncing...");
		
		// Return
		return emptyView;
	}

	@Override
	public RemoteViews getViewAt(int p_position)
	{
		if(m_syncing) // We're refreshing. So the first time we try to get a view make sure we've updated our collection data
		{
			// Sleep
			try
			{
				//TODO: This needs to be long enough that MMS adds it to the database, but not too long that it's obnoxious to the user.
				// Test this on a device... Note the sleep time seems to be dwarfed by the time taking to populate the list
				// We'll need to fix the populate to only add new texts (cache the timestamp of the last received message?)
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				Log.d("MVF: Sleep interruped", e.toString());
			}
			populateList();
			// We don't want to populate the array EVERY time.
			m_syncing = false;
		}
		Log.d("RV: getting view","at position!");
	    RemoteViews row=new RemoteViews(m_context.getPackageName(),
                                        R.layout.row);

	    row.setTextViewText(R.id.row_item, m_smsList.get(p_position).getContact() + " : " + m_smsList.get(p_position).getBody());

	    Intent i=new Intent();
	    Bundle extras=new Bundle();

	    //extras.putString(MessagingProvider.SMS, m_smsList.get(p_position).getBody());

	    // Add the sms thread id to the extras!
	    //Log.d("RV: ", " putting long : " + Long.toString(m_smsList.get(p_position).getID()));
	    extras.putLong(MessagingProvider.SMS_THREAD_ID, m_smsList.get(p_position).getID());
	    i.putExtras(extras);
	    row.setOnClickFillInIntent(R.id.row_item, i);

	    return(row);
	}

	@Override
	public int getViewTypeCount()
	{
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	/**
	 * Called when we need to setup our connection to the underlying data.
	 */
	@Override
	public void onCreate()
	{
		Log.d("MVF:", "ONCREATE _______________ POPULATE!");
		// Initial contents of our list.
		populateList();
	}

	/**
	 *  AppWidgetManager has notified us the data has changed
	 *	Currently we just set a flag - getViewAt() will load the most recent data
	 *  when the list is being populated.
	 */
	@Override
	public void onDataSetChanged()
	{
		Log.d("MVF", "data set changed - updating list!");
		m_syncing = true;
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		
	}

	private void populateList()
	{
		Log.d("MVF", " populating list");
		m_smsList.clear();
    	Cursor smsCursor = m_context.getContentResolver().query(
    			Uri.parse("content://sms/inbox"),
    			new String[] {"_id", "thread_id", "address", "person", "date", "body" },
    			null,
    			null,
    			"date DESC");
        while(smsCursor.moveToNext())
        {
        	Log.d("MVF","Moving to next: " + Long.toString(smsCursor.getLong(1)));
        	m_smsList.add( new SmsHolder(smsCursor.getString(5), getContactNameFromNumber(smsCursor.getString(2)), smsCursor.getLong(1)));
        }
        smsCursor.close();
	}


    /**
     * Obtains a contact display name from a phone number
     *
     * @param p_number The target phone number
     * @return The contact name associated with the supplied number, or the phone number
     *     if the name could not be retrieved from the contacts.
     */
    private String getContactNameFromNumber(String p_number)
    {
    	String contact = p_number;
    	// Resolve Contact
    	ContentResolver contactResolver = m_context.getContentResolver();
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
            Log.d("MVF", ("Found contact for: " + p_number));
    		contact = contactLookupCursor.getString(0);
    	}
    	contactLookupCursor.close();

    	return contact;
    }

}
