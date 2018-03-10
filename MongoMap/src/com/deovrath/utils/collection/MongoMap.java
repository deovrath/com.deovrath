package com.deovrath.utils.collection;

import java.util.Date;

import org.bson.Document;

import com.deovrath.utils.Logger.InOutError;
import com.deovrath.utils.mongo.MongoUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

public class MongoMap 
{
	private static final class Config
	{
		public static final String COLLECTION = "KeyValueConfig";
		public static final String KEY_COLUMN = "KEY";
		public static final String VALUE_COLUMN = "VALUE";
		public static final String CREATED_KEY = "CREATED";
		public static final String MODIFIED_KEY = "MODIFIED";
	}
	
	private static final String CLASS_NAME = MongoMap.class.getSimpleName();
	private MongoUtil mongo = null;
	
	public MongoMap(String mongoDatabase, int port, String uniqueProjectName, InOutError error)
	{
		mongo = new MongoUtil(mongoDatabase, port, uniqueProjectName,Config.COLLECTION, error);
	}
	
	public synchronized Boolean set(String key, String value, InOutError error)
	{
		try
		{
			FindIterable<Document> result =  mongo.search(Config.KEY_COLUMN, key, null, Boolean.FALSE, 1, error);

			if(result != null)
			{
				Document d = result.first();
				
				if(d != null)
				{
					d.put(Config.VALUE_COLUMN, value);
					d.put(Config.MODIFIED_KEY, new Date());
					
					long count = mongo.update(d, error);
					
					if(error.getError() != null)
					{
						throw(new Exception(String.format("Failed to update key:value %s:%s", key, value)));
					}
					
					if(count > 0)
					{
						return(Boolean.TRUE);
					}
					else
					{
						return(Boolean.FALSE);
					}
				}
				else
				{
					d = new Document();
					d.put(Config.KEY_COLUMN, key);
					d.put(Config.VALUE_COLUMN, value);
					
					Date now = new Date();
					d.put(Config.CREATED_KEY, now);
					d.put(Config.MODIFIED_KEY, now);
					
					Boolean success = mongo.insert(d, error);
					
					if(error.getError() != null)
					{
						throw(new Exception(String.format("Failed to insert key:value %s:%s", key, value)));
					}
					
					return(success);
				}
			}
			
			return(Boolean.FALSE);
			
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while updating row in CustomQueue.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	
	public String get(String key, InOutError error)
	{
		try
		{
			FindIterable<Document> result =  mongo.search(Config.KEY_COLUMN, key, null, Boolean.FALSE, 1, error);
			
			if(result != null)
			{
				Document d = result.first();
				
				if(d == null)
				{
					return(null);
				}
						
				return(d.getString(Config.VALUE_COLUMN));
			}
			else
			{
				return(null);
			}
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while getting key [ %s ] value from MongoMap.", CLASS_NAME, key), ex);
			return(null);
		}
	}
	
	public synchronized Boolean delete(String key, InOutError error)
	{
		try
		{	
			Boolean success = mongo.delete(Filters.eq(Config.KEY_COLUMN,key), error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to delete key %s", key)));
			}
			
			return(success);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while deleting key [ %s ] value from MongoMap.", CLASS_NAME, key), ex);
			return(null);
		}
	}
}
