package com.tdquery;

import java.lang.reflect.Field;
import com.tdquery.Command.Argument;

public class FieldArgumentType {
	
	Field field;
	Argument argument;
	
	public FieldArgumentType(Field field, Argument argument) {
		this.field = field;
		this.argument = argument;
	}

}
