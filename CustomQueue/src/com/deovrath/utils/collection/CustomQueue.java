package com.deovrath.utils.collection;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.deovrath.utils.Logger.InOutError;
import com.deovrath.utils.mongo.MongoUtil;
import com.mongodb.client.FindIterable;

public class CustomQueue 
{
	private static final class Config
	{
		public static final String CREATED_KEY = "CREATED";
		public static final String MODIFIED_KEY = "MODIFIED";
		public static final String PROCESSED_KEY = "PROCESSED";
		public static final String PROCESSED_VALUE = "DONE";
		public static final String PROCESSING_VALUE = "PROCESSING";
		public static final String ERROR_VALUE = "ERROR IN PROCESSING";
		public static final String NOT_PROCESSED_VALUE = "NEW";
	}
	
	private static final String CLASS_NAME = CustomQueue.class.getSimpleName();	
	private MongoUtil mongo = null;	
	
	public CustomQueue(String mongoDatabase, int port, String uniqueProjectName, String topic, InOutError error)
	{
		mongo = new MongoUtil(mongoDatabase, port, uniqueProjectName, topic, error);
	}
	
	public Boolean push(String json, InOutError error)
	{
		try
		{			
			Document document = Document.parse(json);
			
			document.append(Config.PROCESSED_KEY,Config.NOT_PROCESSED_VALUE);
			
			Date now = new Date();
			
			document.append(Config.CREATED_KEY, now);
			document.append(Config.MODIFIED_KEY, now);
			
			Boolean success = mongo.insert(document, error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to insert document in queue. Document: [ %s ]", json)));
			}
			
			return(success);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while executing PUSH on CustomQueue",CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	public synchronized String pop(Boolean change_status_to_processing, InOutError error)
	{
		try
		{
			FindIterable<Document> result =  mongo.search(Config.PROCESSED_KEY, Config.NOT_PROCESSED_VALUE, Config.CREATED_KEY, Boolean.FALSE, 1, error);
			
			if(result != null)
			{
				Document d = result.first();
				
				if(d == null)
				{
					return(null);
				}
				
				Document update = new Document();
				update.put("_id",d.getObjectId("_id"));
				if(change_status_to_processing)
				{
					update.put(Config.PROCESSED_KEY,Config.PROCESSING_VALUE);
				}
				update.put(Config.MODIFIED_KEY, new Date());
				
				mongo.update(update, error);
				
				if(error.getError() != null)
				{
					throw(new Exception(String.format("Failed to update document status to %s. Document: [ %s ]", Config.PROCESSING_VALUE, d.toJson())));
				}
				
				return(d.toJson());
			}
			else
			{
				return(null);
			}
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while executing POP on CustomQueue.", CLASS_NAME), ex);
			return(null);
		}
	}
	
	
	public Boolean markProcessed(ObjectId id, InOutError error)
	{
		try
		{
			Document d = new Document();
			d.put("_id",id);
			d.put(Config.PROCESSED_KEY, Config.PROCESSED_VALUE);
			d.put(Config.MODIFIED_KEY, new Date());
			
			long count = mongo.update(d, error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to update document status to %s. Document: [ %s ]", Config.PROCESSED_VALUE, d.toJson())));
			}
			
			if(count < 1)
			{
				return(Boolean.FALSE);
			}
			
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while marking queue row as processed.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	public Boolean markError(ObjectId id, InOutError error)
	{
		try
		{
			Document d = new Document();
			d.put("_id",id);
			d.put(Config.PROCESSED_KEY, Config.ERROR_VALUE);
			d.put(Config.MODIFIED_KEY, new Date());
			
			long count = mongo.update(d, error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to update document status to %s. Document: [ %s ]", Config.ERROR_VALUE, d.toJson())));
			}
			
			if(count < 1)
			{
				return(Boolean.FALSE);
			}
			
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while marking row as error in CustomQueue.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	
	public Boolean markNew(ObjectId id, InOutError error)
	{
		try
		{
			Document d = new Document();
			d.put("_id",id);
			d.put(Config.PROCESSED_KEY, Config.NOT_PROCESSED_VALUE);
			d.put(Config.MODIFIED_KEY, new Date());
			
			long count = mongo.update(d, error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to update document status to %s. Document: [ %s ]", Config.NOT_PROCESSED_VALUE, d.toJson())));	
			}
			
			if(count < 1)
			{
				return(Boolean.FALSE);
			}
			
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while marking row as new in CustomQueue.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	
	public Boolean update(ObjectId id, String column, String value, InOutError error)
	{
		try
		{
			Document d = new Document();
			d.put("_id",id);
			d.put(Config.PROCESSED_KEY, Config.NOT_PROCESSED_VALUE);
			d.put(Config.MODIFIED_KEY, new Date());
			d.put(column, value);
			
			long count = mongo.update(d, error);
			
			if(error.getError() != null)
			{
				throw(new Exception(String.format("Failed to update document status to %s. Document: [ %s ]", Config.NOT_PROCESSED_VALUE, d.toJson())));
			}
			
			if(count < 1)
			{
				return(Boolean.FALSE);
			}
			
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while updating row in CustomQueue.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
}
