package com.example.messaginglistwidget;

import android.app.ListActivity;
import android.os.Bundle;

/**
 * Manages the notes list
 * 
 */
public class NotesViewsActivity extends ListActivity
{
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.notes_activity);
	}
}
