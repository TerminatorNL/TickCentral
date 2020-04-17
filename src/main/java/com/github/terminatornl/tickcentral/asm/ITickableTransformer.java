package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class ITickableTransformer implements IClassTransformer {

	public static final String INTERFACE_CLASS_NON_OBF = "net.minecraft.util.ITickable";
	public static final String TRUE_ITICKABLE_UPDATE = TickCentral.NAME + "_TrueITickableUpdate";

	private static final HashSet<String> KNOWN_ITICKABLE_SUPERS = new HashSet<>();

	static {
		KNOWN_ITICKABLE_SUPERS.add(INTERFACE_CLASS_NON_OBF.replace(".", "/"));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {

		if(transformedName.equals(INTERFACE_CLASS_NON_OBF)){
			KNOWN_ITICKABLE_SUPERS.add(name);
		}

		if (basicClass == null) {
			return basicClass;
		}

		ClassReader reader = new ClassReader(basicClass);

		String className = reader.getClassName();
		boolean IS_SUPERTYPE_OF_ITICKABLE = false;
		if (KNOWN_ITICKABLE_SUPERS.contains(className)) {
			IS_SUPERTYPE_OF_ITICKABLE = true;
		} else if (KNOWN_ITICKABLE_SUPERS.contains(reader.getSuperName())) {
			KNOWN_ITICKABLE_SUPERS.add(className);
			IS_SUPERTYPE_OF_ITICKABLE = true;
		}else{
			for (String iface : reader.getInterfaces()) {
				if(KNOWN_ITICKABLE_SUPERS.contains(iface)){
					IS_SUPERTYPE_OF_ITICKABLE = true;
					KNOWN_ITICKABLE_SUPERS.add(className);
				}
			}
		}
		if(IS_SUPERTYPE_OF_ITICKABLE == false){
			return basicClass;
		}
		TickCentral.LOGGER.info("ITickable found: " + className);

		ClassNode classNode = new ClassNode();
		reader.accept(classNode, 0);



		return basicClass;
	}

}
