package com.tdquery;

public class AppRunner {

	public AppRunner() {
	}

	public static void main(String[] args) {

		QueryCommand command = new QueryCommand();

		try {
			command.parse(args);

			QueryBuilder query = new QueryBuilder();
			query.setColumns(command.getColumns());
			query.setTablename(command.getTableName());
			query.setMinTime(command.getMinTime());
			query.setMaxTime(command.getMaxTime());
			query.setLimit(command.getLimit());

			DataSource dataSource = new DataSource(command.getDatabaseName());
			ResultSet result = dataSource.executeQuery(command.getEngine(), query);

			System.out.println("======== RESULT =========");
			if (result.getItems().size() > 0) {
				OutputGenerator outputParser = new OutputGenerator(result, command.getFormat());
				outputParser.generate(command.getPath());
				System.out.println(String.format("Query: %s", query.toString()));
				System.out.println(String.format("Total Item: %d", result.getItems().size()));
			} else {
				System.out.println("No Results Found");
			}
			System.exit(0);
		} catch(InvalidCommandException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (QueryProcessingException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (OutputProcessingException e) {
			System.out.print(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.out.print(e.getMessage());
			System.exit(1);
		}
	}

}
