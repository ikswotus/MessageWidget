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

/**
 * TODO:
 * 		 Dynamically register/unregister the receiver for incoming text messages based on the configuration option? Is that allowed for widgets?
 * 
 * 		 More config options? Max number of texts? 
 * 
 * 		 Only display texts from unique contacts - Similar to the main conversation list of the mms app?
 * 
 * 		 Outgoing messages? -> It might be better to use the conversation list approach and have a display indicating the message direction (incoming/outbound)	
 * 
 * 		 Implement tabs -> will need to customize button behavior accordingly
 * 
 * Long-Term:
 * 		Make it pretty! Play around with style/layout/colors (First replace the buttons/text with images)
 */



public class MessagingProvider extends AppWidgetProvider
{
	/** Tag for log messages */
	private static final String TAG = MessagingProvider.class.getSimpleName();
	
    /** Provides the MessageViewsFactory a name to associate with the ID */
	public static final String SMS_THREAD_ID = "SMS_THREAD_ID";

	/** Action for when a list item is pressed */
	private static final String LIST_CLICK = "LIST_CLICK";
	
	/** Action for when the refresh button is clicked*/
	private static final String ACTION_REFRESH = "ACTION_REFRESH";

	/** Action for when the compose button is pressed */
	private static final String ACTION_COMPOSE = "ACTION_COMPOSE";

	/** Action for when the SMS tab is selected */
	private static final String SMS_TAB = "SMS_TAB";
	
	/** Action for when the PHONE tab is selected */
	private static final String PHONE_TAB = "PHONE_TAB";
	
	/** Configuration flag - If set to true, we notify MessageViewsFactory when SMS_RECEIVED occurs so it automatically updates the listview */
	private static boolean m_activeListen = false;

	/**
	 * Logging function - Logs everything at debug level
	 * @param p_message The current message to log.
	 */
	 private static void log(String p_message)
	 {
		 Log.d(TAG, p_message);
	 }
	
	
	/**
	 * Called from the ConfigurationActivity to perform initial setup once the user has confirmed the options.
     *
	 * @param p_context The current context
	 * @param p_manager The widget manager - Used to update the widget
	 * @param p_widgetID The current widget
	 * @param p_isEnabled  True if the config flag for activeListen was set.
	 */
	static void updateAppWidget(Context p_context, AppWidgetManager p_manager, int p_widgetID, boolean p_isEnabled)
	{
		log(" updateAppWidget E. p_widgetID = " + p_widgetID + ". IsEnabled = " + p_isEnabled);

		m_activeListen = p_isEnabled;
		
		updateHelper(p_context, p_manager, p_widgetID);
	}
	
	/**
	 *	Helper function to handle setting up the widget (initially after configuration and when onUpdate() is called
	 *
	 * @param p_context
	 * @param p_manager
	 * @param p_widgetID
	 */
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

	      //Register our tabs with actions
	      Intent smsIntent = new Intent(p_context, MessagingProvider.class);
	      smsIntent.setAction(SMS_TAB);
	      PendingIntent pendingSms = PendingIntent.getBroadcast(p_context, p_widgetID, smsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.smsTab, pendingSms);
	      
	      Intent phoneIntent = new Intent(p_context, MessagingProvider.class);
	      phoneIntent.setAction(PHONE_TAB);
	      PendingIntent pendingPhone = PendingIntent.getBroadcast(p_context, p_widgetID, phoneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.phoneTab, pendingPhone);
	      
	      p_manager.updateAppWidget(p_widgetID, widget);
	}
	
	
	
	@Override
	public void onReceive(Context p_context, Intent p_intent)
	{
		log(" onReceive E");

        super.onReceive(p_context, p_intent);

        if(p_intent.getAction().equals(LIST_CLICK))
        {
        	Bundle bun = p_intent.getExtras();
        	// Pass the thread ID to the messaging activity
        	startMessagingThreadActivity(p_context, bun.getLong(SMS_THREAD_ID));
        }
        else if(p_intent.getAction().equals(ACTION_COMPOSE))
        {
        	// Start generic compose activity - no specific thread
        	startMessagingActivity(p_context);
        }
        else if(p_intent.getAction().equals(ACTION_REFRESH))
        {
        	// New Text - Update the view.
        	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(p_context);
       	 	appWidgetManager.notifyAppWidgetViewDataChanged(
       	 			appWidgetManager.getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);       	
        }
        else if(m_activeListen && p_intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
        	// We're configured to actively update the view on new texts - do so now!
        	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(p_context);
       	 	appWidgetManager.notifyAppWidgetViewDataChanged(
       	 			appWidgetManager.getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);  
        }
        else if(p_intent.getAction().equals(SMS_TAB))
        {
        	// for now just log a message
        	log(" onReceive - SMS Tab selected!");
        }
        else if(p_intent.getAction().equals(PHONE_TAB))
        {
        	log(" onReceive - PHONE Tab selected! ");
        }
	}
	
	/**
	 * Launches the messaging activity with ComposeMessageActivity specified so we can immediately
	 * start a new message
     *
	 * @param p_context The current context.
	 */
	private void startMessagingActivity(Context p_context)
	{
		Intent newIntent = new Intent(Intent.ACTION_MAIN);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		newIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        p_context.startActivity(newIntent);	
	}
	
	/**
	 * Launches the messaging with a specific conversation ID so that messaging thread is displayed
	 * 
	 * 
	 * @param p_context Current context
	 * @param p_threadID The messaging thread ID to be opened
	 */
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
