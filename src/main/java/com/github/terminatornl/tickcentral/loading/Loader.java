package com.github.terminatornl.tickcentral.loading;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.TransformerSupplier;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class Loader {

	private final LinkedList<Map.Entry<Integer, TransformerSupplier>> suppliers = new LinkedList<>();
	private final LaunchClassLoader loader;
	private final Side side = FMLLaunchHandler.side();

	public LaunchClassLoader getClassLoader(){
		return loader;
	}

	public Loader(){
		loader = ((LaunchClassLoader) getClass().getClassLoader());
		findTransformersInJarFiles();
		if(System.getProperty("net.minecraftforge.gradle.GradleStart.csvDir") != null){
			findTransformersInDeobfuscatedEnvironment();
		}
		suppliers.sort(Map.Entry.comparingByKey());
		if(suppliers.size() > 0){
			TickCentral.LOGGER.info("Loaded " + suppliers.size() + " addons.");
		}else{
			TickCentral.LOGGER.warn("Warning: No addons detected. " + TickCentral.NAME + " currently serves no purpose!");
		}

	}

	public void distributeCalls() throws Exception{
		for (Map.Entry<Integer, TransformerSupplier> transformer : suppliers) {
			transformer.getValue().call();
		}
	}

	public void distributeInject(Map<String, Object> fmlData){
		for (Map.Entry<Integer, TransformerSupplier> transformer : suppliers) {
			transformer.getValue().injectData(fmlData);
		}
	}

	/**
	 * Returns a new list containing all the classes of class transformers that have to be executed as late as possible
	 * @return a list of classtransformers
	 */
	public LinkedList<Class<? extends IClassTransformer>> getLastClassTransformersTypes(){
		LinkedList<Class<? extends IClassTransformer>> list = new LinkedList<>();
		suppliers.forEach(e -> list.addAll(e.getValue().getLastTransformers()));
		return list;
	}

	/**
	 * Returns a new list containing all classes of class transformers
	 * @return a list of classtransformers
	 */
	public LinkedList<String> getAllClassTransformers(){
		LinkedList<String> list = new LinkedList<>();
		suppliers.forEach(e -> list.addAll(e.getValue().getTransformers()));
		return list;
	}

	private void findTransformersInDeobfuscatedEnvironment(){
		TickCentral.LOGGER.info("We're in a deobfuscated environment! Looking for " + TickCentral.NAME + "-loadable.txt files");

		InputStream stream = getClass().getClassLoader().getResourceAsStream(TickCentral.NAME+"-loadable.txt");
		if(stream != null){
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			try {
				//noinspection ConstantConditions
				parseLoadables(reader, new File(getClass().getClassLoader().getResource(TickCentral.NAME+"-loadable.txt").toURI()));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}else{
			TickCentral.LOGGER.info(TickCentral.NAME+"-loadable.txt was not found!");
		}
	}

	private void findTransformersInJarFiles(){
		try{
			String fullPath = URLDecoder.decode(Loader.class.getProtectionDomain().getCodeSource().getLocation().getFile(), StandardCharsets.UTF_8.name());
			Pattern pattern = Pattern.compile("^(?:.+:)?(.+)!.*$");

			Matcher matcher = pattern.matcher(fullPath);
			final File modsDirectory;
			if(matcher.find() == false){
				TickCentral.LOGGER.info("Unable to perform regex on string: " + fullPath);
				TickCentral.LOGGER.info("Assuming mods directory is in ./mods");
				modsDirectory = new File("./mods");
			}else{
				String jarOnly = matcher.group(1);
				modsDirectory = new File(jarOnly).getParentFile();
			}

			File[] potentialMods = modsDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));


			/* The mods directory, is a directory... */
			if(potentialMods == null){
				throw new IllegalStateException("modsDir.listFiles() returned null! Attempted to index: " + modsDirectory);
			}

			for (File file : potentialMods) {
				JarFile jar = new JarFile(file);
				ZipEntry transformerEntry = jar.getEntry(TickCentral.NAME + "-loadable.txt");
				if(transformerEntry != null){
					BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(transformerEntry)));
					parseLoadables(reader, file);
				}
			}
		}catch (Throwable e){
			throw new RuntimeException(e);
		}
	}

	public void parseLoadables(BufferedReader reader, File file) throws Throwable {
		final AtomicReference<Throwable> e = new AtomicReference<>();
		reader.lines().forEach((line) -> {
			try {
				String loadable = line.trim();
				TickCentral.LOGGER.info("Loading: " + loadable);
				loader.addTransformerExclusion(loadable);
				loader.addURL(file.getAbsoluteFile().toURI().toURL());
				Class<?> clazz = Class.forName(loadable, true, loader);
				Object obj = clazz.newInstance();
				if (obj instanceof TransformerSupplier) {
					TransformerSupplier transformer = (TransformerSupplier) obj;
					CoreModManager.getReparseableCoremods().add(file.getName());
					transformer.onLoad(loader, side);
					suppliers.add(new AbstractMap.SimpleEntry<>(transformer.callOrder(), transformer));
				} else {
					throw new IllegalArgumentException("Class " + obj.getClass().getName() + " is not an instance of " + TransformerSupplier.class.getName());
				}
			}catch (Throwable t){
				e.set(t);
			}
		});
		reader.close();
		if(e.get() != null){
			throw e.get();
		}
	}
}
