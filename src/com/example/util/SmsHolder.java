package com.example.util;

/**
 * A simple class to encapsulate the data that will be stored in the list used to populate the 
 * widget views.
 *
 * TODO: Add fields only as needed - We might be creating a lot of these guys 
 */
public class SmsHolder
{
	public SmsHolder(String p_body, String p_contact, long p_id, long p_timestamp)
	{
		m_body = p_body;
		m_contact = p_contact;
		m_id = p_id;
		m_timestamp = p_timestamp;
	}

	public String getBody()
	{
		return m_body;
	}

	public String getContact()
	{
		return m_contact;
	}

	public long getID()
	{
		return m_id;
	}
	
	public long getTime()
	{
		return m_timestamp;
	}
	
	String m_body;
	String m_contact;
	long m_id;
	long m_timestamp;
}
