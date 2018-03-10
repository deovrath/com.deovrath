package com.deovrath.utils.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class InOutError 
{
	private static final Logger logger = LogManager.getLogger(InOutError.class);
	private String error = null;
	
	public static final void log_info(String message)
	{
		logger.info(message);
	}
	
	public static final void log_error(String message)
	{
		logger.error(message);
	}
	
	public static final void log_error(String message, Throwable t)
	{
		logger.error(message, t);
	}
	
	public void setError(String message, Throwable t)
	{
		this.error = message;
		
		if(message != null)
		{
			logger.error(message, t);
		}
	}
	
	public void setError(String message)
	{
		this.error = message;
		
		if(message != null)
		{
			logger.error(message);
		}
	}
	
	public void reset()
	{
		this.error = null;
	}
	
	public String getError()
	{
		return(this.error);
	}
}
