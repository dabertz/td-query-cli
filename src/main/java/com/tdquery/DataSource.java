package com.tdquery;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.tdquery.exception.QueryProcessingException;
import com.treasuredata.client.ExponentialBackOff;
import com.treasuredata.client.TDClient;
import com.treasuredata.client.model.TDColumn;
import com.treasuredata.client.model.TDJob;
import com.treasuredata.client.model.TDJobRequest;
import com.treasuredata.client.model.TDJobSummary;
import com.treasuredata.client.model.TDResultFormat;
import com.treasuredata.client.model.TDTable;

/**
 * Data Source connector to establish Treasure Data client connection and submit query/job request.
 *
 */
public class DataSource {

	public String dbname;
	public String engine;

	private TDClient client;

	public DataSource(String dbname) {
		this.dbname = dbname;
		this.client = TDClient.newClient();
	}

	public DataSource(String dbname, String apiKey) {
		this.dbname = dbname;
		if (apiKey != null) {
			this.client = TDClient.newBuilder().setApiKey(apiKey).build();
		} else {
			this.client = TDClient.newClient();
		}
	}

	public boolean testDb() {
		return this.client.existsDatabase(this.dbname);
	}

	public boolean existsDatabase(String databaseName) {
		return this.client.existsDatabase(databaseName);
	}

	public boolean existsTablename(String databaseName, String tablename) {
		return this.client.existsTable(databaseName, tablename);
	}
	
	public boolean existsTablename(String tablename) {
		return this.client.existsTable(this.dbname, tablename);
	}

	public void closeClientConnection() {
		this.client.close();
	}
	
	public List<String> getTableColumnNames(String tablename) {
		List<String> columnNames = new ArrayList<String>();
		TDTable table = this.client.showTable(this.dbname, tablename);
		List<TDColumn> columns = table.getColumns();		
		for(TDColumn column: columns) {
			columnNames.add(column.getName().toLowerCase());
		}
		return columnNames;
	}

	/**
	 * 
	 * Execute the SQL Statement on Treasure data
	 * 
	 * @param engine	Query Engine. presto | hive
	 * @param query		Statement
	 * @return			Return ResultSet
	 * @throws Exception
	 */
	public ResultSet executeQuery(String engine, QueryBuilder query) throws Exception {

		if (!this.testDb()) {
			throw new QueryProcessingException(String.format("Database [%s] not exists", this.dbname));
		}

		List<String> unknownColumns = new ArrayList<String>();
		List<String> availableColumns = this.getTableColumnNames(query.getTablename());
		for(String column: query.getColumnNames()) {
			if(!availableColumns.contains(column.toLowerCase()) && !column.equalsIgnoreCase("time")) {
				unknownColumns.add(column);
			}
		}
		if(unknownColumns.size() > 0) {
			throw new QueryProcessingException(String.format("Unknown columns[%s]. Available columns[%s]", 
					String.join(",",unknownColumns), String.join(",",availableColumns)));
		}

		TDJobRequest jobRequest = null;
		if(engine.equals("presto")) {
			jobRequest = TDJobRequest.newPrestoQuery(this.dbname, query.toString());
		} else if (engine.equals("hive")) {
			jobRequest = TDJobRequest.newHiveQuery(this.dbname, query.toString());
		} else {
			throw new QueryProcessingException(String.format("Unknown engine [%s]", engine));
		}

		String jobId = client.submit(jobRequest);

		// Wait until the query finishes
		 ExponentialBackOff backOff = new ExponentialBackOff();
		 TDJobSummary job = client.jobStatus(jobId);
         while (!job.getStatus().isFinished()) {
             Thread.sleep(backOff.nextWaitTimeMillis());
             job = client.jobStatus(jobId);
         }

         // Read the detailed job information
         TDJob jobInfo = client.jobInfo(jobId);
         if(jobInfo.getStatus() == TDJob.Status.ERROR) {
        	 throw new QueryProcessingException(String.format("Error Processing job request [%s]", jobInfo.getStdErr()));
         }

         JSONArray schemaJSONArray = new JSONArray();
         
         Optional<String> schema = jobInfo.getResultSchema();
         if(schema.isPresent()) {
        	 schemaJSONArray = new JSONArray(schema.get());
         }

         List<Object[]> items = client.jobResult(jobId, TDResultFormat.MESSAGE_PACK_GZ, new Function<InputStream, List<Object[]>>() 
         {
             public List<Object[]> apply(InputStream input) {
            	 List<Object[]> result = new ArrayList<Object[]>();        
                 try {
					MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new GZIPInputStream(input));
					while(unpacker.hasNext()) {
						ArrayValue array = unpacker.unpackValue().asArrayValue();
						result.add(array.list().toArray(new Object[0]));
					}
					unpacker.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
                return result;
             }
         });

         return new ResultSet(schemaJSONArray, items);
	}
}
