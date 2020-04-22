package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Config {

	public boolean DEBUG = false;
	public boolean SHOW_FORCED_LOADING = false;
	public int FILE_COUNT = 0;

	public File FOLDER = new File("config", TickCentral.NAME);
	public File CONFIG_FILE = new File(FOLDER, "config.cfg");

	public Config() {
		try {
			if (FOLDER.exists() == false && FOLDER.mkdirs() == false) {
				throw new IOException("Unable to create directory: " + FOLDER);
			}
			if (CONFIG_FILE.exists() == false) {
				//noinspection ConstantConditions
				Files.write(CONFIG_FILE.toPath(), IOUtils.toByteArray(Config.class.getClassLoader().getResourceAsStream("assets/tickcentral/default_config.txt")));
			}
			for (String line : Files.readAllLines(CONFIG_FILE.toPath())) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				String[] split = line.split(" ?= ?", 2);
				if (split.length == 2) {
					String subject = split[0].trim();
					String value = split[1].trim();

					if (subject.equalsIgnoreCase("SHOW_FORCED_LOADING")) {
						SHOW_FORCED_LOADING = Boolean.parseBoolean(value);
					}
					if (subject.equalsIgnoreCase("DEBUG")) {
						DEBUG = Boolean.parseBoolean(value);
					}
					if (subject.equalsIgnoreCase("FILE_COUNT")) {
						FILE_COUNT = Integer.parseInt(value);
					}
				}

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save() throws IOException {
		HashMap<String, String> CURRENTS = new HashMap<>();
		CURRENTS.put("SHOW_FORCED_LOADING", String.valueOf(SHOW_FORCED_LOADING));
		CURRENTS.put("DEBUG", String.valueOf(DEBUG));
		CURRENTS.put("FILE_COUNT", String.valueOf(FILE_COUNT));

		List<String> builder = new LinkedList<>();
		for (String line : Files.readAllLines(CONFIG_FILE.toPath())) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				builder.add(line);
				continue;
			}

			String[] split = line.split(" ?= ?", 2);
			if (split.length == 2) {
				String subject = split[0].trim().toUpperCase();
				String value = CURRENTS.get(subject);
				if (value != null) {
					builder.add(subject + " = " + value);
					continue;
				}
				builder.add("# Unknown option: " + line);
			}
		}
		for (Map.Entry<String, String> entry : CURRENTS.entrySet()) {
			builder.add(entry.getKey() + " = " + entry.getValue());
		}
		Files.write(CONFIG_FILE.toPath(), builder, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
