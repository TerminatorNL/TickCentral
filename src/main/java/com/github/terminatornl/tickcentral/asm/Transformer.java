package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.LinkedList;

public class Transformer implements IClassTransformer {

	public static final String TRUE_UPDATE_TICK_NAME = TickCentral.NAME + "_TrueUpdateTick";
	public static final String TRUE_RANDOM_TICK_NAME = TickCentral.NAME + "_TrueRandomTick";
	private static final HashSet<String> KNOWN_BLOCK_SUPERS = new HashSet<>();
	//private static final HashSet<String> KNOWN_TICKABLE_SUPERS = new HashSet<>();
	static{
		KNOWN_BLOCK_SUPERS.add("net/minecraft/block/Block");
		//KNOWN_TICKABLE_SUPERS.add("net/minecraft/util/ITickable");
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null) {
			return null;
		}

		ClassReader reader = new ClassReader(basicClass);

		String className = reader.getClassName();
		boolean IS_SUPERTYPE_OF_BLOCK = false;
		if(KNOWN_BLOCK_SUPERS.contains(className)){
			IS_SUPERTYPE_OF_BLOCK = true;
		}else if(KNOWN_BLOCK_SUPERS.contains(reader.getSuperName())){
			KNOWN_BLOCK_SUPERS.add(className);
			IS_SUPERTYPE_OF_BLOCK = true;
		}

		if(IS_SUPERTYPE_OF_BLOCK){
			TickCentral.LOGGER.info("Block found: " + className);




			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			//BlockMethodRedirector visitor = new BlockMethodRedirector(writer);

			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			/* CHANGES GO HERE */

			MethodNode originalMethodUpdateTickSignature = null;
			MethodNode originalMethodRandomTickSignature = null;


			for (MethodNode method : classNode.methods) {
				if(method.name.equals("updateTick")){
					originalMethodUpdateTickSignature = Utilities.CopyMethodAppearance(method);
					method.name = TRUE_UPDATE_TICK_NAME;
				}else if(method.name.equals("randomTick")){
					originalMethodRandomTickSignature = Utilities.CopyMethodAppearance(method);
					method.name = TRUE_RANDOM_TICK_NAME;
				}
			}

			{
				MethodNode newUpdate = new MethodNode();
				newUpdate.exceptions = new LinkedList<>();
				newUpdate.access = Opcodes.ACC_PUBLIC;
				newUpdate.name = "updateTick";
				newUpdate.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V";



				newUpdate.instructions = new InsnList();
				newUpdate.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "EXECUTOR", "Lcom/github/terminatornl/tickcentral/api/TickExecutor;"));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
				newUpdate.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickExecutor", "redirectUpdateTick", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", true));
				newUpdate.instructions.add(new InsnNode(Opcodes.RETURN));

				classNode.methods.add(newUpdate);
			}

			{
				MethodNode newUpdate = new MethodNode();
				newUpdate.exceptions = new LinkedList<>();
				newUpdate.access = Opcodes.ACC_PUBLIC;
				newUpdate.name = "randomTick";
				newUpdate.desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V";
				newUpdate.instructions = new InsnList();
				newUpdate.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "EXECUTOR", "Lcom/github/terminatornl/tickcentral/api/TickExecutor;"));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
				newUpdate.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
				newUpdate.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickExecutor", "redirectRandomTick", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V", true));
				newUpdate.instructions.add(new InsnNode(Opcodes.RETURN));

				classNode.methods.add(newUpdate);
			}



	/*

	LINENUMBER 27 L0
    GETSTATIC com/github/terminatornl/tickcentral/api/TickHub.EXECUTOR : Lcom/github/terminatornl/tickcentral/api/TickExecutor;
    ALOAD 0
    ALOAD 1
    ALOAD 2
    ALOAD 3
    ALOAD 4
    INVOKEINTERFACE com/github/terminatornl/tickcentral/api/TickExecutor.redirectUpdateTick (Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V
   L1
    LINENUMBER 28 L1
    RETURN


	 */





			classNode.accept(writer);




			return writer.toByteArray();
		}else{
			return basicClass;
		}
	}

}
