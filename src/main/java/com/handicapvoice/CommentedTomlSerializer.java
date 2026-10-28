package com.handicapvoice;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Writes the config as TOML with every {@link Comment} above its value, and reads it back key by key
 * so a missing key keeps its own default instead of resetting the whole section.
 * Supports one level of sections (public object fields) holding boolean, int, long, float, double and String values.
 */
public class CommentedTomlSerializer<T extends ConfigData> implements ConfigSerializer<T> {
	private final Class<T> configClass;
	private final Path file;

	public CommentedTomlSerializer(Config definition, Class<T> configClass) {
		this.configClass = configClass;
		this.file = FabricLoader.getInstance().getConfigDir().resolve(definition.name() + ".toml");
	}

	@Override
	public void serialize(T config) throws SerializationException {
		StringBuilder out = new StringBuilder();
		List<Field> sections = new ArrayList<>();
		try {
			for (Field field : fields(configClass)) {
				if (isValue(field.getType())) {
					writeValue(out, field, field.get(config));
				} else {
					sections.add(field);
				}
			}
			for (Field section : sections) {
				Object value = section.get(config);
				if (value == null) {
					continue;
				}
				if (!out.isEmpty()) {
					out.append('\n');
				}
				writeComment(out, section);
				out.append('[').append(section.getName()).append("]\n");
				for (Field field : fields(section.getType())) {
					writeValue(out, field, field.get(value));
				}
			}
			Files.createDirectories(file.getParent());
			Files.writeString(file, out);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public T deserialize() throws SerializationException {
		if (!Files.exists(file)) {
			return createDefault();
		}
		try {
			Toml toml = new Toml().read(file.toFile());
			T config = createDefault();
			for (Field field : fields(configClass)) {
				if (isValue(field.getType())) {
					readValue(toml, field, config);
				} else {
					Toml table = toml.getTable(field.getName());
					Object section = field.get(config);
					if (table != null && section != null) {
						for (Field child : fields(field.getType())) {
							readValue(table, child, section);
						}
					}
				}
			}
			return config;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public T createDefault() {
		return Utils.constructUnsafely(configClass);
	}

	private static List<Field> fields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
				fields.add(field);
			}
		}
		return fields;
	}

	private static boolean isValue(Class<?> type) {
		return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class;
	}

	private static void writeComment(StringBuilder out, Field field) {
		Comment comment = field.getAnnotation(Comment.class);
		if (comment != null) {
			for (String line : comment.value().split("\n")) {
				out.append("# ").append(line.strip()).append('\n');
			}
		}
	}

	private static void writeValue(StringBuilder out, Field field, Object value) {
		writeComment(out, field);
		out.append(field.getName()).append(" = ");
		if (value instanceof String s) {
			out.append('"').append(s.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
		} else {
			out.append(value);
		}
		out.append('\n');
	}

	/** Leaves the default in place when the key is missing or has the wrong type. */
	private static void readValue(Toml toml, Field field, Object target) throws IllegalAccessException {
		String key = field.getName();
		if (!toml.contains(key)) {
			return;
		}
		Class<?> type = field.getType();
		try {
			if (type == boolean.class || type == Boolean.class) {
				field.set(target, toml.getBoolean(key));
			} else if (type == int.class || type == Integer.class) {
				field.set(target, toml.getLong(key).intValue());
			} else if (type == long.class || type == Long.class) {
				field.set(target, toml.getLong(key));
			} else if (type == float.class || type == Float.class) {
				field.set(target, number(toml, key).floatValue());
			} else if (type == double.class || type == Double.class) {
				field.set(target, number(toml, key).doubleValue());
			} else if (type == String.class) {
				field.set(target, toml.getString(key));
			}
		} catch (ClassCastException e) {
			HandicapVoice.LOGGER.warn("Config value {} has the wrong type, using the default", key);
		}
	}

	/** TOML keeps 1 and 1.0 apart, accept both for decimal values. */
	private static Number number(Toml toml, String key) {
		Object value = toml.toMap().get(key);
		return (Number) value;
	}
}
