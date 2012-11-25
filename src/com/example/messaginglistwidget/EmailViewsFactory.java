package com.example.messaginglistwidget;

import java.util.LinkedList;

import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.content.Context;

public class EmailViewsFactory implements RemoteViewsService.RemoteViewsFactory
{

	private Context m_context;
	LinkedList<String> m_emailList;
	
	public EmailViewsFactory(Context p_context)
	{
		this.m_context = p_context;
		m_emailList = new LinkedList<String>();
	}
	
	@Override
	public int getCount()
	{
		return 1; //m_emailList.size();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// Use default
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		RemoteViews emailView = new RemoteViews(m_context.getPackageName(),
				                                R.layout.email_layout);
		emailView.setTextViewText(R.id.fromAddress, "From:");
		emailView.setTextViewText(R.id.subject, "Subject");

		return emailView;
	}

	@Override
	public int getViewTypeCount()
	{
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
		Log.d("EmailViewsFactory", " onCreate E");
	}

	@Override
	public void onDataSetChanged()
	{
		Log.d("EmailViewsFactory", " onDataSetChanged E");		
	}

	@Override
	public void onDestroy()
	{
		Log.d("EmailViewsFactory", " onDestroy E");
	}

}
