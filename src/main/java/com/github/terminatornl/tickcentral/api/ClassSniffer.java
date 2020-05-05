package com.github.terminatornl.tickcentral.api;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Is able to determine if a class is an instance of a yet not-loaded class.
 * This assumes no transformers are changing superclasses DRAMATICALLY. I guess nobody is crazy enough to
 * actually change tonnes of classes and their hierarchy, so this should be reasonably safe.
 */
public class ClassSniffer {

	private static final HashMap<String, HashSet<String>> KNOWN_IMPLEMENTORS = new HashMap<>();
	private static final Set<String> classLoaderExceptions;
	private static final Set<String> transformerExceptions;

	static {
		try {
			Field classExceptions = LaunchClassLoader.class.getDeclaredField("classLoaderExceptions");
			classExceptions.setAccessible(true);
			//noinspection unchecked
			classLoaderExceptions = (Set<String>) classExceptions.get(ClassSniffer.class.getClassLoader());

			Field transformExceptions = LaunchClassLoader.class.getDeclaredField("transformerExceptions");
			transformExceptions.setAccessible(true);
			//noinspection unchecked
			transformerExceptions = (Set<String>) transformExceptions.get(ClassSniffer.class.getClassLoader());
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isProtected(String target) {
		target = target.replace("/", ".");
		for (String exceptions : classLoaderExceptions) {
			if (target.startsWith(exceptions)) {
				return true;
			}
		}
		for (String exceptions : transformerExceptions) {
			if (target.startsWith(exceptions)) {
				return true;
			}
		}
		return false;
	}

	private static void addKnownImplementor(String className, String obfuscated) {
		HashSet<String> implementors = KNOWN_IMPLEMENTORS.computeIfAbsent(obfuscated, k -> new HashSet<>());
		implementors.add(className);
	}

	private static boolean isKnownImplementor(String className, String obfuscated) {
		HashSet<String> implementors = KNOWN_IMPLEMENTORS.get(obfuscated);
		if (implementors == null) {
			return false;
		} else {
			return implementors.contains(className);
		}
	}

	public static boolean isInstanceOf(ClassReader reader, String obfuscated) throws IOException {
		return isInstanceOf(reader, obfuscated, true);
	}

	public static boolean isInstanceOf(ClassReader reader, String obfuscated, boolean disallowMixinSupers) throws IOException {
		String className = FMLDeobfuscatingRemapper.INSTANCE.unmap(reader.getClassName());
		if (isKnownImplementor(className, obfuscated)) {
			return true;
		}
		if (className.equals(obfuscated)) {
			addKnownImplementor(className, obfuscated);
			return true;
		}
		String superName = reader.getSuperName();
		if (superName.equals(obfuscated)) {
			addKnownImplementor(className, obfuscated);
			return true;
		}
		for (String iface : reader.getInterfaces()) {
			if (iface.equals(obfuscated)) {
				addKnownImplementor(className, obfuscated);
				return true;
			}
		}
		if (isProtected(superName) == false) {
			byte[] superClass = TickCentral.LOADER.getClassLoader().getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(superName));
			if (superClass == null) {
				TickCentral.LOGGER.warn("Unable to get superclass as resource: " + superName + " (" + FMLDeobfuscatingRemapper.INSTANCE.map(superName) + ") Do you have a broken installation? It is referenced in " + className + " (" + FMLDeobfuscatingRemapper.INSTANCE.map(className) + ")");
			} else {
				ClassReader superReader = new ClassReader(superClass);
				if (disallowMixinSupers) {
					ClassNode superClassNode = new ClassNode();
					superReader.accept(superClassNode, 0);
					if (superClassNode.invisibleAnnotations != null) {
						for (AnnotationNode annotation : superClassNode.invisibleAnnotations) {
							if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
								return false;
							}
						}
					}
				}
				if (isInstanceOf(superReader, obfuscated, disallowMixinSupers)) {
					return true;
				}
			}
		}
		for (String iface : reader.getInterfaces()) {
			if (isProtected(iface) == false) {
				byte[] ifaceData = TickCentral.LOADER.getClassLoader().getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(iface));
				if (ifaceData != null) {
					if (isInstanceOf(new ClassReader(ifaceData), obfuscated, disallowMixinSupers)) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public static <R> R performOnSource(String source, Function<ClassReader, R> callable) throws IOException, ClassNotFoundException {
		byte[] data = TickCentral.LOADER.getClassLoader().getClassBytes(FMLDeobfuscatingRemapper.INSTANCE.unmap(source));
		if(data == null){
			URL resource = Launch.classLoader.getResource(source + ".class");
			if(resource != null){
				data = IOUtils.toByteArray(resource);
			}
		}
		if (data == null) {
			throw new ClassNotFoundException("Unable to find class: " + source + " (" + FMLDeobfuscatingRemapper.INSTANCE.unmap(source) + ")");
		}
		return callable.apply(new ClassReader(data));
	}


}
