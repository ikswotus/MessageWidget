package com.example.messaginglistwidget;

import java.util.LinkedList;

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
 * Clicking on the list item will take you to the 'details' page
 */
public class PhoneViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private static final String TAG = PhoneViewsFactory.class.getSimpleName();

	private void log(String p_message)
	{
		Log.d(TAG, p_message);
	}

	/** Want to display a message if our call list is empty - otherwise it looks like the widget is broken :( */
	private boolean m_useEmptyView;
	
	
	//TODO:BIG TODO: Static -> all widgets share this? This could be unsafe if each widget gets its own thread...
	// For now, just focus on getting a single widget working correctly then look into threading/multiple widgets
	private static LinkedList<PhoneHolder> m_callList = new LinkedList<PhoneHolder>();
	
	/** Saved Application Context */
	private Context m_context;
	
	public PhoneViewsFactory(Context p_context, Intent p_intent)
	{
		log(" PhoneViewsFactory constructed!");
		m_context = p_context;
	}

	@Override
	public int getCount()
	{
		log(" getCount returning: " + m_callList.size());
		//TODO: see if size() is O(n) for LinkedLists. We could probably just use a counter updated in populateList();
		return m_callList.size();
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
	//	RemoteViews
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		log(" getViewAt E. Position = " + position);
		// TODO Use a different layout for phone logs
		RemoteViews callView = new RemoteViews(m_context.getPackageName(), R.layout.row);
		callView.setTextViewText(R.id.row_contact, m_callList.get(position).getContact());
		callView.setTextViewText(R.id.row_date, "date"); // TODO Move #->name function to utils class

	    // Add the Call ID to the view so when it's clicked we open the right details page
	    Intent rowIntent = new Intent();
	    Bundle extras = new Bundle();
		extras.putLong(MessagingProvider.PHONE_CALL_ID, m_callList.get(position).getID());
	    rowIntent.putExtras(extras);
	    callView.setOnClickFillInIntent(R.id.row_item, rowIntent);
		
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
    			android.provider.CallLog.Calls.DATE + " DESC");
		while(callCursor.moveToNext())
		{
			log(" populateList - adding call to list ");
			m_callList.addFirst(new PhoneHolder(callCursor.getString(0), callCursor.getLong(4), callCursor.getLong(3)));
		}
		m_useEmptyView = (m_callList.size() == 0);
		// TODO: Cache last call
		callCursor.close();
	}

}
