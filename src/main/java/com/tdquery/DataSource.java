package com.tdquery;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;

import com.google.common.base.Function;
import com.treasuredata.client.ExponentialBackOff;
import com.treasuredata.client.TDClient;
import com.treasuredata.client.model.TDJob;
import com.treasuredata.client.model.TDJobRequest;
import com.treasuredata.client.model.TDJobSummary;
import com.treasuredata.client.model.TDResultFormat;


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
		this.client = TDClient.newBuilder().setApiKey(apiKey).build();
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
	
	public boolean existsTableColumns(String columns) {
		return true;
	}

	public void executeQuery(String engine, String statement) throws Exception {

//		if (!this.testDb()) {
//			throw new Exception(String.format("Database [%s] not exist", this.dbname));
//		}

		TDJobRequest jobRequest = null;
		if(engine.equals("presto")) {
			jobRequest = TDJobRequest.newPrestoQuery(this.dbname, statement);
		} else if (engine.equals("hive")) {
			jobRequest = TDJobRequest.newHiveQuery(this.dbname, statement);
		} else {
			throw new Exception(String.format("Unknown engine [%s]", engine));
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
         System.out.println("log:\n" + jobInfo.getCmdOut());
         System.out.println("error log:\n" + jobInfo.getStdErr());
         System.out.println("Result log:\n" + jobInfo.getResult());
         com.google.common.base.Optional<String> schema = jobInfo.getResultSchema();
         System.out.println(jobInfo.getResult());
 

         List<ArrayValue> result = client.jobResult(jobId, TDResultFormat.MESSAGE_PACK_GZ, new Function<InputStream, List<ArrayValue>>() 
         {
             public List<ArrayValue> apply(InputStream input) {
            	 List<ArrayValue> result = new ArrayList<ArrayValue>();        
                 try {
					MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new GZIPInputStream(input));
					while(unpacker.hasNext()) {
						ArrayValue array = unpacker.unpackValue().asArrayValue();
						result.add(array);
					}
					unpacker.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
                return result;
             }
         });

         System.out.println(result);
	}

}
