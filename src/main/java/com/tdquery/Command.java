package com.tdquery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public abstract class Command {
	
	private static final String LONG = "--";
	private static final String SHORT = "-";
	static final String NEWLINE = "\n";

	Map<String, FieldOptionType> fieldOptionMap;
	Map<Integer, FieldArgumentType> fieldArgumentMap;

	/**
	 * Construct a Command Instance. Find the fields with Option and Argument annotations. 
	 */
	public Command() {
		Class<?> cls = this.getClass();
		fieldOptionMap = new HashMap<String, FieldOptionType>();
		fieldArgumentMap = new HashMap<Integer, FieldArgumentType>();
		for (Field field: cls.getDeclaredFields()) {
			if (!field.isSynthetic()) {

				Argument argument = field.getAnnotation(Argument.class);
				if(argument != null) {
					int idx = argument.index();
					if(fieldArgumentMap.containsKey(idx)) {
						throw new InvalidCommandException(String.format("Argument index [%d] found in [%s] and [%s] ", idx, fieldArgumentMap.get(idx), field));
					}

					FieldArgumentType fieldArgumentType = new FieldArgumentType(field, argument);
					fieldArgumentMap.put(idx, fieldArgumentType);
					continue;
				}

				Option option = field.getAnnotation(Option.class);
				if(option != null) {
					String[] keys = option.keys();
					FieldOptionType fieldOption = new FieldOptionType(field,option);
					for(String key: keys) {
						if(fieldOptionMap.containsKey(key)) {
							throw new InvalidCommandException(String.format("Option [%s] found in [%s] and [%s] ", key, fieldOptionMap.get(key).field, field));
						}

						fieldOptionMap.put(key, fieldOption);
					}
				}

			}
		}
	}

	/**
	 * Parse the input arguments
	 * 
	 * @param args Input arguments
	 */
	public void parse(String[] args) {

		List<String> params = new ArrayList<String>();
		ListIterator<String> list = Arrays.asList(args).listIterator();
		
		while(list.hasNext()) {
			String arg = list.next();

			if (arg.equals("--help")) {
				displayHelp();
				System.exit(0);
			}

			if (arg.startsWith(LONG) || arg.startsWith(SHORT)) {
				parseOption(arg, list);
			} else {
				params.add(arg);
			}
		}

		parseArguments(params);
		validate();
	}

	protected abstract void validate();
	
	/**
	 * Annotate the {@link CommandInfo} fields with Option
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Argument {
		int index();
		String description();
		boolean required() default false;
	}

	/**
	 * Annotate the {@link }
	 * 
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Option {
		String[] keys();
		String description();
		String[] options() default {};
		boolean required() default false;
	}

	/**
	 * Annotate a class as CommandInfo to use to display the command information
	 * 
	 */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface CommandInfo {
		String name();
		String description();
	}
	
	private void parseOption(String key, ListIterator<String> list) {
		FieldOptionType fieldOption = fieldOptionMap.get(key);
		if(fieldOption == null) {
			throw new InvalidCommandException(String.format("Unknow option [%s]", key));
		}
		
		if(!list.hasNext()) {
			throw new InvalidCommandException(String.format("Value must be defined for option [%s]", key));
		}

		Field field = fieldOption.field;
		field.setAccessible(true);
		Class<?> fieldType = field.getType();

		Option option = fieldOption.option;
		String rawValue = list.next();

		String[] options = option.options();
		if(options.length > 0) {
			if (!Arrays.asList(options).contains(rawValue)) {
				throw new InvalidCommandException(String.format("Invalid value for option [%s]. Posible options [%s]", key, String.join(",", options)));
			}
		}

		Object value;
		try {			
			value = parseValue(fieldType, rawValue);
		} catch (Exception e) {
			throw new InvalidCommandException(String.format("Error parsing value for option [%s]", field.getName()));
		}

		try {
			field.set(this, value);
		} catch (IllegalArgumentException e) {
			throw new InvalidCommandException(String.format("Error setting the value for option [%s]", field.getName()), e);
		} catch (IllegalAccessException e) {
			throw new InvalidCommandException(String.format("Error setting value for option [%s]", field.getName()), e);
		}
		
	}

	private void parseArguments(List<String> params) {
		for(Map.Entry<Integer, FieldArgumentType> entry: fieldArgumentMap.entrySet()) {
			String rawValue = null;
			try {
				rawValue = params.get(entry.getKey());
			} catch(IndexOutOfBoundsException e) {}

			FieldArgumentType fieldArgument = entry.getValue();
			Field field = fieldArgument.field;
			field.setAccessible(true);
			Argument annotation = fieldArgument.argument;
			Class<?> type = field.getType();
			if(annotation.required() && rawValue == null) {
				throw new InvalidCommandException(String.format("Argument [%s] must be define in the command line", field.getName()));	
			}

			if (rawValue != null) {
				Object value;
				try {
					value = parseValue(type, rawValue);
				} catch (Exception e) {
					throw new InvalidCommandException(String.format("Error parsing value for argument [%s]", field.getName()));
				}

				try {
					field.set(this, value);
				} catch (IllegalArgumentException e) {
					throw new InvalidCommandException(String.format("Error setting the value for option [%s]", field.getName()), e);
				} catch (IllegalAccessException e) {
					throw new InvalidCommandException(String.format("Error setting value for option [%s]", field.getName()), e);
				}
			}
		}
	}
	
	private Object parseValue(Class<?> type, String value) {
		if (type == String.class) {
			return value;
		} else if (type == int.class || type == Integer.class) {
			return Integer.decode(value);
		} else if (type == long.class || type == Long.class) {
			return Long.decode(value);
		}
		return value;
	}
	
	/**
	 * Display the command information.
	 */
	public void displayHelp() {
		StringBuilder sb = new StringBuilder();
		
		Class<?> cls = this.getClass();
		
		CommandInfo info = cls.getAnnotation(CommandInfo.class);
		if (info != null) {
			sb.append(info.description());
			sb.append(NEWLINE);
		}

		sb.append("Options:");
		sb.append(NEWLINE);
		List<String> fieldNames = new ArrayList<String>();
		for(Map.Entry<String,FieldOptionType> entry: this.fieldOptionMap.entrySet()) {
			Field field = entry.getValue().field;

			if(fieldNames.contains(field.getName())) {
				continue;
			}

			Option option = entry.getValue().option;
			String keys = String.join("/",option.keys());
			String description = option.description();
			sb.append(String.format("%-20s %s", keys, description));
			sb.append(NEWLINE);

			fieldNames.add(field.getName());
		}

		System.out.print(sb.toString());
	}

}
