package com.tdquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.tdquery.exception.CommandException;
import com.tdquery.exception.InvalidValueSectionException;
import com.tdquery.exception.MinimumUnixTimestampException;
import com.tdquery.exception.ParseOptionValueException;
import com.tdquery.exception.RequiredArgumentException;

/**
 * Unit test for QueryCommand class
 * Validate the different inputs and values of the command options 
 *
 */
public class QueryCommandTest {
	
	@Test
	public void testRequiredFieldException() {
		// Failure test 1
		String[] args = new String[]{"-f", "csv"};
		try {
			QueryCommand command = new QueryCommand();
			command.parse(args);
			assertEquals(command.getDatabaseName(), null);
		} catch (Exception e) {
			assertEquals(RequiredArgumentException.class, e.getClass());
		}

		// Failure test 2. <tablename> not defined
		args = new String[]{"-f", "csv", "dbname"};
		try {
			QueryCommand command = new QueryCommand();
			command.parse(args);
			assertEquals(command.getTableName(), null);
		} catch (CommandException e) {
			assertEquals(RequiredArgumentException.class, e.getClass());
			assertEquals("tablename",e.fieldName);
		}

		// Success test
		args = new String[]{"test_db", "test_table"};
		QueryCommand command = null;
		boolean asExpected = false;
		try {
			command = new QueryCommand();
			command.parse(args);
			assertEquals(command.getDatabaseName(), "test_db");
			assertEquals(command.getTableName(), "test_table");
			asExpected = true;
		} catch (Exception e) {
			assertEquals(RequiredArgumentException.class, e.getClass());
		}
		assertTrue(asExpected);
	}

	@Test
	public void testAllInputCommand() {
		// Test all options

		QueryCommand command = null;

		String[][] keys = {
				{ 
					"-f", "csv",
					"-e", "hive", 
					"-c",  "my_col1,my_col2,my_col5", 
					"-m", "1427347140",
					"-M", "1427350725", 
					"-l", "100",
					"my_db",
					"my_table"
				},
				{ 
					"--format", "csv",
					"--engine", "hive", 
					"--column",  "my_col1,my_col2,my_col5", 
					"--min", "1427347140",
					"--MAX", "1427350725", 
					"--limit", "100",
					"my_db",
					"my_table"
				},
		};

		for(int i=0; i < keys.length; i++) {
			
			try {
				command = new QueryCommand();
				command.parse(keys[i]);
			} catch (Exception e) {
			}

			assertEquals(command.getFormat(), "csv");
			assertEquals(command.getEngine(), "hive");
			assertEquals(command.getColumns(), "my_col1,my_col2,my_col5");
			assertEquals(command.getMinTime(), 1427347140);
			assertEquals(command.getMaxTime(), 1427350725);
			assertEquals(command.getLimit(), 100);
			assertEquals(command.getDatabaseName(), "my_db");
			assertEquals(command.getTableName(), "my_table");
		}

		// Test default value of optional fields
		String[] args = new String[]{
				"my_db",
				"my_table"};

		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
		}

		assertEquals(command.getFormat(), "tabular");
		assertEquals(command.getEngine(), "presto");
		assertEquals(command.getColumns(), null);
		assertEquals(command.getMinTime(), 0);
		assertEquals(command.getMaxTime(), 0);
		assertEquals(command.getLimit(), 0);
	}

	@Test
	public void testValidMinUnixTimeStamp() {
		// Test time stamp of min time
		String[] args = new String[]{ 
				"-m", "1000",
				"-l", "100",
				"my_db",
				"my_table"};
		
		boolean asExpected = false;
		QueryCommand command = null;
		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
			assertEquals(MinimumUnixTimestampException.class, e.getClass());
			asExpected = true;
		}
		assertTrue(asExpected);

		args = new String[]{ 
				"-M", "1000", 
				"-l", "100",
				"my_db",
				"my_table"};

		asExpected = false;
		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
			assertEquals(MinimumUnixTimestampException.class, e.getClass());
			asExpected = true;
		}

		// Test time stamp of max time
		assertTrue(asExpected);
	}

	@Test
	public void testInvalidOptionValueType() {
		// Test value for invalid min time
		String[] args = new String[]{ 
				"-m", "10000timestamp",
				"my_db",
				"my_table"};
		boolean invalidMinTime = false;
		QueryCommand command = null;
		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
			assertEquals(ParseOptionValueException.class, e.getClass());
			invalidMinTime = true;
		}
		assertTrue(invalidMinTime);

		
		// Test value for invalid min time
		args = new String[]{ 
				"-M", "10000timestamp",
				"my_db",
				"my_table"};
		boolean invalidMaxTime = false;
		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
			assertEquals(ParseOptionValueException.class, e.getClass());
			invalidMaxTime = true;
		}
		assertTrue(invalidMaxTime);
		
		// Test value for invalid limit value
		args = new String[]{ 
				"-l", "100number",
				"my_db",
				"my_table"};
		boolean invalidLimit = false;
		try {
			command = new QueryCommand();
			command.parse(args);
		} catch (Exception e) {
			assertEquals(ParseOptionValueException.class, e.getClass());
			invalidLimit = true;
		}
		assertTrue(invalidLimit);
	}

	@Test
	public void testPosibleOptions() {

		String[] validFormats = new String[] {"csv","tabular"};
		for(String format: validFormats) {
			String[] args = new String[]{ 
					"-f", format,
					"my_db",
					"my_table"};

			boolean asExpected = false;
			try {
				QueryCommand command = new QueryCommand();
				command.parse(args);
				assertEquals(command.getFormat(), format);
				asExpected = true;
			} catch (Exception e) {
				assertEquals(InvalidValueSectionException.class, e.getClass());
			}
			assertTrue(asExpected);
		}
		
		String[] validEngines = new String[] {"presto","hive"};
		for(String engine: validEngines) {
			String[] args = new String[]{ 
					"-e", engine,
					"my_db",
					"my_table"};

			boolean asExpected = false;
			try {
				QueryCommand command = new QueryCommand();
				command.parse(args);
				assertEquals(command.getEngine(), engine);
				asExpected = true;
			} catch (Exception e) {
				assertEquals(InvalidValueSectionException.class, e.getClass());
			}
			assertTrue(asExpected);
		}
		
	}
}
