package com.example.messaginglistwidget;

import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class WebViewsFactory implements RemoteViewsFactory
{
	private final static String TAG = WebViewsFactory.class.getSimpleName();
	private Context m_context;

	// STATIC until the site activity is fixed
	private static LinkedList<String> m_websiteList;
	
	// Constructor
	public WebViewsFactory(Context p_context, Intent p_intent)
	{
		this.m_context = p_context;
		this.m_websiteList = new LinkedList<String>();
		m_websiteList.add("www.google.com");
		m_websiteList.add("www.xkcd.com");
	}

	public static void addToList(String p_newSite)
	{
		// Should only be called when a valid list has been created
		if(m_websiteList == null)
		{
			return;
		}
		m_websiteList.add(p_newSite);
	}
	
	@Override
	public int getCount()
	{
		// Return the size or lie and say we have 1
		return m_websiteList.size() > 0 ? m_websiteList.size() : 1;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		Log.d(TAG, " webViews = Getting position " + position);
		RemoteViews webViews = new RemoteViews(m_context.getPackageName(), R.layout.web_row);

		// View just gets the website name
		webViews.setTextViewText(R.id.web_row_name, m_websiteList.get(position));
		// Add url to pending intent
	    Intent i = new Intent();
	    Bundle extras = new Bundle();

	    // Add the sms thread id to the extras!
	    extras.putString(MessagingProvider.WEBSITE_URL_ID, m_websiteList.get(position));
	    i.putExtras(extras);
	    webViews.setOnClickFillInIntent(R.id.web_row, i);
		
		return webViews;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataSetChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub

	}
	
	private void populateList()
	{
		// Add list of entries
		
	}
}