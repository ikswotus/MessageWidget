package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class NotesService extends RemoteViewsService
{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new NotesViewsFactory(this.getApplicationContext());
	}

}
