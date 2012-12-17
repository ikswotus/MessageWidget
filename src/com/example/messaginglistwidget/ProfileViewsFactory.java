package com.example.messaginglistwidget;

import java.util.LinkedList;

import com.example.util.SystemProfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ProfileViewsFactory implements RemoteViewsService.RemoteViewsFactory
{
	// List of available profiles (TODO: hardcode options?)
	// More flexible approach -> allow users to create their own settings.
	private static LinkedList<SystemProfile> m_profileList = new LinkedList<SystemProfile>();
	
	private Context m_context;
	
	// Constructor called by service
	public ProfileViewsFactory(Context p_context)
	{
		this.m_context = p_context;
		// Construct starting profiles
		// TODO: Store them in a file? No content provider for LTS
		SystemProfile home = new SystemProfile("home", SystemProfile.VolumeSetting.loud, true, true);
		SystemProfile work = new SystemProfile("work", SystemProfile.VolumeSetting.silent, false, false);
		SystemProfile out = new SystemProfile("out", SystemProfile.VolumeSetting.vibrate, true, false);
		// Add
		m_profileList.add(home);
		m_profileList.add(work);
		m_profileList.add(out);
	}
	
	// Used by MessagingProvider to access current profile
	// Alternatively -> store everything in the pending intent?
	public static SystemProfile getProfile(int id)
	{
		return m_profileList.get(id);
	}
	
	@Override
	public int getCount()
	{
		return m_profileList.size();
	}

	@Override
	public long getItemId(int position)
	{
		// just use position as id
		return position;
	}

	@Override
	public RemoteViews getLoadingView()
	{
		// Use default
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		RemoteViews profileView = new RemoteViews(m_context.getPackageName(),
				                                  R.layout.profile_row);
		SystemProfile currentProfile = m_profileList.get(position);
		
		profileView.setTextViewText(R.id.profile_name,
				                    currentProfile.getName());
		if(currentProfile.getVolume() == SystemProfile.VolumeSetting.vibrate)
		{
	    	profileView.setImageViewResource(R.id.profile_volume, R.drawable.audio_vibrate);
		}
		else if(currentProfile.getVolume() == SystemProfile.VolumeSetting.silent)
		{
			profileView.setImageViewResource(R.id.profile_volume, R.drawable.audio_silent);
		}
		// else default 'loud' is fine (TODO: Does it reset to default between calls to getViewAt()???
		
		
	    Intent i = new Intent();
	    Bundle extras = new Bundle();
	    extras.putInt(MessagingProvider.LIST_CLICK_TYPE, MessagingProvider.k_profile);
		extras.putLong(MessagingProvider.PROFILE_ID, position);
	    i.putExtras(extras);
	    profileView.setOnClickFillInIntent(R.id.profile_row, i);
		
		
		return profileView;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public void onCreate()
	{
		// TODO? 
	}

	@Override
	public void onDataSetChanged()
	{
		// Data shouldnt really change...
		
	}

	@Override
	public void onDestroy()
	{
		// Clean up?
	}

}
