package com.example.messaginglistwidget;

import java.util.LinkedList;

import com.example.util.CommonUtils;
import com.example.util.PhoneHolder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * This will be used to generate the phone tab view
 * 
 * This view will display information about recent calls (missed/outgoing/incoming)
 *
 * Clicking on a list element take you to the 'details' page of the call
 */
public class PhoneViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	/** Debug tag for the current component */
	private static final String TAG = PhoneViewsFactory.class.getSimpleName();

	private void log(String p_message)
	{
		Log.d(TAG, p_message);
	}

	/**
	 *  Want to display a message if our call list is empty - otherwise it looks like the widget is broken :(
	 *  Note: This may or may not be a hack...Sample widgets do this too, so there might not be a better way atm
	*/
	private boolean m_useEmptyView;
	
	/** Our list of recent phone activity - This will be populated using the call log content provider
	 * 
	 * 	See com.example.util for details on the PhoneHolder -> Convenience object to hold fields we need to populate the views.
	 *  */
	private LinkedList<PhoneHolder> m_callList = new LinkedList<PhoneHolder>();
	
	/** Saved Application Context */
	private Context m_context;
	
	/**
	 * Constructor
	 * 	Called from PhoneService when the phone tab is activated
	 * @param p_context The current application context
	 * @param p_intent Unused at the moment - Can be used to get the widget ID of who created us which may be needed in the future.
	 */
	public PhoneViewsFactory(Context p_context, Intent p_intent)
	{
		log(" PhoneViewsFactory constructed!");
		m_context = p_context;
	}

	/**
	 * Return the number of elements in the list
	 * 
	 * @return The size of the recent calls list, or 1 if the call list is empty.
	 * 	We lie and say 1 so we can display the "No recent call activity" message in the
	 *  content pane.
	 */
	@Override
	public int getCount()
	{
		log(" getCount returning: " + m_callList.size());
		// Lie - Always report size >= 1. We'll use an empty view if the call list is empty.
		//TODO: see if size() is O(n) for LinkedLists. We could probably just use a counter updated in populateList();
		return m_callList.size() > 0 ? m_callList.size() : 1;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		log(" getLoadingView E");
	    // TODO: Add a loading RemoteViews
		return null;
	}

	/**
	 * Called to actually populate the listview.
	 * This will be called AFTER getCount() -> First the number of views is determined by getCount,
	 * then this will be called for each position
	 * 
	 * @return A view that has been populated with the information saved in our list. This view is added
	 * as a row in our main layout's list view
	 */
	@Override
	public RemoteViews getViewAt(int position)
	{
		log(" getViewAt E. Position = " + position);

		RemoteViews callView = new RemoteViews(m_context.getPackageName(), R.layout.call_item_row);

		// Call log is empty
		if(m_useEmptyView)
		{
			callView.setTextViewText(R.id.call_row_contact, "No recent phone activity");
			return callView;
		}
		PhoneHolder currentEntry = m_callList.get(position);
		
		// Fill in the row view
		String tempContact = CommonUtils.getContactNameFromNumber(currentEntry.getContact(), m_context, false);
		callView.setTextViewText(R.id.call_row_contact, tempContact.isEmpty() ? "Unknown" : tempContact);
		callView.setTextViewText(R.id.call_row_number, currentEntry.getContact());
		callView.setTextViewText(R.id.call_row_date, CommonUtils.formatTimeStampString(currentEntry.getTimestamp(), false, m_context));
		
		switch(currentEntry.getType())
		{
		case android.provider.CallLog.Calls.OUTGOING_TYPE:
			callView.setImageViewResource(R.id.call_row_details, R.drawable.outgoing);
			break;
		case android.provider.CallLog.Calls.INCOMING_TYPE:
			callView.setImageViewResource(R.id.call_row_details, R.drawable.incoming_answered);
			break;
		case android.provider.CallLog.Calls.MISSED_TYPE:
			callView.setImageViewResource(R.id.call_row_details, R.drawable.incoming_missed);
			break;
			default:
		}
		
	    // Add the Call ID to the view so when it's clicked we open the right details page
	    Intent rowIntent = new Intent();
	    Bundle extras = new Bundle();
		extras.putLong(MessagingProvider.PHONE_CALL_ID, m_callList.get(position).getID());
	    rowIntent.putExtras(extras);
	    callView.setOnClickFillInIntent(R.id.call_row, rowIntent);
		
		return callView;
	}

	@Override
	public int getViewTypeCount()
	{
		log(" getViewTypeCount E");
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public void onCreate()
	{
		log(" onCreate E");
		this.m_useEmptyView = true;
		populateList();
	}

	@Override
	public void onDataSetChanged()
	{
		log(" onDataSetChanged E");
		populateList();
	}

	@Override
	public void onDestroy()
	{
		log(" onDestroy E");
	}

	private void populateList()
	{
		log(" populatingList E");
		// TODO: See if its better to repopulate every time, or just cache recent entries.
		// Caching should be faster since we can limit the results of the query (see MessagingViewsFactory for an example)
		// BUT! There's a problem -> if the user deletes their inbox, we can't update our view to reflect this...I'm guessing
		// this also invalidates our cached ID approach
		m_callList.clear();
		String[] callFields =
    		{
    			android.provider.CallLog.Calls.NUMBER,
    			android.provider.CallLog.Calls.TYPE,
    			android.provider.CallLog.Calls.CACHED_NAME,
    			android.provider.CallLog.Calls._ID,
    			android.provider.CallLog.Calls.DATE
    		};//TODO : Add more fields as needed

		Cursor callCursor = m_context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI,
    			callFields,
    			null,
    			null,
    			android.provider.CallLog.Calls.DATE + " ASC");
		while(callCursor.moveToNext())
		{
			log(" populateList - adding call to list ");
			m_callList.addFirst(new PhoneHolder(callCursor.getString(0), callCursor.getLong(4), callCursor.getLong(3), callCursor.getInt(1)));
		}
		// Determine if we need to display an empty view.
		m_useEmptyView = (m_callList.size() == 0);

		// TODO: Cache last call so the list doesn't have to be repeatedly cleared.
		callCursor.close();
	}

}
