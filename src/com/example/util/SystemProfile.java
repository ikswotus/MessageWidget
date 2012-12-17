package com.example.util;

/**
 * Stores information about a given profile.
 * 
 * Specifically:
 *  1) Name of profile
 * 	2) Volume setting
 *  3) Data Setting
 *  4) Wifi setting
 *  
 *  TODO:: look into memory management - kill unnecessary services/apps
 * 
 */
public class SystemProfile
{
	public enum VolumeSetting
	{
		loud,
		vibrate,
		silent //OFF
	}
	
	
	public SystemProfile(String p_name, VolumeSetting p_volume,
			             boolean p_data, boolean p_wifi)
	{
		m_name = p_name;
		m_volume = p_volume;
		m_dataEnabled = p_data;
		m_wifiEnabled = p_wifi;
	}
	
	
	public String getName()
	{
		return m_name;
	}
	
	public boolean isDataEnabled()
	{
		return m_dataEnabled;
	}
	
	public boolean isWifiEnabled()
	{
		return m_wifiEnabled;
	}
	
	public VolumeSetting getVolume()
	{
		return m_volume;
	}
	
	private String m_name;
	private boolean m_dataEnabled;
	private boolean m_wifiEnabled;
	private VolumeSetting m_volume;

}
