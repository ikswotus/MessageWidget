package com.example.util;

public class EmailHolder
{
	public EmailHolder(String p_from, String p_subject)
	{
		m_from = p_from;
		m_subject = p_subject;
	}
	
	public String getSubject()
	{
		return m_subject;
	}
	
	public String getFrom()
	{
		return m_from;
	}
	
	private String m_from;
	private String m_subject;
}
