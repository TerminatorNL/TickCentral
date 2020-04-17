package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class BlockTransformer implements IClassTransformer {

	public static final String BLOCK_CLASS_NON_OBF = "net.minecraft.block.Block";
	public static final String TRUE_UPDATE_TICK_NAME = TickCentral.NAME + "_TrueUpdateTick";
	public static final String TRUE_RANDOM_TICK_NAME = TickCentral.NAME + "_TrueRandomTick";
	private static final HashSet<String> KNOWN_BLOCK_SUPERS = new HashSet<>();

	public static Map.Entry<String, String> RANDOM_TICK_METHOD = null;
	public static Map.Entry<String, String> UPDATE_TICK_METHOD = null;

	static {
		KNOWN_BLOCK_SUPERS.add(BLOCK_CLASS_NON_OBF.replace(".", "/"));
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		//TickCentral.LOGGER.info(name + " -> " + transformedName);

		if(transformedName.equals(BLOCK_CLASS_NON_OBF)){
			KNOWN_BLOCK_SUPERS.add(name);
		}

		if (basicClass == null) {
			return basicClass;
		}

		ClassReader reader = new ClassReader(basicClass);

		String className = reader.getClassName();
		boolean IS_SUPERTYPE_OF_BLOCK = false;
		if (KNOWN_BLOCK_SUPERS.contains(className)) {
			IS_SUPERTYPE_OF_BLOCK = true;
		} else if (KNOWN_BLOCK_SUPERS.contains(reader.getSuperName())) {
			KNOWN_BLOCK_SUPERS.add(className);
			IS_SUPERTYPE_OF_BLOCK = true;
		}

		if (IS_SUPERTYPE_OF_BLOCK) {
			//TickCentral.LOGGER.debug("Block found: " + className);

			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			if(RANDOM_TICK_METHOD == null || UPDATE_TICK_METHOD == null){
				/* Find the method to target. We base this off a java Random at the fourth position, and there are two identical methods */

				HashMap<String, Map.Entry<String, Integer>> targetMethods = new HashMap<>();
				LinkedList<String> potentialMethods = new LinkedList<>();
				String targetDescription = null;

				for (MethodNode method : classNode.methods) {
					String[] descSplit = method.desc.split(";");
					if (descSplit.length > 3 && descSplit[3].equals("Ljava/util/Random") && descSplit[descSplit.length - 1].equals(")V")) {
						TickCentral.LOGGER.info("Potential method found: " + className + " -> " + method.name + method.desc);

						if(potentialMethods.contains(method.desc)){
							targetDescription = method.desc;
							break;
						}else {
							potentialMethods.add(method.desc);
						}
					}
				}

				if(targetDescription == null){
					TickCentral.LOGGER.info("Unable to locate the obfuscated updateTick and randomTick method signature!");
					TickCentral.LOGGER.info("This probably means some other coremod has changed these methods, causing " + TickCentral.NAME + " not to work. In order to stop you from wasting your time, the server is stopped.");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}

				int highestInstructionCount = Integer.MIN_VALUE;
				for (MethodNode method : classNode.methods) {
					if(method.desc.equals(targetDescription)){
						targetMethods.put(method.name, new AbstractMap.SimpleEntry<>(method.desc, method.instructions.size()));
						highestInstructionCount = Math.max(highestInstructionCount, method.instructions.size());
					}
				}

				if(targetMethods.size() != 2){
					TickCentral.LOGGER.info("Unable to determine which methods are updateTick and randomTick!");
					TickCentral.LOGGER.info("This probably means some other coremod has changed these methods, causing " + TickCentral.NAME + " not to work. In order to stop you from wasting your time, the server is stopped.");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}

				for(Map.Entry<String, Map.Entry<String, Integer>> e : targetMethods.entrySet()){
					if(e.getValue().getValue() == highestInstructionCount){
						RANDOM_TICK_METHOD = new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getKey());
						TickCentral.LOGGER.info("Found randomTick update method: " + RANDOM_TICK_METHOD.getKey() + RANDOM_TICK_METHOD.getValue());
					}else{
						UPDATE_TICK_METHOD = new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().getKey());
						TickCentral.LOGGER.info("Found updateTick update method: " + UPDATE_TICK_METHOD.getKey() + UPDATE_TICK_METHOD.getValue());
					}
				}

			}

			MethodNode newUpdateTick = null;
			MethodNode newRandomTick = null;

			for (MethodNode method : classNode.methods) {
				if (TickCentral.CONFIG.UPDATE_TICK_NAMES.contains(method.name) || (UPDATE_TICK_METHOD.getKey().equals(method.name) && UPDATE_TICK_METHOD.getValue().equals(method.desc))) {
					newUpdateTick = Utilities.CopyMethodAppearance(method);
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

				} else if (TickCentral.CONFIG.RANDOM_TICK_NAMES.contains(method.name) || (RANDOM_TICK_METHOD.getKey().equals(method.name) && RANDOM_TICK_METHOD.getValue().equals(method.desc))) {
					newRandomTick = Utilities.CopyMethodAppearance(method);
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
				}
			}
			boolean isDirty = false;
			if (newUpdateTick != null) {
				/*
					The current block has it's own implementation of UpdateTick
				 */
				classNode.methods.add(newUpdateTick);
				isDirty = true;
			}
			if (newRandomTick != null) {
				/*
					The current block has it's own implementation of RandomTick
				 */
				classNode.methods.add(newRandomTick);
				isDirty = true;
			}
			if (isDirty) {
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
				classNode.accept(writer);
				return writer.toByteArray();
			} else {
				return basicClass;
			}
		} else {
			return basicClass;
		}
	}

}
