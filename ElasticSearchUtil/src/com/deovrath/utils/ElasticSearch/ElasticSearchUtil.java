package com.deovrath.utils.ElasticSearch;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.deovrath.utils.Logger.InOutError;

public class ElasticSearchUtil 
{
	private final class Config
	{
		public static final String ELASTIC_SEARCH_SERVER = "ulvmctmfil200.internal.colt.net";
		public static final int ELASTIC_SEARCH_SERVER_PORT = 9300;
	}
	
	private static final String CLASS_NAME = ElasticSearchUtil.class.getSimpleName();
	private static final TransportClient client = new PreBuiltTransportClient(Settings.EMPTY);
	
	static
	{
		try 
		{
			client.addTransportAddress(new TransportAddress(InetAddress.getByName(Config.ELASTIC_SEARCH_SERVER), Config.ELASTIC_SEARCH_SERVER_PORT));
		} 
		catch (Exception ex) 
		{
			InOutError.log_error(String.format("%s : Failed to init ElasticSearch server.", CLASS_NAME),ex);
		}
	}
	
	public Boolean delete_index(String index_name, InOutError error)
	{
		try
		{
			DeleteIndexRequest request = new DeleteIndexRequest(index_name);
			DeleteIndexResponse response = client.admin().indices().delete(request).get();
			return(response.isAcknowledged());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception while deleting index from ElasticSearch.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	public Boolean create_index(String index_name, InOutError error)
	{
		try
		{
			CreateIndexRequest request = new CreateIndexRequest(index_name);
			CreateIndexResponse response = client.admin().indices().create(request).get();
			return(response.isAcknowledged());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in creating ElasticSearch index.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	public Boolean create_index_mapping(String index_name, String type_name, XContentBuilder mappings, InOutError error)
	{
		try
		{
			PutMappingRequest request = new PutMappingRequest(index_name);
			request.type(type_name);
			request.source(mappings);
			PutMappingResponse response = client.admin().indices().putMapping(request).get();
			return(response.isAcknowledged());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in creating ElasticSearch index mapping.", CLASS_NAME), ex);
			return(Boolean.FALSE);
		}
	}
	
	public int bulk_insert(String index_name, String type_name, List<XContentBuilder> records, InOutError error)
	{
		try
		{		
			BulkRequest request = new BulkRequest();
			
			Iterator<XContentBuilder> itr = records.iterator();
			while(itr.hasNext())
			{
				IndexRequest ir = new IndexRequest(index_name, type_name);
				ir.source(itr.next());
				request.add(ir);
			}
			
			BulkResponse response = client.bulk(request).get();
			return(response.status().getStatus());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in performing bulk insert on ElasticSearch.", CLASS_NAME), ex);
			return(-1);
		}
	}
	
	public int insert(String index_name, String type_name, XContentBuilder record, InOutError error)
	{
		try
		{
			IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index_name,type_name);
			indexRequestBuilder.setSource(record);
			IndexResponse response = indexRequestBuilder.get();
			return(response.status().getStatus());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in performing insert on ElasticSearch.", CLASS_NAME), ex);
			return(-1);
		}
	}
	
	public List<Map<String,Object>> search_records(String index_name, String type_name, QueryBuilder builder, String[] includeFields, String[] excludeFields, String sortField, Boolean isDescending, int noOfRecords, InOutError error)
	{
		List<Map<String,Object>> returnData = new ArrayList<Map<String,Object>>();
		try
		{
			SearchRequest request = new SearchRequest(index_name);
			request.types(type_name);
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(builder);
			sourceBuilder.fetchSource(includeFields,excludeFields);
			sourceBuilder.size(noOfRecords);
			
			if(sortField != null)
			{
				if(isDescending)
				{
					sourceBuilder.sort(new FieldSortBuilder(sortField).order(SortOrder.DESC));
				}
				else
				{
					sourceBuilder.sort(new FieldSortBuilder(sortField).order(SortOrder.ASC));
				}
			}
			
			request.source(sourceBuilder);	
			SearchResponse response = client.search(request).get();
			
			SearchHits searchHits = response.getHits();
			for(SearchHit hit : searchHits)
			{
				Map<String,Object> sourceAsMap = hit.getSourceAsMap();
				if(sourceAsMap != null)
				{
					returnData.add(sourceAsMap);
				}
			}
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in searching records on ElasticSearch.", CLASS_NAME), ex);
		}
		
		return(returnData);
	}
	
	public int delete_record(String index_name, String type_name, String document_id, InOutError error)
	{
		try
		{
			DeleteRequest request = new DeleteRequest(index_name, type_name, document_id);
			DeleteResponse response = client.delete(request).get();
			return(response.status().getStatus());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in deleting single record from ElasticSearch.", CLASS_NAME), ex);
			return(-1);
		}
	}
	
	public int bulk_delete(String index_name, String type_name, List<String> records, InOutError error)
	{
		try
		{
			BulkRequest request = new BulkRequest();
			
			Iterator<String> itr = records.iterator();
			while(itr.hasNext())
			{
				DeleteRequest dr = new DeleteRequest(index_name, type_name, itr.next());
				request.add(dr);
			}
			
			BulkResponse response = client.bulk(request).get();
			return(response.status().getStatus());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in deleting bulk records from ElasticSearch.", CLASS_NAME), ex);
			return(-1);
		}
	}
	
	public long search_and_delete(String index_name, String type_name, QueryBuilder builder, InOutError error)
	{
		try
		{
			BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
					.filter(builder)
					.source(index_name)
					.get();
			
			return(response.getDeleted());
		}
		catch(Exception ex)
		{
			error.setError(String.format("%s : Exception in searching and deleting records from ElasticSearch.", CLASS_NAME), ex);
			return(-1);
		}
	}
}
