package com.example.messaginglistwidget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

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

/**
 * TODO:
 * 	1) investigate getLoadingView() - Currently we'll have 'Syncing...' displayed in EVERY textview in the list. It'd be nice
 * to have only a single view displayed.
 *
 */

public class MessageViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	/** Debugging tag for log messages */
	private static final String TAG = MessageViewsFactory.class.getSimpleName();
	
	/**
	 * Helper function to log all messages at the debug level
	 * @param p_message The message to log
	 */
	private void log(String p_message)
	{
		Log.d(TAG, p_message);
	}
	
    /** Stores the texts from the cursor TODO: Find a way to adapt the cursor to the textview directly */
	private static LinkedList<SmsHolder> m_smsList = new LinkedList<SmsHolder>();

	/**
	 * Save the context - Used to access content resolvers
	 */
	private Context m_context;

	/** Cache the last text message we added to our list so we don't have to rescan the entire inbox on an update */
	private static long m_lastMessageID = -1;

	/** We'll need an accurate count for the listview to be displayed properly
	 * If we delay updating the list, the count will be off by the amount of texts received since the last update and they
	 * will be missing from the listview. getCount() will need to return the most accurate count.
	 */
	private int m_count;
	// Cache the most recent text from the last refresh - this allows us to avoid having to repopulate the entire list
	
	
	/** HACK to get new messages to display immediately without having to hit refresh */
	private boolean m_syncing;
	/** True if we should display an empty view - "No Messages" */
	private static boolean m_useEmpty;
	
	/**
	 * Constructor
	 * 
	 * @param p_context The current context
	 * @param p_intent The provided intent (We can pull data out if we need to...)
	 */
	public MessageViewsFactory(Context p_context, Intent p_intent)
	{
		this.m_context = p_context;	
		log("Constructor");
	}
	
	/**
	 * Return the size of the list, which will be the size of the ArrayList of texts we've saved
     *
     * @return The current size of our list.
	 */
	@Override
	public int getCount()
	{
		log("getCount:" + m_smsList.size());
		// If we don't have any messages, pretend we have one - empty view.
		m_useEmpty = m_count == 0;
		return m_count > 0 ? m_count : 1;
	}

	/**
	 * If we want to associate a particular ID with a list position, we can do so here.
	 * 
	 * @return the ID of the current item.
	 */
	@Override
	public long getItemId(int p_position)
	{
		return p_position;
	}

	/**
	 * Called when the listview is loading, the returned view will be displayed in place of the collection.
	 * 
	 * if null is returned, a default view is used.
	 * 
	 * NOTE: This is currently a bit of a hack - m_syncing is used to get around the fact that the widget updates faster than
	 * incoming sms get added to the inbox database.
	 */
	@Override
	public RemoteViews getLoadingView()
	{
		log(" getLoadingView E");

		if(!m_syncing)
		{
			return null;
		}
		RemoteViews emptyView = new RemoteViews(m_context.getPackageName(),
                								R.layout.empty_row);

		//TODO: This fills in every item in the listview :(
		emptyView.setTextViewText(R.id.empty_row_item, "Syncing...");
		
		// Return our empty view
		return emptyView;
	}

	@Override
	public RemoteViews getViewAt(int p_position)
	{
		log(" Getting view at: " + p_position);

		RemoteViews row=new RemoteViews(m_context.getPackageName(),
                R.layout.row);
		
		if(m_useEmpty == true)
		{
			log("No messages - bailing");
		    row.setTextViewText(R.id.row_item, "No messages");
		    return row;
		}

	    row.setTextViewText(R.id.row_item, m_smsList.get(p_position).getContact() + " : " + m_smsList.get(p_position).getBody());

	    Intent i = new Intent();
	    Bundle extras = new Bundle();

	    // Add the sms thread id to the extras!
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
		log(" onCreate - Populating initial list");
		// Initial contents of our list.
		populateList(false);
	}

	/**
	 *  AppWidgetManager has notified us the data has changed
	 *	Currently we just set a flag - getViewAt() will load the most recent data
	 *  when the list is being populated.
	 */
	@Override
	public void onDataSetChanged()
	{
		log(" onDataSetChanged E");
		// Don't actually update the list here. We need to wait so the sms has time to get to the database
		// use m_syncing to force a wait.
		m_syncing = true;

		// Sleep so we can update the count correctly. (TODO Only do this when actively listening);
		try
		{
			Thread.sleep(350);
		}
		catch (InterruptedException e)
		{
			log(" DataSetChanged: Sleep interruped: " + e.toString());
		}
		
		populateList(true);
		log("onDataSetChanged X");
	}

	/**
	 * Widget destroyed!
	 */
	@Override
	public void onDestroy()
	{
		m_smsList.clear();
		m_lastMessageID = -1;
	}
	
	/**
	 * Populates our list of texts
	 * @param p_pushFront True if we should add new messages to the front of the list, false otherwise. Currently this is only
	 *     set to true when the list is first constructed. (Alternatively we could sort differently).
	 */
	private void populateList(boolean p_pushFront)
	{
		Log.d("MVF", " populating list");

    	Cursor smsCursor = m_context.getContentResolver().query(
    			Uri.parse("content://sms/inbox"),
    			new String[] {"_id", "thread_id", "address", "person", "date", "body" },
    			null,
    			null,
    			"date DESC");
    	
    	m_count = smsCursor.getCount();
    	// Loop until we hit the cached message
        while(smsCursor.moveToNext())
        {
        	log( " populateList: Current ID: " + smsCursor.getLong(0) + ": Message: " + smsCursor.getString(5));
        	if(m_lastMessageID == smsCursor.getLong(0))
        	{
        		Log.d("MVF:"," Found cached ID: " + m_lastMessageID);
        		break; // We already have this message
        	}
        	//Log.d("MVF","Moving to next: " + Long.toString(smsCursor.getLong(1)));
        	if(p_pushFront)
        	{
        		m_smsList.addFirst( new SmsHolder(smsCursor.getString(5), getContactNameFromNumber(smsCursor.getString(2)), smsCursor.getLong(1)));
        	}
        	else
        	{
        		m_smsList.add( new SmsHolder(smsCursor.getString(5), getContactNameFromNumber(smsCursor.getString(2)), smsCursor.getLong(1)));
        	}
        }
               
        // Save the most recent ID.
    	if(smsCursor.moveToFirst())
    	{
    		Log.d("MVF", "caching ID: " + smsCursor.getLong(0));
    		m_lastMessageID = smsCursor.getLong(0);
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
