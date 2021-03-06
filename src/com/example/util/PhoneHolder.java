package com.example.util;

/**
 * Simple class to hold details from the call log.
 * 
 * These will be stored in a list and used to populate the main list view when the phone tab is selected.
 */
public class PhoneHolder
{
	public PhoneHolder(String p_contact, long p_timestamp, long p_id, int p_type)
	{
		m_contact = p_contact;
		m_timestamp = p_timestamp;
		m_id = p_id;
		m_type = p_type;
	}
	
	public String getContact()
	{
		return m_contact;
	}
	
	public long getTimestamp()
	{
		return m_timestamp;
	}
	
	public long getID()
	{
		return m_id;
	}
	
	public int getType()
	{
		return m_type;
	}
	
	private String m_contact;
	private long m_timestamp;
	private long m_id;
	private int m_type;
}
