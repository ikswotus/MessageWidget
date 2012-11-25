package com.example.messaginglistwidget;

import com.example.messaginglistwidget.R;
import com.example.messaginglistwidget.WebViewsFactory;

import android.app.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class WebsiteListActivity extends Activity
{
	@Override
	public void onCreate(Bundle icicle)
	{
		setContentView(R.layout.website_layout);
		super.onCreate(icicle);		
	}
	
	// called when button is clicked
	public void addWebsite(View p_view)
	{
		EditText textBox = (EditText) findViewById(R.id.siteText);
		//Execute callback
		WebViewsFactory.addToList(textBox.getText().toString());
		finish();
	}
}
