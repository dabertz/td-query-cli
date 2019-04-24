package com.tdquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tdquery.exception.QueryProcessingException;

/**
 * Unit test for submitting query/request to Treasure Data - Data Source
 * Validate the data to ensure that the given column names and # of records match with the database table. 
 *
 */
public class QueryDataSourceTest {

	private static final String SAMPLE_DB = "test_db";
	private static final String SAMPLE_TABLE = "access_logs";
	private static final String ENGINE = "presto";

	private DataSource dataSource;


	@Before
    public void setUp() throws Exception{
		this.dataSource = new DataSource(SAMPLE_DB);
	}

	@After
	public void tearDown() throws Exception {
		this.dataSource.closeClientConnection();
	}

	@Test
	public void testExistsDatabase() {
		assertTrue(this.dataSource.testDb());
	}

	@Test
	public void testExistsTable() {
		assertTrue(this.dataSource.existsTablename(SAMPLE_TABLE));
	}

	@Test
	public void checkTableColumns() {
		// Get current table columns
		List<String> columns = this.dataSource.getTableColumnNames(SAMPLE_TABLE);

		assertEquals(columns.size(), 2);

		// Check column name matches with the current database.
		String[] columnNames = new String[] {"action", "user"};
		for(int i=0; i < columnNames.length; i++) {
			assertTrue(columns.contains(columnNames[i].toLowerCase()));
		}
	}

	@Test
	public void testQueryResultMatches() throws Exception {

		// Check the query result whether matches with the test database
		
		ResultSet result;
		QueryBuilder query;

		// [Test Case 1] 
		// Query with specified limit
		query = new QueryBuilder();
		query.setTablename(SAMPLE_TABLE);
		query.setLimit(100);
		result = this.dataSource.executeQuery(ENGINE, query);
		assertEquals(100, result.getItems().size());

		// [Test Case 2] 
		// Query with specified min and max time to select all records 
		// whose timestamp is larger than min timestamp and smaller than max timestamp
		query = new QueryBuilder();
		query.setTablename(SAMPLE_TABLE);
		query.setMinTime(1395770792);
		query.setMaxTime(1396029992);
		result = this.dataSource.executeQuery(ENGINE, query);
		assertEquals(297, result.getItems().size());

		// [Test Case 3]
		// Test query with specified minimum time only to select all records
		// whose timestamp is larger than min timestamp
		query = new QueryBuilder();
		query.setTablename(SAMPLE_TABLE);
		query.setMinTime(1396029992);
		result = this.dataSource.executeQuery(ENGINE, query);
		assertEquals(198, result.getItems().size());
		
		// [Test Case 4]
		// Test query with specified maximum time only to select all records
		// whose timestamp is smaller than max timestamp
		query = new QueryBuilder();
		query.setTablename(SAMPLE_TABLE);
		query.setMaxTime(1393697192);
		result = this.dataSource.executeQuery(ENGINE, query);
		assertEquals(102, result.getItems().size());
		
		// [Test Case 5]
		// Test query to check all records with specified valid column names in the test database(Records:3072)
		query = new QueryBuilder();
		query.setColumns("action,user");
		query.setTablename(SAMPLE_TABLE);
		boolean asExpected = false;
		try {
			result = this.dataSource.executeQuery(ENGINE, query);
			assertEquals(3072, result.getItems().size());
			asExpected = true;
		} catch(QueryProcessingException e) {
			asExpected = false;
		}
		assertTrue(asExpected);
		
		// [Test Case 6]
		// Failure test for unknown column names
		query = new QueryBuilder();
		query.setColumns("unknown1, user");
		query.setTablename(SAMPLE_TABLE);
		asExpected = false;
		try {
			result = this.dataSource.executeQuery(ENGINE, query);
		} catch(QueryProcessingException e) {
			asExpected = true; // Should throw an exception
		}
		assertTrue(asExpected);
	}
}
