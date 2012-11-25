package com.example.messaginglistwidget;

import com.example.messaginglistwidget.WebsiteListActivity;

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
 * 
 * 		 THIS CLASS CANNOT STORE STATE!
 * 			TODO: Map widgetID to variables if we want to track state. This provider class is created/destroyed whenever 
 * 				onReceive/onUpdate are called.
 * 
 * 
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

	/** Provides the PhoneViewsFactory with an ID to store the call_id in so we can access it here */
	public static final String PHONE_CALL_ID = "PHONE_CALL_ID";
	
	/** Really just the url...ID added for consistency */
	public static final String WEBSITE_URL_ID = "WEBSITE_URL_ID";

	public static final String NOTES_ID = "NOTES_ID";
	
	public static final String WEBSITE_NAME = "WEBSITE_NAME";
	
	/** Sent with a ListClick event - Will identify the type of action */
	public enum ItemType
	{
		sms,
		call,
		email,
		web
	}
	
	/** Action for when a list item is pressed */
	private static final String LIST_CLICK = "LIST_CLICK";
	
	/** Action for when the refresh button is clicked*/
	private static final String ACTION_REFRESH = "ACTION_REFRESH";

	/** Action for when the compose button is pressed */
	private static final String ACTION_COMPOSE = "ACTION_COMPOSE";

	/** Action to launch settings activity */
	private static final String CONFIGURE_SETTINGS = "CONFIGURE_SETTINGS";
	
	/** Action for when the SMS tab is selected */
	private static final String SMS_TAB = "SMS_TAB";
	
	/** Action for when the PHONE tab is selected */
	private static final String PHONE_TAB = "PHONE_TAB";
	
	/** Action for when the WEB tab is selected */
	private static final String WEB_TAB = "WEB_TAB";
	
	/** Email support */
	private static final String EMAIL_TAB = "EMAIL_TAB";
	
	/** Notes */
	private static final String NOTES_TAB = "NOTES_TAB";
	
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
	
	 /** Tracks which tab is currently active TODO: Dont think this should be static...but it gets reset between calls????*/
	 private static int m_currentTab;
	
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
	static void updateHelper(Context p_context, AppWidgetManager p_manager, int p_widgetID, Intent p_serviceIntent)
	{

	      
	      RemoteViews widget = new RemoteViews(p_context.getPackageName(),
	                                          R.layout.main_layout);
	      
	      widget.setRemoteAdapter(R.id.message_list, p_serviceIntent);
	      widget.setEmptyView(R.id.message_list,R.layout.empty_row);

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
	      smsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);

	      PendingIntent pendingSms = PendingIntent.getBroadcast(p_context, p_widgetID, smsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.smsTab, pendingSms);

	      // Phone tab
	      Intent phoneIntent = new Intent(p_context, MessagingProvider.class);
	      phoneIntent.setAction(PHONE_TAB);
	      phoneIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);
	      PendingIntent pendingPhone = PendingIntent.getBroadcast(p_context, p_widgetID, phoneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.phoneTab, pendingPhone);
	      
	      // Web tab
	      Intent webIntent = new Intent(p_context, MessagingProvider.class);
	      webIntent.setAction(WEB_TAB);
	      webIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);
	      PendingIntent pendingWeb = PendingIntent.getBroadcast(p_context, p_widgetID, webIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.webTab, pendingWeb);

	      // Email tab
	      Intent emailIntent = new Intent(p_context, MessagingProvider.class);
	      emailIntent.setAction(EMAIL_TAB);
	      emailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);
	      PendingIntent pendingEmail = PendingIntent.getBroadcast(p_context, p_widgetID, emailIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.emailTab, pendingEmail);
	      
	      // Notes tab
	      Intent notesIntent = new Intent(p_context, MessagingProvider.class);
	      notesIntent.setAction(NOTES_TAB);
	      notesIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);// TODO- what is this used for?
	      PendingIntent pendingNotes = PendingIntent.getBroadcast(p_context, p_widgetID, notesIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.notesTab, pendingNotes);
	      
	      // Configure button
	      Intent settingsIntent = new Intent(p_context, MessagingProvider.class);
	      settingsIntent.setAction(CONFIGURE_SETTINGS);
	      PendingIntent pendingSettings = PendingIntent.getBroadcast(p_context, p_widgetID, settingsIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	      widget.setOnClickPendingIntent(R.id.settingsButton, pendingSettings);
	      
	      p_manager.updateAppWidget(p_widgetID, widget);
	}
	
	static void updateHelper(Context p_context, AppWidgetManager p_manager, int p_widgetID)
	{
	      Intent svcIntent = new Intent(p_context, MessagingService.class);
	      
	      svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_widgetID);
	      svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
	      updateHelper(p_context, p_manager, p_widgetID, svcIntent);
	}
	
	@Override
	public void onReceive(Context p_context, Intent p_intent)
	{
		log(" onReceive E");

		//TODO Refactor this to look for an enum in the bundle and switch on it for proper view behavior
        if(p_intent.getAction().equals(LIST_CLICK))
        {
        	Bundle bun = p_intent.getExtras();
        	// Pass the thread ID to the messaging activity
        	if(bun.getLong(SMS_THREAD_ID) == 0)
        	{
        		if(bun.getLong(PHONE_CALL_ID) == 0)
        		{
        			// Should have WEBSITE_URL_ID
        			log(" WEB_ID [" + bun.getString(WEBSITE_URL_ID) + "]");
        			startWebActivity(p_context, bun.getString(WEBSITE_URL_ID));
        		}
        		else
        		{
        			// Should have PHONE_THREAD_ID
        			startCallLogActivity(p_context, bun.getLong(PHONE_CALL_ID));
        		}
        	}
        	else
        	{
            	startMessagingThreadActivity(p_context, bun.getLong(SMS_THREAD_ID));
        	}
        }
        else if(p_intent.getAction().equals(ACTION_COMPOSE))
        {
        	// Start generic compose activity - no specific thread
        	if(m_currentTab == R.id.smsTab)
        	{
        		startMessagingActivity(p_context);
        	}
        	else if(m_currentTab == R.id.phoneTab)
        	{
        		startPhoneActivity(p_context);
        	}
        	else if (m_currentTab == R.id.webTab)
        	{
        		//TODO Start web list activity
        		log(" TODO Start web list editor!");
        		Intent newWeb = new Intent(p_context, WebsiteListActivity.class);
        		newWeb.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		p_context.startActivity(newWeb);
        		notifyManager(p_context);// todo? Does this work???
        	}
        	else if(m_currentTab == R.id.emailTab)
        	{
        		// TODO launch email
        		log(" TODO: Start email composition");
        	}
        	else if(m_currentTab == R.id.notesTab)
        	{
        		// Launch notes
        		log(" TODO Start notes activity!");
        	}
        	else
        	{
        		log(" currentTab = " + m_currentTab);
        	}
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
        	m_currentTab = R.id.smsTab;
        	log("set current tab = " + m_currentTab);
            updateHelper(p_context, AppWidgetManager.getInstance(p_context), p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            updateTabs(R.id.smsTab, p_context);

        }
        else if(p_intent.getAction().equals(PHONE_TAB))
        {
        	log(" onReceive - PHONE Tab selected! ");
            m_currentTab = R.id.phoneTab;
        	// call update to switch the view
            Intent serviceIntent = new Intent(p_context, PhoneService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            updateHelper(p_context, AppWidgetManager.getInstance(p_context), p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID), serviceIntent);
            // Notify the content has changed?
            AppWidgetManager.getInstance(p_context).notifyAppWidgetViewDataChanged(
            		AppWidgetManager.getInstance(p_context).getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);  
            updateTabs(R.id.phoneTab, p_context);
        }
        else if(p_intent.getAction().equals(WEB_TAB))
        {
        	// Update web view
        	log(" onReceive() - WEB Tab selected! ");
        	m_currentTab = R.id.webTab;
        	Intent serviceIntent = new Intent(p_context, WebViewsService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            updateHelper(p_context, AppWidgetManager.getInstance(p_context), p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID), serviceIntent);
            // Notify the content has changed?
            AppWidgetManager.getInstance(p_context).notifyAppWidgetViewDataChanged(
            		AppWidgetManager.getInstance(p_context).getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);  
            // Currently does nothing - Add Web image
            updateTabs(R.id.webTab, p_context);
        }
        else if(p_intent.getAction().equals(EMAIL_TAB))
        {
        	log(" onReceive() - EML tab selected");
        	m_currentTab = R.id.emailTab;
        	Intent serviceIntent = new Intent(p_context, EmailService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            updateHelper(p_context, AppWidgetManager.getInstance(p_context), p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID), serviceIntent);
            // Notify the content has changed?
            AppWidgetManager.getInstance(p_context).notifyAppWidgetViewDataChanged(
            		AppWidgetManager.getInstance(p_context).getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);  
            // Currently does nothing - Add Web image
            updateTabs(R.id.emailTab, p_context);
        }
        else if(p_intent.getAction().equals(NOTES_TAB))
        {
        	log(" onReceive() - NOTES tab selected");
        	m_currentTab = R.id.notesTab;
        	Intent serviceIntent = new Intent(p_context, NotesService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            updateHelper(p_context, AppWidgetManager.getInstance(p_context), p_intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID), serviceIntent);
            // Notify the content has changed?
            AppWidgetManager.getInstance(p_context).notifyAppWidgetViewDataChanged(
            		AppWidgetManager.getInstance(p_context).getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);  
            // Currently does nothing - Add Web image
            updateTabs(R.id.notesTab, p_context);
        }
        else if(p_intent.getAction().equals(CONFIGURE_SETTINGS))
        {
        	log(" onRecieve - Settings activity launching");
        	// TODO: reopen configuration activity  (or launch a new activity designed to dynamically change settings)
        }
        super.onReceive(p_context, p_intent);

	}

	private void notifyManager(Context p_context)
	{
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(p_context);
   	 	appWidgetManager.notifyAppWidgetViewDataChanged(
   	 			appWidgetManager.getAppWidgetIds(new ComponentName(p_context, MessagingProvider.class )), R.id.message_list);       	

	}
	
	/** TODO: replace this with images. When the tab is selected, use a 'lit' image in place of a dark one
	 *        until that happens we'll use blueics color to display the currently selected tab.
	 * @param p_currentTabID
	 * @param p_context
	 */
	private void updateTabs(int p_currentTabID, Context p_context)
	{
	    RemoteViews remoteViews = new RemoteViews(p_context.getPackageName(),
                                                  R.layout.main_layout);
	    
    	remoteViews.setImageViewResource(R.id.phoneTab, R.drawable.phone_white);
    	remoteViews.setImageViewResource(R.id.smsTab, R.drawable.sms_white);
    	
    	switch(p_currentTabID)
    	{
    	case R.id.smsTab:
    		remoteViews.setImageViewResource(R.id.smsTab, R.drawable.sms_blue);
    		break;
    	case R.id.phoneTab:
    		remoteViews.setImageViewResource(R.id.phoneTab, R.drawable.phone_blue);
    		break;
    	
    	default: //TODO add other tabs as needed - remember to reset them all to white too
    			
    	}
    	
    	// publish changes
    	AppWidgetManager.getInstance(p_context).updateAppWidget(new ComponentName(p_context, MessagingProvider.class), remoteViews);

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
		newIntent.setData(ContentUris.withAppendedId(Uri.parse("content://mms-sms/conversations"), 0));
        p_context.startActivity(newIntent);	
	}
	
	private void startWebActivity(Context p_context, String p_url)
	{
        Intent webIntent = new Intent( Intent.ACTION_VIEW );
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webIntent.setData(Uri.parse("http://" + p_url));
        p_context.startActivity(webIntent);
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
	
	private void startPhoneActivity(Context p_context)
	{
		// Pretty simple - no additional info to append here
    	Intent intent = new Intent(Intent.ACTION_DIAL);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		p_context.startActivity(intent);
	}
	
	
	/**
	 * Opens a details page for a call.
	 * @param p_context
	 * @param p_callID
	 */
	private void startCallLogActivity(Context p_context, Long p_callID)
	{
		Intent newIntent = new Intent(Intent.ACTION_MAIN);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		newIntent.setClassName("com.android.contacts", "com.android.contacts.CallDetailActivity");
		newIntent.setData(ContentUris.withAppendedId(Uri.parse("content://call_log/calls"), p_callID));
		p_context.startActivity(newIntent);
	}

	@Override
	public void onUpdate(Context p_context,
						 AppWidgetManager p_widgetManager,
						 int[] p_widgetIDs)
	{
		Log.d("MP: ", "onUpdate E");

		 for (int widgetID : p_widgetIDs)
		 {
			 updateHelper(p_context, p_widgetManager, widgetID);
		 }
		 super.onUpdate(p_context, p_widgetManager, p_widgetIDs);
	}
}
