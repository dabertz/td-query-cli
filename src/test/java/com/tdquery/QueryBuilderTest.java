package com.tdquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit Test for QueryBuilder class 
 *
 */
public class QueryBuilderTest {

	@Test
	public void selectStatement() {
		HashMap<String, String[]> map = new HashMap<String, String[]>(); 
		map.put("SELECT * FROM my_table", new String[]{"my_database", "my_table"});
		map.put("SELECT * FROM my_table LIMIT 200", new String[]{"my_database", "my_table", "-l", "200"});
		map.put("SELECT col1,col2 FROM my_table", new String[]{"-c", "col1,col2", "my_database", "my_table"});
		map.put("SELECT col1,col2 FROM my_table WHERE time > 1427347140", new String[]{"-c", "col1,col2", "-m","1427347140", "my_database", "my_table"});
		map.put("SELECT col1,col2 FROM my_table WHERE time < 1427347140", new String[]{"-c", "col1,col2", "-M","1427347140", "my_database", "my_table"});
		map.put("SELECT col1,col2 FROM my_table WHERE TD_TIME_RANGE(time, 1427347140, 1427350725)", new String[]{"-c", "col1,col2", "-m","1427347140", "-M","1427350725", "my_database", "my_table"});
		map.put("SELECT col1,col2 FROM my_table WHERE TD_TIME_RANGE(time, 1427347140, 1427350725) LIMIT 100", new String[]{"-c", "col1,col2", "-m","1427347140", "-M","1427350725", "-l", "100", "my_database", "my_table"});
		map.put("SELECT col1,col2 FROM my_table", new String[]{"-c", "col1,col2,,,,,", "my_database", "my_table"});
		map.put("SELECT col1 FROM my_table", new String[]{"-c", "col1,,,", "my_database", "my_table"});

		for(Map.Entry<String,String[]> entry: map.entrySet()) {

			QueryCommand command = null;
			boolean asExpected = false;
			try {
				command = new QueryCommand();
				command.parse(entry.getValue());

				QueryBuilder query = new QueryBuilder();
				query.setColumns(command.getColumns());
				query.setTablename(command.getTableName());
				query.setMinTime(command.getMinTime());
				query.setMaxTime(command.getMaxTime());
				query.setLimit(command.getLimit());
				assertEquals(query.toString(), entry.getKey());
				asExpected = true;
			} catch (Exception e) {}

			assertTrue(asExpected);	
		}
		
	}

}
