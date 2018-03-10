package com.deovrath.utils.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.deovrath.utils.Logger.InOutError;

public class OracleUtil 
{
	private final class Config
	{
		public static final int RECORDS_IN_SINGLE_RUN = 1000;
	}
	
	private static final String CLASS_NAME = OracleUtil.class.getSimpleName();
	private static Connection dbConnection = null;
	
	private String host;
	private String port;
	private String sid;
	private String user;
	private String password;
	private String cols;
	private String where;
	private String table;
	
	public String getTable()
	{
		return(table);
	}
	
	public OracleUtil(String host, String port, String sid, String user, String password, String table, String cols, String where)
	{
		this.host = host;
		this.port = port;
		this.sid = sid;
		this.user = user;
		this.password = password;
		this.table = table;
		this.cols = cols;
		this.where = where;
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
	
	public ResultSet getRecords(String last_row_id, InOutError error)
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
			try 
			{	
				String query = null; 
				
				if(last_row_id == null) 
				{
					query = "SELECT ROWIDTOCHAR(ROWID) ROW_ID, " + cols + " FROM " + table + " WHERE " + where + " ROWNUM <= " +  Config.RECORDS_IN_SINGLE_RUN + " ORDER BY ROWID";
				}
				else 
				{	
					query = "SELECT ROWIDTOCHAR(ROWID) ROW_ID, " + cols + " FROM " + table + " WHERE " + where + " ROWID < '" + last_row_id + "' AND ROWNUM <= " +  Config.RECORDS_IN_SINGLE_RUN + " ORDER BY ROWID";
				}
								
				Statement statement = dbConnection.createStatement();
				ResultSet rs = statement.executeQuery(query);
				return(rs);
			}
			catch(Exception ex) 
			{
				error.setError(String.format("%s : Error while getting records form oracle db connection [host: %s, port: %s, sid: %s, user: %s]",CLASS_NAME,host,port,sid,user),ex);
				return(null);
			}		
		}
		else
		{
			error.setError(String.format("%s : Oracle db connection is null [host: %s, port: %s, sid: %s, user: %s]",CLASS_NAME,host,port,sid,user));
			return(null);
		}
	}
}
