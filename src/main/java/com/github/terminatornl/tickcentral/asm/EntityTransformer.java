package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.ClassDebugger;
import com.github.terminatornl.tickcentral.api.ClassSniffer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.AbstractMap;
import java.util.Map;

public class EntityTransformer implements IClassTransformer {

	public static final String ENTITY_CLASS_NON_OBF = "net.minecraft.entity.Entity";
	public static final String ENTITY_CLASS_OBF = FMLDeobfuscatingRemapper.INSTANCE.unmap(ENTITY_CLASS_NON_OBF.replace(".", "/"));
	public static final String TRUE_ONUPDATE_TICK_NAME = TickCentral.NAME + "_TrueOnUpdateTick";

	public static Map.Entry<String, String> ONUPDATE_TICK_METHOD = null;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			if (ONUPDATE_TICK_METHOD == null) {
				/* Find the method to target. We base this off one method calling the other which refers to a constant, where the caller is our target.*/
				ClassNode classNode = ClassSniffer.performOnSource(ENTITY_CLASS_OBF, k -> {
					ClassNode node = new ClassNode();
					k.accept(node, 0);
					return node;
				});

				String targetTargetMethod = null;
				for (MethodNode node : classNode.methods) {
					if (node.desc.equals("()V") && Utilities.usesConstant(node.instructions, "entityBaseTick")) {
						TickCentral.LOGGER.info("Found onEntityUpdate as " + FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, node.name, node.desc) + FMLDeobfuscatingRemapper.INSTANCE.mapDesc(node.desc));
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
					if (Utilities.usesMethodInstruction(Opcodes.INVOKEVIRTUAL, classNode.name, targetTargetMethod, "()V", node.instructions)) {
						ONUPDATE_TICK_METHOD = new AbstractMap.SimpleEntry<>(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, node.name, node.desc), FMLDeobfuscatingRemapper.INSTANCE.mapDesc(node.desc));
						TickCentral.LOGGER.info("Found onUpdate as " + ONUPDATE_TICK_METHOD.getKey() + ONUPDATE_TICK_METHOD.getValue());
					}
				}
				if (ONUPDATE_TICK_METHOD == null) {
					TickCentral.LOGGER.fatal("Unable to find the entity onUpdate method! (Stage 2)");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}
			}
			if (basicClass == null) {
				return null;
			}
			ClassReader reader = new ClassReader(basicClass);
			if (ClassSniffer.isInstanceOf(reader, ENTITY_CLASS_OBF) == false) {
				return basicClass;
			}
			boolean dirty = false;
			String className = reader.getClassName();

			if(TickCentral.CONFIG.DEBUG){
				TickCentral.LOGGER.info("Entity found: " + className + " (" + transformedName + ")");
			}


			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);


			MethodNode newUpdateTick = null;
			for (MethodNode method : classNode.methods) {
				if((method.access & Opcodes.ACC_ABSTRACT) != 0){
					//Skip abstract methods.
					continue;
				}
				if (ONUPDATE_TICK_METHOD.getKey().equals(method.name) && ONUPDATE_TICK_METHOD.getValue().equals(method.desc)) {
					newUpdateTick = Utilities.CopyMethodAppearanceWithoutFinal(method);
					newUpdateTick.instructions = new InsnList();
					newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectOnUpdate", "(Lnet/minecraft/entity/Entity;)V", true));
					newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
					method.name = TRUE_ONUPDATE_TICK_NAME;
					dirty = true;
				}
			}
			if(newUpdateTick != null){
				classNode.methods.add(newUpdateTick);
			}
			for (MethodNode method : classNode.methods) {
				dirty = Utilities.convertTargetInstruction(className, ONUPDATE_TICK_METHOD.getKey(), ONUPDATE_TICK_METHOD.getValue(), className, TRUE_ONUPDATE_TICK_NAME, method.instructions) || dirty;
				dirty = Utilities.convertSuperInstructions(ONUPDATE_TICK_METHOD.getKey(), ONUPDATE_TICK_METHOD.getValue(), TRUE_ONUPDATE_TICK_NAME, method.instructions) || dirty;
			}
			if(dirty){
				return ClassDebugger.WriteClass(classNode, transformedName);
			}else{
				return basicClass;
			}
		} catch (Throwable e) {
			TickCentral.LOGGER.fatal("An error has occurred",e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}

}
