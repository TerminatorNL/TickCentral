package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class Config {

	public boolean DEBUG = false;
	public boolean SHOW_FORCED_LOADING = false;

	public File FOLDER = new File("config", TickCentral.NAME);
	public File CONFIG_FILE = new File(FOLDER, "config.cfg");

	public Config(){
		try {
			if(FOLDER.exists() == false && FOLDER.mkdirs() == false){
				throw new IOException("Unable to create directory: " + FOLDER);
			}
			if(CONFIG_FILE.exists() == false){
				//noinspection ConstantConditions
				Files.write(CONFIG_FILE.toPath(), IOUtils.toByteArray(Config.class.getClassLoader().getResourceAsStream("assets/tickcentral/default_config.txt")));
			}
			for (String line : Files.readAllLines(CONFIG_FILE.toPath())) {
				line = line.trim();
				if(line.isEmpty() || line.startsWith("#")){
					continue;
				}
				String[] split = line.split(" ?= ?",2);
				if(split.length == 2){
					String subject = split[0].trim();
					String value = split[1].trim();

					if(subject.equalsIgnoreCase("SHOW_FORCED_LOADING")){
						SHOW_FORCED_LOADING = Boolean.parseBoolean(value);
					}
					if(subject.equalsIgnoreCase("DEBUG")){
						DEBUG = Boolean.parseBoolean(value);
					}
				}

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
