package com.example.messaginglistwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

//TODO Need this?
//import com.example.messaginglistwidget.R;
/**
 * Notes:
 * onUpdate() in the widget is skipped the first time the widget is created, so this activity
 * must setup the widget initially.
 * 
 * 
 *
 */
public class MessagingConfigureActivity extends Activity
{
	static final String TAG = "MessagingWidgetConfigure";

	private static final String PREFS_NAME = "com.example.messaginglistwidget.MessagingProvider";
	private static final String PREF_PREFIX_KEY = "prefix_";
	
	int m_widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;

	CheckBox m_checkBox;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		// Initially set to cancelled- widget won't be placed if the BACK button is pressed
		setResult(RESULT_CANCELED);
		
		// Our view layout
		setContentView(R.layout.configure);

		// Find our checkbox
		m_checkBox = (CheckBox)findViewById(R.id.autoRefreshCB);
		findViewById(R.id.okButton).setOnClickListener(m_onClickListener);

		// get widget id
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			m_widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if(m_widgetID == AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			finish();// bail
		}
	}

	View.OnClickListener m_onClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			final Context context = MessagingConfigureActivity.this;
			//save the state of the checkbox
			saveState(context, m_widgetID, m_checkBox.isChecked());

			// Push widget update to surface
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			MessagingProvider.updateAppWidget(context, manager, m_widgetID, m_checkBox.isChecked());

			// Pass back the original widget id
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, m_widgetID);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};
	
	static void saveState(Context p_context, int p_widgetID, boolean p_isEnabled)
	{
		SharedPreferences.Editor prefs = p_context.getSharedPreferences(PREFS_NAME, 0).edit();
		prefs.putBoolean(PREF_PREFIX_KEY + p_widgetID, p_isEnabled);
		prefs.commit();
	}
	static boolean loadState(Context p_context, int p_widgetID)
	{
		SharedPreferences prefs = p_context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getBoolean(PREF_PREFIX_KEY + p_widgetID, false);
	}
}
