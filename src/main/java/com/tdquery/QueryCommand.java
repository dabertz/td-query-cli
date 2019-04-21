package com.tdquery;

import com.tdquery.Command.CommandInfo;

@CommandInfo(name = "Query", description = "CLI tool to issue a query on Treasure Data\n") 
public class QueryCommand extends Command{

	@Argument(index=0, description = "Database name", required = true)
	private String databasename = null;

	@Argument(index=1, description = "Table name", required = true)
	private String tablename = null;

	@Option(keys= {"-c", "--column"}, description = "is optional and specifies the comma separated list of columns to restrict the\n" + 
			"result to. Read all columns if not specified.")
	private String columns = null;
	
	@Option(keys= {"-m", "--min"}, description = "is optional and specifies the minimum timestamp: NULL by default")
	private long minTime;
	
	@Option(keys= {"-M", "--MAX"}, description = "is optional and specifies the minimum timestamp: NULL by default")
	private long maxTime;
	
	@Option(keys= {"-e", "--engine"}, description = "is optional and specifies the query engine: ‘presto’ by default", options = {"hive","presto"})
	private String engine = "presto";

	@Option(keys= {"-f", "--format"}, description = "is optional and specifies the output format: tabular by default", options = {"csv", "tabular"})
	private String format = "tabular";

	@Option(keys= {"-l", "--limit"}, description = "specifies the limit of records returned. Read all records if not specified.")
	private int limit;

	@Option(keys= {"-d", "--directory"}, description = "is optional and specifies the output directory: by default")
	private String path = null;
	
	@Option(keys= {"-k", "--key"}, description = "is optional and specifies the output directory: by default")
	private String apiKey = null;

	public String getDatabaseName() {
		return this.databasename; 
	}
	
	public String getTableName() {
		return this.tablename; 
	}

	public String getColumns() {
		return this.columns; 
	}

	public Long getMinTime() {
		return this.minTime; 
	}

	public Long getMaxTime() {
		return this.maxTime; 
	}
	
	public int getLimit() {
		return this.limit;
	}

	public String getEngine() {
		return this.engine;
	}
	
	public String getFormat() {
		return this.format;
	}

	public String getPath() {
		return this.format;
	}
	
	public String getApiKey() {
		return this.apiKey;
	}

	@Override
	protected void validate() {
		if (this.minTime > 0 && this.maxTime > 0) {
			if (this.minTime > this.maxTime) {
				throw new InvalidCommandException("max time must be greater than min time");
			}
		}
	}
}
