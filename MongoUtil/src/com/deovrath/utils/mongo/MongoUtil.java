package com.deovrath.utils.mongo;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.deovrath.utils.Logger.InOutError;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;

public class MongoUtil 
{
	private static final String CLASS_NAME = MongoUtil.class.getSimpleName();
	private static MongoClient client = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> collection = null; 
		
	public MongoUtil(String mongoDatabase, int port, String db, String collection, InOutError error)
	{
		if(client == null)
		{
			client = new MongoClient(mongoDatabase, port);
		}
		
		this.db = client.getDatabase(db);
		this.collection = this.db.getCollection(collection);
	}

	public Boolean delete(Bson data, InOutError error) 
	{
		try
		{
			collection.findOneAndDelete(data);
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : %s", CLASS_NAME,"Failed to delete document."),ex);
			return(Boolean.FALSE);
		}
	}

	public Boolean insert(Document data, InOutError error)
	{
		try
		{
			collection.insertOne(data);
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while inserting document.",CLASS_NAME),ex);
			return(Boolean.FALSE);
		}
	}
	
	public Boolean insertMany(List<Document> documents, InOutError error)
	{
		try
		{
			collection.insertMany(documents);
			return(Boolean.TRUE);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Failed to insert multiple documents.",CLASS_NAME),ex);
			return(Boolean.FALSE);
		}
	}
	
	public FindIterable<Document> search(String column, String value, String sortColumn, Boolean ascending, int limit, InOutError error)
	{
		try
		{
			Bson query = Filters.eq(column, value);
			Bson sort = null;
			if(sortColumn != null)
			{
				sort = (ascending)?Sorts.ascending(sortColumn):Sorts.descending(sortColumn);
			}
			FindIterable<Document> results = (sort!=null)?collection.find(query).sort(sort).limit(limit):collection.find(query).limit(limit);
			return(results);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Error while searching document.", CLASS_NAME),ex); 
			return(null);
		}
	}
	
	public FindIterable<Document> search(String sortColumn, Boolean ascending, int limit, InOutError error)
	{
		try
		{
			Bson sort = null;
			if(sortColumn != null)
			{
				sort = (ascending)?Sorts.ascending(sortColumn):Sorts.descending(sortColumn);
			}
			FindIterable<Document> results = (sort!=null)?collection.find().sort(sort).limit(limit):collection.find().limit(limit);
			return(results);
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Error while searching document.", CLASS_NAME),ex); 
			return(null);
		}
	}
	
	public long update(ObjectId id, String columnName, String value, InOutError error)
	{
		try
		{
			Bson query = Filters.eq(id);			
			UpdateResult result = collection.updateOne(query,new BasicDBObject("$set",new BasicDBObject(columnName, value)));
			return(result.getModifiedCount());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in updating mongo db.", CLASS_NAME), ex);
			return(0);
		}
	}
	
	
	public long update(Document d, InOutError error)
	{
		try
		{
			Bson query = Filters.eq(d.getObjectId("_id"));
			d.remove("_id");
			
			UpdateResult result = collection.updateOne(query,new BasicDBObject("$set",d));
			return(result.getModifiedCount());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in updating document to mongo db.", CLASS_NAME), ex);
			return(0);
		}
	}

}
