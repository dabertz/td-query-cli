package com.tdquery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.msgpack.value.ArrayValue;

import com.tdquery.exception.OutputProcessingException;

import dnl.utils.text.table.TextTable;

public class OutputGenerator {
	
	private String format;
	private ResultSet result;
	private String fileExtension;

	public OutputGenerator(ResultSet result, String format) {
		this.result = result;
		this.format = format;
	}

	/**
	 * Generate output file
	 * 
	 * @param path	Directory where to save the output file. By default, in will saved in user HOME directory. 
	 * @throws OutputProcessingException
	 * 
	 * @return String The absolute path of generated file
	 */
	public String generate(String path) throws OutputProcessingException {

			if(format.equals("csv")) {
				this.fileExtension = "csv";
			} else if(format.equals("tabular")) {
				this.fileExtension = "txt";
			} else {
				throw new OutputProcessingException("Unknow file format");
			}

			List<String> columns = new ArrayList<String>();
			JSONArray schema = result.getSchema();
			for(int i=0; i < schema.length(); i ++) {
				JSONArray col;
				try {
					col = new JSONArray(schema.get(i).toString());
					columns.add(col.get(0).toString());
				} catch (JSONException e) {
					new OutputProcessingException("Error parsing columns to JSON", e);
				}
			}

			List<Object[]> items = this.result.getItems();
			Object[][] x = items.toArray(new Object[0][0]);

			String[] columnNames = columns.toArray(new String[0]);
			TextTable textTable = new TextTable(columnNames, x);		

			File outputDir;			
			if (path != null && !path.isEmpty()) {
				outputDir = new File(path);
			} else {
				try {
					outputDir = new File(SystemUtils.getUserHome().getCanonicalPath() + File.separator + "tdquery");
				} catch (IOException e) {
					throw new OutputProcessingException("Unable to read output directory");
				}
			}

			if(!outputDir.exists()) {
				if(!outputDir.mkdirs()) {
					try {
						throw new OutputProcessingException(String.format("Unable to create output directory [%s]", outputDir.getCanonicalPath()));
					} catch (IOException e) {
						throw new OutputProcessingException("Unable to read output directory");
					}
				}
			}

			try {
				String fileName =  "query-result." + this.fileExtension;
				File file = new File(outputDir.getCanonicalPath() + File.separator + fileName);
				FileOutputStream fop = new FileOutputStream(file);
				PrintStream ps = new PrintStream(fop);
				if(format.equals("csv")) {
					textTable.toCsv(ps);	
				} else if(format.equals("tabular")) {
					textTable.printTable(ps, 0);
				}
				fop.close();
				return file.getAbsolutePath();
			} catch (IOException e) {
				throw new OutputProcessingException("Unable to create out file", e);
			}
	}	
}
