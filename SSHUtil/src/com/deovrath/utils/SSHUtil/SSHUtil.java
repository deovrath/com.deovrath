package com.deovrath.utils.SSHUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import com.deovrath.utils.Logger.InOutError;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHUtil 
{
	private static final String CLASS_NAME = SSHUtil.class.getSimpleName();
	private Session session = null;
	
	private String host;
	String user;
	String password;
	int port;
	InOutError error;
	
	public String getHost()
	{
		return(host);
	}
	
	public SSHUtil(String user, String password, String host, int port, InOutError error)
	{
		this.host = host;
		this.user = user;
		this.password = password;
		this.port = port;
		this.error = error;
		
		session = createSession(user, password, host, port, error);
	}
	
	private Session createSession(String user, String password, String host, int port, InOutError error) 
	{
        try 
        {
            JSch jsch = new JSch();
            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            return session;
        } 
        catch (JSchException ex) 
        {
            error.setError(String.format("%s : Error creating ssh session with %s@%s",CLASS_NAME,user,host),ex);
            return null;
        }
    }
    
    public String executeRemoteCommand(String command, InOutError error)
	{
		try
		{
	        Channel channel = session.openChannel("exec");
	        ((ChannelExec) channel).setCommand(command);
	        
	        channel.connect();
	        
	        byte[] buffer = new byte[1024];
	        int length;
	        InputStream in = channel.getInputStream();
	        ByteArrayOutputStream result = new ByteArrayOutputStream();
	        while((length=in.read(buffer))!=-1)
	        {
	        	result.write(buffer,0,length);
	        }
	         
	        channel.disconnect();
	        return(result.toString("utf-8"));
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Error while executing command - '%s'",CLASS_NAME,command),ex);
			return(null);
		}
	}
    
    public void disconnectSSHSession()
    {
    		if(session != null)
    		{
    			session.disconnect();
    		}
    }
    
    public Boolean checkSessionState()
    {
    		if(session!=null)
    		{
    			return(session.isConnected());
    		}
    		else
    		{
    			return(Boolean.FALSE);
    		}
    }
    
    public void check_session_and_reinitiate_if_closed()
    {
    		if(session == null)
    		{
    			session = createSession(user, password, host, port, error);
    		}
    		else
    		{
    			if(!session.isConnected())
    			{
    				session = createSession(user, password, host, port, error);
    			}
    		}
    }
    
    // Specific function using the core functions above
    
    public String getTotalLinesInRemoteFile(String fileName, InOutError error)
    {
	    	error.reset();
	    	String result = executeRemoteCommand(String.format("wc %s | awk '{print $1}'", fileName), error);
	    	return(result);
    }
    
    public String getRemoteFileContents(String fileName, String start, String end, InOutError error)
    {
	    	error.reset();
	    	String result = executeRemoteCommand(String.format("sed -n %s,%sp %s",start.trim(),end.trim(),fileName), error);
	    	return(result);
    }
}
