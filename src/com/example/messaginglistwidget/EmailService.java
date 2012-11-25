package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class EmailService extends RemoteViewsService
{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent p_intent)
	{
		return new EmailViewsFactory(this.getApplicationContext());
	}
}
