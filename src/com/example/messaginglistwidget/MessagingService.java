package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class MessagingService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent p_intent)
	{
		return new MessageViewsFactory(this.getApplicationContext(), p_intent);
	}

}
