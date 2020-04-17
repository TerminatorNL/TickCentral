package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.core.TrueITickable;
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
	public static Map.Entry<String, String> UPDATE_METHOD = null;

	static {
		KNOWN_ITICKABLE_SUPERS.add(INTERFACE_CLASS_NON_OBF.replace(".", "/"));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {

		if (basicClass == null) {
			return basicClass;
		}

		if (transformedName.equals(INTERFACE_CLASS_NON_OBF)) {
			KNOWN_ITICKABLE_SUPERS.add(name);
		}


		ClassReader reader = new ClassReader(basicClass);

		String className = reader.getClassName();
		boolean IS_SUPERTYPE_OF_ITICKABLE = false;
		if (KNOWN_ITICKABLE_SUPERS.contains(className)) {
			IS_SUPERTYPE_OF_ITICKABLE = true;
		} else if (KNOWN_ITICKABLE_SUPERS.contains(reader.getSuperName())) {
			KNOWN_ITICKABLE_SUPERS.add(className);
			IS_SUPERTYPE_OF_ITICKABLE = true;
		} else {
			for (String iface : reader.getInterfaces()) {
				if (KNOWN_ITICKABLE_SUPERS.contains(iface)) {
					IS_SUPERTYPE_OF_ITICKABLE = true;
					KNOWN_ITICKABLE_SUPERS.add(className);
				}
			}
		}
		if (IS_SUPERTYPE_OF_ITICKABLE == false) {
			return basicClass;
		}


		TickCentral.LOGGER.info("ITickable found: " + className + " (" + transformedName + ")");

		ClassNode classNode = new ClassNode();
		reader.accept(classNode, 0);

		if (UPDATE_METHOD == null) {
			if (classNode.methods.size() != 1) {
				TickCentral.LOGGER.fatal("ITickable interface was modified (another mod?). This is not allowed!");
				FMLCommonHandler.instance().exitJava(1, false);
				throw new RuntimeException();
			}
			MethodNode method = classNode.methods.get(0);
			UPDATE_METHOD = new AbstractMap.SimpleEntry<>(method.name, method.desc);
			classNode.interfaces.add("com/github/terminatornl/tickcentral/core/TrueITickable");
		}
		if((classNode.access & Opcodes.ACC_INTERFACE) != 0){
			TickCentral.LOGGER.info("(No need to modify interface)");
			return basicClass;
		}

		MethodNode newUpdateTick = null;
		for (MethodNode method : classNode.methods) {
			if (TickCentral.CONFIG.ITICKABLE_UPDATE_NAMES.contains(method.name) || (UPDATE_METHOD.getKey().equals(method.name) && UPDATE_METHOD.getValue().equals(method.desc))) {
				newUpdateTick = Utilities.CopyMethodAppearance(method);
				newUpdateTick.instructions = new InsnList();
				newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
				newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectUpdate", "(Lnet/minecraft/util/ITickable;)V", true));
				newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
				method.name = TRUE_ITICKABLE_UPDATE;
			}
		}
		if (newUpdateTick != null) {
			/*
				The current block has it's own implementation of UpdateTick
			 */
			classNode.methods.add(newUpdateTick);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		} else {
			return basicClass;
		}
	}

}
