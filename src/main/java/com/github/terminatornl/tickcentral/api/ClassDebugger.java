package com.github.terminatornl.tickcentral.api;

import com.github.terminatornl.tickcentral.TickCentral;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClassDebugger {

	public static File DEBUG_FOLDER = new File(TickCentral.CONFIG.FOLDER, "DEBUG");
	static {
		try {
			if(DEBUG_FOLDER.exists()){
				FileUtils.deleteDirectory(DEBUG_FOLDER);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] WriteClass(ClassNode classNode, String transformedName) throws Throwable{
		if(TickCentral.CONFIG.DEBUG) {
			TickCentral.LOGGER.info("Writing class: " + classNode.name + " (" + transformedName + ")");
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		byte[] bytes = writer.toByteArray();

		if(TickCentral.CONFIG.DEBUG){
			File classFile = Paths.get(DEBUG_FOLDER.toString(), transformedName.replaceAll("[\\.\\/]+",".").replace(".", "/")).toFile();

			if(classFile.getParentFile().exists() == false && classFile.getParentFile().mkdirs() == false){
				throw new IOException("Unable to create directory: " + classFile.getParentFile());
			}
			Files.write(classFile.toPath().resolveSibling(classFile.getName() + ".class"), bytes, StandardOpenOption.CREATE);
		}
		return bytes;
	}

}
