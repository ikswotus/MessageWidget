package com.example.messaginglistwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent p_intent)
	{
		return new ProfileViewsFactory(this.getApplicationContext());
	}

}
