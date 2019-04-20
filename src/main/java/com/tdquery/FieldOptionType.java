package com.tdquery;

import java.lang.reflect.Field;
import com.tdquery.Command.Option;

public class FieldOptionType {
	Field field;
	Option option;

	public FieldOptionType(Field field, Option option) {
		this.field = field;
		this.option = option;
	}

	public String help() {
		String description = option.description();
		String[] keys = option.keys();
		return String.format("%s %s", String.join("/", keys), description);
	}
}
