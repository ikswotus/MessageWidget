package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class PhoneService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent p_intent)
	{
		return new PhoneViewsFactory(this.getApplicationContext(), p_intent);
	}
}
