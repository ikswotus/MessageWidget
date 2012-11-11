package com.example.messaginglistwidget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.net.Uri;
import android.os.Bundle;

public class MessagingProvider extends AppWidgetProvider
{
	// TODO WHY ARE THESE PUBLIC?????
	public static final String SMS = "SMS";
	public static final String SMS_THREAD_ID = "SMS_THREAD_ID";

	public static final String LIST_CLICK = "LIST_CLICK";
	public static final String ACTION_REFRESH = "ACTION_REFRESH";
	public static final String ACTION_COMPOSE = "ACTION_COMPOSE";

	private static boolean m_activeListen = false;

	// called from configuration activity
	static void updateAppWidget(Context p_context, AppWidgetManager p_manager, int p_widgetID, boolean p_isEnabled)
	{
		Log.d("MP", "updateAppWidget! ID = " + p_widgetID + " enabled = " + p_isEnabled);
		// Load the boolean
		m_activeListen = p_isEnabled;
		
		
		updateHelper(p_context, p_manager, p_widgetID);
	}
	
	
	static void updateHelper(Context p_context, AppWidgetManager p_manager, int p_widgetID)
	{
	      Intent svcIntent = new Intent(p_context, MessagingService.class);
	      
	      svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);
	      svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
	      
	      RemoteViews widget = new RemoteViews(p_context.getPackageName(),
	                                          R.layout.main_layout);
	      
	      widget.setRemoteAdapter(R.id.message_list, svcIntent);

	      Intent clickIntent = new Intent(p_context, MessagingProvider.class);
	      clickIntent.setAction(LIST_CLICK);
	      PendingIntent pending = PendingIntent.getBroadcast(p_context, p_widgetID, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setPendingIntentTemplate(R.id.message_list, pending);	      

	      // Register our buttons with intents!
	      Intent refreshIntent = new Intent(p_context, MessagingProvider.class);
	      refreshIntent.setAction(ACTION_REFRESH);
	      PendingIntent pendingRefresh = PendingIntent.getBroadcast(p_context, p_widgetID, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	      widget.setOnClickPendingIntent(R.id.refreshButton, pendingRefresh);

	      Intent composeIntent = new Intent(p_context, MessagingProvider.class);
	      composeIntent.setAction(ACTION_COMPOSE);
	      PendingIntent pendingCompose = PendingIntent.getBroadcast(p_context, p_widgetID, composeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	      widget.setOnClickPendingIntent(R.id.composeButton, pendingCompose);
	      
	      p_manager.updateAppWidget(p_widgetID, widget);
	}
	
	
	
	@Override
	public void onReceive(Context p_context, Intent p_intent)
	{
		//It's a text update we'll want mms to add it to the database before we update. Do this here.
        super.onReceive(p_context, p_intent);

        if(p_intent.getAction().equals(LIST_CLICK))
        {
        	Log.d("MP:", "onRecieve - list clicked");

        	Bundle b = p_intent.getExtras();
        	// get the thread Id from the extras
        	startMessagingThreadActivity(p_context, b.getLong(SMS_THREAD_ID));
        	
        }
        else if(p_intent.getAction().equals(ACTION_COMPOSE))
        {
        	// Start generic compose activity - no specific thread
        	startMessagingActivity(p_context);
        }
        else if(p_intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") ||
                (p_intent.getAction().equals(ACTION_REFRESH)))
        {
        	// New Text - Update the view.
        	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(p_context);
       	 	appWidgetManager.notifyAppWidgetViewDataChanged(
       	 			appWidgetManager.getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);       	
        }
	}
	
	
	private void startMessagingActivity(Context p_context)
	{
		Intent newIntent = new Intent(Intent.ACTION_MAIN);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		newIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        p_context.startActivity(newIntent);	
	}
	
	
	private void startMessagingThreadActivity(Context p_context, Long p_threadID)
	{
		Intent newIntent = new Intent(Intent.ACTION_MAIN);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		newIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
		// Identify which thread to open
		newIntent.setData(ContentUris.withAppendedId(Uri.parse("content://mms-sms/conversations"), p_threadID));
        p_context.startActivity(newIntent);	
	}
	
	
	
	@Override
	public void onUpdate(Context p_context,
						 AppWidgetManager p_widgetManager,
						 int[] p_widgetIDs)
	{
		Log.d("MP: ", "onUpdate E");

		 for (int i=0; i< p_widgetIDs.length; i++)
		 {
			 updateHelper(p_context, p_widgetManager, p_widgetIDs[i]);
		 }
		 super.onUpdate(p_context, p_widgetManager, p_widgetIDs);
	}
}
