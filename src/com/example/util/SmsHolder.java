package com.example.util;

public class SmsHolder
{
	public SmsHolder(String p_body, String p_contact, long p_id)
	{
		m_body = p_body;
		m_contact = p_contact;
		m_id = p_id;
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
	
	
	String m_body;
	String m_contact;
	long m_id;
}
