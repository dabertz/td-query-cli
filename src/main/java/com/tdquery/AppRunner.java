package com.tdquery;

import com.tdquery.exception.CommandException;
import com.tdquery.exception.OutputProcessingException;
import com.tdquery.exception.QueryProcessingException;

/**
 * Main class to run the CLI Application
 *
 */
public class AppRunner {

	public AppRunner() {
	}

	public static void main(String[] args) {

		QueryCommand command = null;

		try {
			command = new QueryCommand();
			command.parse(args);
		} catch(CommandException e) {
			System.out.print("ERROR: ");
			System.out.println(e.getMessage());
			command.displayHelp();
			System.exit(1);
		}

		DataSource dataSource = null;
		int status = 0;
		try {
			QueryBuilder query = new QueryBuilder();
			query.setColumns(command.getColumns());
			query.setTablename(command.getTableName());
			query.setMinTime(command.getMinTime());
			query.setMaxTime(command.getMaxTime());
			query.setLimit(command.getLimit());

			dataSource = new DataSource(command.getDatabaseName(), command.getApiKey());
			ResultSet result = dataSource.executeQuery(command.getEngine(), query);

			System.out.println("-------------------------------");
			System.out.println("            RESULT             ");
			System.out.println("-------------------------------");
			if (result.getItems().size() > 0) {
				OutputGenerator outputParser = new OutputGenerator(result, command.getFormat());
				String outputFilePath = outputParser.generate(command.getPath());
				System.out.println(String.format("Query       :%s", query.toString()));
				System.out.println(String.format("Records     :%d", result.getItems().size()));
				System.out.println(String.format("Output file : %s", outputFilePath));
			} else {
				System.out.println("No Results Found");
			}
		} catch (Exception e) {
			System.out.print("ERROR: ");
			System.out.println(e.getMessage());
			status = 1;
		} finally {
			dataSource.closeClientConnection();
		}

		System.exit(status);
	}

}
