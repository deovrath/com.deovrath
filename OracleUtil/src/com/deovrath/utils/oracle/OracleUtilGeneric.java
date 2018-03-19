package com.deovrath.utils.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.deovrath.utils.Logger.InOutError;

public class OracleUtilGeneric 
{
	private static final String CLASS_NAME = OracleUtilGeneric.class.getSimpleName();
	private static Connection dbConnection = null;
	
	private String host;
	private String port;
	private String sid;
	private String user;
	private String password;

	
	public OracleUtilGeneric(String host, String port, String sid, String user, String password)
	{
		this.host = host;
		this.port = port;
		this.sid = sid;
		this.user = user;
		this.password = password;
		
	}
	
	public void closeConnection()
	{
		if(dbConnection != null)
		{
			try 
			{
				dbConnection.close();
			}
			catch (SQLException e) 
			{
				//do nothing.
			}
		}
	}
	
	private Connection getConnection(InOutError error) 
	{
		try 
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection cnn = DriverManager.getConnection(String.format("jdbc:oracle:thin:@%s:%s/%s",host,port,sid),user,password); 
			return(cnn);
		}
		catch (ClassNotFoundException ex) 
		{
			error.setError(String.format("%s : Error while opening oracle db connection [host: %s, port: %s, sid: %s, user: %s]",CLASS_NAME,host,port,sid,user),ex);
			return null;
		} 
		catch (SQLException ex) 
		{
			error.setError(String.format("%s : Error while opening oracle db connection [host: %s, port: %s, sid: %s, user: %s]",CLASS_NAME,host,port,sid,user),ex);
			return null;
		}  
	}
	
	
	public ResultSet getQueryResult(String query, InOutError error)
	{
		if(dbConnection==null)
		{
			dbConnection = getConnection(error);
		}
		
		if(error.getError() != null)
		{
			return null;
		}
		
		if(dbConnection != null) 
		{
			Statement statement = null;
			ResultSet rs = null;
			try
			{
				statement = dbConnection.createStatement();
				rs = statement.executeQuery(query);
				return(rs);
			}
			catch(Exception ex)
			{
				error.setError(String.format("%s : Error while queryRecords [%s]",CLASS_NAME,query),ex);
				return(null);
			}
			finally
			{
				if(statement != null)
				{
					try {
						statement.close();
					} catch (SQLException e) {
						
						// do nothing.
					}
				}
				if(rs != null)
				{
					try {
						rs.close();
					} catch (SQLException e) {
						// do nothing.
					}
				}
			}
		}
		else
		{
			return(null);
		}
		
	}
	
}
