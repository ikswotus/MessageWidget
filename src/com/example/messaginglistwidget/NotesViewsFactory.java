package com.example.messaginglistwidget;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class NotesViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	private Context m_context;
	
	public NotesViewsFactory(Context p_context)
	{
		this.m_context = p_context;
	}
	
	@Override
	public int getCount()
	{
		return 1; // m_notesList.size();
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// Use Default
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		// TODO Auto-generated method stub
		RemoteViews notesView = new RemoteViews(m_context.getPackageName(),
											    R.layout.notes_layout);
		notesView.setTextViewText(R.id.notes_title, "First Note");
		notesView.setTextViewText(R.id.notes_date, "1/1/11");

		Log.d("NotesViewsFactory", " getViewAt ");
		
		return notesView;
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
		Log.d("NotesViewsFactory", " onCreate E");
	}

	@Override
	public void onDataSetChanged()
	{
		Log.d("NotesViewsFactory", " onDataSetChanged E");
	}

	@Override
	public void onDestroy()
	{
		Log.d("NotesViewsFactory", " onDestroy E");
	}

}
