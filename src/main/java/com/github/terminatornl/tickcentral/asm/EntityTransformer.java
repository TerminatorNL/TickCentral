package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class EntityTransformer implements IClassTransformer {

	public static final String ENTITY_CLASS_NON_OBF = "net.minecraft.entity.Entity";
	public static final String TRUE_ONUPDATE_TICK_NAME = TickCentral.NAME + "_TrueOnUpdateTick";
	private static final HashSet<String> KNOWN_ENTITY_SUPERS = new HashSet<>();

	public static Map.Entry<String, String> ONUPDATE_TICK_METHOD = null;

	static {
		KNOWN_ENTITY_SUPERS.add(ENTITY_CLASS_NON_OBF.replace(".", "/"));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			if (transformedName.equals(ENTITY_CLASS_NON_OBF)) {
				KNOWN_ENTITY_SUPERS.add(name);
			}

			if (basicClass == null) {
				return basicClass;
			}

			ClassReader reader = new ClassReader(basicClass);
			Utilities.ensureOrderedLoading(transformedName, reader, getClass().getClassLoader());

			String className = reader.getClassName();
			boolean IS_SUPERTYPE_OF_ENTITY = false;
			if (KNOWN_ENTITY_SUPERS.contains(className)) {
				IS_SUPERTYPE_OF_ENTITY = true;
			} else if (KNOWN_ENTITY_SUPERS.contains(reader.getSuperName())) {
				KNOWN_ENTITY_SUPERS.add(className);
				IS_SUPERTYPE_OF_ENTITY = true;
			}

			if (IS_SUPERTYPE_OF_ENTITY) {

				if(TickCentral.CONFIG.DEBUG){
					TickCentral.LOGGER.info("Entity found: " + className + " (" + transformedName + ")");
				}


				ClassNode classNode = new ClassNode();
				reader.accept(classNode, 0);

				if (ONUPDATE_TICK_METHOD == null) {
					/* Find the method to target. We base this off one method calling the other which refers to a constant, where the caller is our target.*/

					String targetTargetMethod = null;
					for (MethodNode node : classNode.methods) {
						if (node.desc.equals("()V") && Utilities.usesConstant(node.instructions, "entityBaseTick")) {
							TickCentral.LOGGER.info("Found onEntityUpdate as " + node.name);
							targetTargetMethod = node.name;
							break;
						}
					}
					if (targetTargetMethod == null) {
						TickCentral.LOGGER.fatal("Unable to find the entity onEntityUpdate method! (Stage 1)");
						FMLCommonHandler.instance().exitJava(1, false);
						throw new RuntimeException();
					}
					for (MethodNode node : classNode.methods) {
						if (Utilities.usesMethodInstruction(Opcodes.INVOKEVIRTUAL, className, targetTargetMethod, "()V", node.instructions)) {
							TickCentral.LOGGER.info("Found onUpdate as " + targetTargetMethod);
							ONUPDATE_TICK_METHOD = new AbstractMap.SimpleEntry<>(node.name, node.desc);
						}
					}
					if (ONUPDATE_TICK_METHOD == null) {
						TickCentral.LOGGER.fatal("Unable to find the entity onUpdate method! (Stage 2)");
						FMLCommonHandler.instance().exitJava(1, false);
						throw new RuntimeException();
					}
				}
				MethodNode newUpdateTick = null;
				for (MethodNode method : classNode.methods) {
					if (ONUPDATE_TICK_METHOD.getKey().equals(method.name) && ONUPDATE_TICK_METHOD.getValue().equals(method.desc)) {
						newUpdateTick = Utilities.CopyMethodAppearance(method);
						newUpdateTick.instructions = new InsnList();
						newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
						newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
						newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectOnUpdate", "(Lnet/minecraft/entity/Entity;)V", true));
						newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
						method.name = TRUE_ONUPDATE_TICK_NAME;
					}
				}
				if(newUpdateTick != null){
					classNode.methods.add(newUpdateTick);
				}
				for (MethodNode method : classNode.methods) {
					Utilities.convertTargetInstruction(className, ONUPDATE_TICK_METHOD.getKey(), ONUPDATE_TICK_METHOD.getValue(), className, TRUE_ONUPDATE_TICK_NAME, method.instructions);
					Utilities.convertSuperInstructions(ONUPDATE_TICK_METHOD.getKey(), ONUPDATE_TICK_METHOD.getValue(), TRUE_ONUPDATE_TICK_NAME, method.instructions);
				}
				return ClassDebugger.WriteClass(classNode, transformedName);
			}
			return basicClass;
		} catch (Throwable e) {
			TickCentral.LOGGER.fatal("An error has occurred",e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}

}
