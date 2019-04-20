package com.tdquery;

import java.util.List;

import com.treasuredata.client.TDClient;
import com.treasuredata.client.model.TDDatabase;
import com.treasuredata.client.model.TDTable;

public class AppRunner {

	public static void main(String[] args) {

		QueryCommand command = new QueryCommand();

		try {
			command.parse(args);

		} catch(ParseException e) {
			System.out.print(e.getMessage());
			System.exit(1);
		}

		TDClient client = TDClient.newClient();
		

		try {
			List<TDDatabase> databases = client.listDatabases();

            TDDatabase db = databases.get(0);

            System.out.println("database: " + db.getName());

            for (TDTable table : client.listTables(db.getName())) {

                System.out.println(" table: " + table);

            }
		}
		catch (Exception e) {

            e.printStackTrace();

        }

        finally {

            client.close();

        }
	}

}
