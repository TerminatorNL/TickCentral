package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;

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
		try {
			if (basicClass == null) {
				return basicClass;
			}
			ClassReader reader = new ClassReader(basicClass);
			Utilities.ensureOrderedLoading(transformedName, reader, getClass().getClassLoader());
			String className = reader.getClassName();
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			if (transformedName.equals(INTERFACE_CLASS_NON_OBF)) {
				KNOWN_ITICKABLE_SUPERS.add(classNode.name);
				MethodNode method = classNode.methods.get(0);
				UPDATE_METHOD = new AbstractMap.SimpleEntry<>(method.name, method.desc);
				MethodNode newUpdateTick = Utilities.CopyMethodAppearance(method);
				newUpdateTick.access = newUpdateTick.access - Opcodes.ACC_ABSTRACT;

				newUpdateTick.instructions = new InsnList();
				newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, classNode.name, ITickableTransformer.TRUE_ITICKABLE_UPDATE, UPDATE_METHOD.getValue(), true));
				newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));

				method.name = ITickableTransformer.TRUE_ITICKABLE_UPDATE;

				TickCentral.LOGGER.info("Modified interface: " + transformedName);
				classNode.methods.add(newUpdateTick);
				return ClassDebugger.WriteClass(classNode, transformedName);
			}


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


			/* THE INTERFACE ITSELF */
			if (UPDATE_METHOD == null) {
				if (classNode.methods.size() != 1) {
					TickCentral.LOGGER.fatal("ITickable interface had another modified by addition of another method! (another mod?). This is not allowed!");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}
				MethodNode method = classNode.methods.get(0);
				UPDATE_METHOD = new AbstractMap.SimpleEntry<>(method.name, method.desc);
				MethodNode newUpdateTick = Utilities.CopyMethodAppearance(method);
				newUpdateTick.access = newUpdateTick.access - Opcodes.ACC_ABSTRACT;

				newUpdateTick.instructions = new InsnList();
				newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, classNode.name, ITickableTransformer.TRUE_ITICKABLE_UPDATE, UPDATE_METHOD.getValue(), true));
				newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));

				method.name = ITickableTransformer.TRUE_ITICKABLE_UPDATE;

				TickCentral.LOGGER.info("Modified interface: " + transformedName);
				classNode.methods.add(newUpdateTick);
				return ClassDebugger.WriteClass(classNode, transformedName);
			}
			if ((classNode.access & Opcodes.ACC_INTERFACE) != 0) {
				TickCentral.LOGGER.info("(No need to modify this interface)");
				return basicClass;
			}

			/* ANY CLASS THAT ACTUALLY IMPLEMENTS IT */
			MethodNode newUpdateTick = null;
			for (MethodNode method : classNode.methods) {
				if (UPDATE_METHOD.getKey().equals(method.name) && UPDATE_METHOD.getValue().equals(method.desc)) {
					newUpdateTick = Utilities.CopyMethodAppearance(method);
					newUpdateTick.instructions = new InsnList();
					newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectUpdate", "(Lnet/minecraft/util/ITickable;)V", true));
					newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
					method.name = TRUE_ITICKABLE_UPDATE;
				}
			}
			if(newUpdateTick != null){
				classNode.methods.add(newUpdateTick);
			}
			for (MethodNode method : classNode.methods) {
				Utilities.convertTargetInstruction(className, UPDATE_METHOD.getKey(), UPDATE_METHOD.getValue(), className, TRUE_ITICKABLE_UPDATE, method.instructions);
				Utilities.convertSuperInstructions(UPDATE_METHOD.getKey(), UPDATE_METHOD.getValue(), TRUE_ITICKABLE_UPDATE, method.instructions);
			}
			return ClassDebugger.WriteClass(classNode, transformedName);
		} catch (Throwable e) {
			TickCentral.LOGGER.fatal("An error has occurred",e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}
}
