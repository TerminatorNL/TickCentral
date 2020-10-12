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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BlockTransformer implements IClassTransformer {

	public static final String BLOCK_CLASS_NON_OBF = "net.minecraft.block.Block";
	public static final String BLOCK_CLASS_OBF = FMLDeobfuscatingRemapper.INSTANCE.unmap(BLOCK_CLASS_NON_OBF.replace(".", "/"));
	public static final String TRUE_UPDATE_TICK_NAME = TickCentral.NAME + "_TrueUpdateTick";
	public static final String TRUE_RANDOM_TICK_NAME = TickCentral.NAME + "_TrueRandomTick";

	public static Map.Entry<String, String> RANDOM_TICK_METHOD = null;
	public static Map.Entry<String, String> UPDATE_TICK_METHOD = null;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			if (RANDOM_TICK_METHOD == null || UPDATE_TICK_METHOD == null) {
				/* Find the method to target. We base this off a java Random at the fourth position, and there are two identical methods */
				ClassNode classNode = ClassSniffer.performOnSource(BLOCK_CLASS_OBF, k -> {
					ClassNode node = new ClassNode();
					k.accept(node, 0);
					return node;
				});

				HashMap<String, Map.Entry<String, Integer>> targetMethods = new HashMap<>();
				LinkedList<String> potentialMethods = new LinkedList<>();
				String targetDescription = null;

				for (MethodNode method : classNode.methods) {
					method.name = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, method.name, method.desc);
					method.desc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc);
					String[] descSplit = method.desc.split(";");
					if (descSplit.length > 3 && descSplit[3].equals("Ljava/util/Random") && descSplit[descSplit.length - 1].equals(")V")) {
						TickCentral.LOGGER.info("Potential method found: " + classNode.name + " -> " + method.name + method.desc);

						if (potentialMethods.contains(method.desc)) {
							targetDescription = method.desc;
							break;
						} else {
							potentialMethods.add(method.desc);
						}
					}
				}

				if (targetDescription == null) {
					TickCentral.LOGGER.info("Unable to locate the obfuscated updateTick and randomTick method signature!");
					TickCentral.LOGGER.info("This probably means some other coremod has changed these methods, causing " + TickCentral.NAME + " not to work. In order to stop you from wasting your time, the server is stopped.");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}

				int highestInstructionCount = Integer.MIN_VALUE;
				for (MethodNode method : classNode.methods) {
					if (method.desc.equals(targetDescription)) {
						targetMethods.put(method.name, new AbstractMap.SimpleEntry<>(method.desc, method.instructions.size()));
						highestInstructionCount = Math.max(highestInstructionCount, method.instructions.size());
					}
				}

				if (targetMethods.size() != 2) {
					TickCentral.LOGGER.info("Unable to determine which methods are updateTick and randomTick!");
					TickCentral.LOGGER.info("This probably means some other coremod has changed these methods, causing " + TickCentral.NAME + " not to work. In order to stop you from wasting your time, the server is stopped.");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}

				for (Map.Entry<String, Map.Entry<String, Integer>> e : targetMethods.entrySet()) {
					if (e.getValue().getValue() == highestInstructionCount) {
						RANDOM_TICK_METHOD = new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getKey());
						TickCentral.LOGGER.info("Found randomTick update method: " + RANDOM_TICK_METHOD.getKey() + RANDOM_TICK_METHOD.getValue());
					} else {
						UPDATE_TICK_METHOD = new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getKey());
						TickCentral.LOGGER.info("Found updateTick update method: " + UPDATE_TICK_METHOD.getKey() + UPDATE_TICK_METHOD.getValue());
					}
				}
			}
			if (basicClass == null) {
				return null;
			}
			ClassReader reader = new ClassReader(basicClass);
			if (ClassSniffer.isInstanceOf(reader, BLOCK_CLASS_OBF) == false) {
				return basicClass;
			}
			String className = reader.getClassName();
			if (TickCentral.CONFIG.DEBUG) {
				TickCentral.LOGGER.info("Block found: " + className + " (" + transformedName + ")");
			}
			boolean dirty = false;

			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			MethodNode newRandomTick = null;
			MethodNode newUpdateTick = null;

			for (MethodNode method : classNode.methods) {
				if((method.access & Opcodes.ACC_ABSTRACT) != 0){
					//Skip abstract methods.
					continue;
				}
				if (UPDATE_TICK_METHOD.getKey().equals(method.name) && UPDATE_TICK_METHOD.getValue().equals(method.desc)) {
					newUpdateTick = Utilities.CopyMethodAppearanceAndStripOtherFromFinal(method);
					newUpdateTick.instructions = new InsnList();
					newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
					newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectUpdateTick", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", true));
					newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
					method.name = TRUE_UPDATE_TICK_NAME;
					dirty = true;
				} else if (RANDOM_TICK_METHOD.getKey().equals(method.name) && RANDOM_TICK_METHOD.getValue().equals(method.desc)) {
					newRandomTick = Utilities.CopyMethodAppearanceAndStripOtherFromFinal(method);
					newRandomTick.instructions = new InsnList();
					newRandomTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
					newRandomTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newRandomTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
					newRandomTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
					newRandomTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
					newRandomTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
					newRandomTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectRandomTick", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", true));
					newRandomTick.instructions.add(new InsnNode(Opcodes.RETURN));
					method.name = TRUE_RANDOM_TICK_NAME;
					dirty = true;
				}
			}
			if (newUpdateTick != null) {
				classNode.methods.add(newUpdateTick);
			}
			if (newRandomTick != null) {
				classNode.methods.add(newRandomTick);
			}
			for (MethodNode method : classNode.methods) {
				dirty = Utilities.convertTargetInstruction(className, RANDOM_TICK_METHOD.getKey(), RANDOM_TICK_METHOD.getValue(), className, TRUE_RANDOM_TICK_NAME, method.instructions) || dirty;
				dirty = Utilities.convertTargetInstruction(className, UPDATE_TICK_METHOD.getKey(), UPDATE_TICK_METHOD.getValue(), className, TRUE_UPDATE_TICK_NAME, method.instructions) || dirty;

				dirty = Utilities.convertSuperInstructions(RANDOM_TICK_METHOD.getKey(), RANDOM_TICK_METHOD.getValue(), TRUE_RANDOM_TICK_NAME, method.instructions) || dirty;
				dirty = Utilities.convertSuperInstructions(UPDATE_TICK_METHOD.getKey(), UPDATE_TICK_METHOD.getValue(), TRUE_UPDATE_TICK_NAME, method.instructions) || dirty;
			}
			if(dirty){
				return ClassDebugger.WriteClass(classNode, transformedName);
			}else{
				return basicClass;
			}
		} catch (Throwable e) {
			TickCentral.LOGGER.fatal("An error has occurred", e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}

}
