package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WebViewsService extends RemoteViewsService
{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent p_intent)
	{
		return new WebViewsFactory(this.getApplicationContext(), p_intent);
	}
	
}
