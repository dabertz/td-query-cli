package com.tdquery;

import java.util.Objects;

import com.tdquery.Command.CommandInfo;

@CommandInfo(name = "Query", description = "CLI tool to issue a query on Treasure Data") 
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
	
	@Option(keys= {"-e", "--engine"}, description = "is optional and specifies the maximum timestamp: NULL by default")
	private String engine = "presto";

	@Option(keys= {"-f", "--format"}, description = "is optional and specifies the output format: tabular by default")
	private String format = "tabular";

	@Option(keys= {"-l", "--limit"}, description = "is optional and specifies the output format: tabular by default")
	private int limit;
	
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

	@Override
	protected void validate() {
		if (!Objects.isNull(this.minTime) && !Objects.isNull(this.maxTime)) {
			if (this.minTime > this.maxTime) {
				throw new ParseException("max time must be greater than min time");
			}
		}
	}
}
