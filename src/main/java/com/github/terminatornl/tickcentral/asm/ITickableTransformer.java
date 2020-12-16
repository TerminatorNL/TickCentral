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

public class ITickableTransformer implements IClassTransformer {

	public static final String INTERFACE_CLASS_NON_OBF = "net.minecraft.util.ITickable";
	public static final String INTERFACE_CLASS_OBF = FMLDeobfuscatingRemapper.INSTANCE.unmap(INTERFACE_CLASS_NON_OBF.replace(".", "/"));
	public static final String TRUE_ITICKABLE_UPDATE = TickCentral.NAME + "_TrueITickableUpdate";

	public static Map.Entry<String, String> UPDATE_METHOD = null;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			/* THE INTERFACE ITSELF */
			if (UPDATE_METHOD == null) {
				ClassNode classNode = ClassSniffer.performOnSource(INTERFACE_CLASS_OBF, k -> {
					ClassNode node = new ClassNode();
					k.accept(node, 0);
					return node;
				});

				if (classNode.methods.size() != 1) {
					TickCentral.LOGGER.fatal("ITickable interface had another modified by addition of another method! (another mod?). This is not allowed! (Or even be possible)");
					FMLCommonHandler.instance().exitJava(1, false);
					throw new RuntimeException();
				}
				MethodNode method = classNode.methods.get(0);
				UPDATE_METHOD = new AbstractMap.SimpleEntry<>(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name,method.name,method.desc), FMLDeobfuscatingRemapper.INSTANCE.mapDesc(method.desc));
			}
			if (basicClass == null) {
				return null;
			}
			ClassReader reader = new ClassReader(basicClass);

			if (ClassSniffer.hasMixinAnnotation(reader) || !ClassSniffer.isInstanceOf(reader, INTERFACE_CLASS_OBF)) {
				return basicClass;
			}

			boolean dirty = false;

			String className = reader.getClassName();
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			if (transformedName.equals(INTERFACE_CLASS_NON_OBF)) {
				MethodNode method = classNode.methods.get(0);
				MethodNode newUpdateTick = Utilities.CopyMethodAppearanceAndStripOtherFromFinal(method);
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


			if(TickCentral.CONFIG.DEBUG){
				TickCentral.LOGGER.info("ITickable found: " + className + " (" + transformedName + ")");
			}

			if ((classNode.access & Opcodes.ACC_INTERFACE) != 0) {
				if(TickCentral.CONFIG.DEBUG){
					TickCentral.LOGGER.info("(No need to modify this interface)");
				}
				return basicClass;
			}

			/* ANY CLASS THAT ACTUALLY IMPLEMENTS IT */
			MethodNode newUpdateTick = null;
			for (MethodNode method : classNode.methods) {
				if((method.access & Opcodes.ACC_ABSTRACT) != 0){
					//Skip abstract methods.
					continue;
				}
				if (UPDATE_METHOD.getKey().equals(method.name) && UPDATE_METHOD.getValue().equals(method.desc)) {
					newUpdateTick = Utilities.CopyMethodAppearanceAndStripOtherFromFinal(method);
					newUpdateTick.instructions = new InsnList();
					newUpdateTick.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
					newUpdateTick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
					newUpdateTick.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectUpdate", "(Lnet/minecraft/util/ITickable;)V", true));
					newUpdateTick.instructions.add(new InsnNode(Opcodes.RETURN));
					method.name = TRUE_ITICKABLE_UPDATE;
					dirty = true;
					break;
				}
			}
			if(newUpdateTick != null){
				classNode.methods.add(newUpdateTick);
			}
			for (MethodNode method : classNode.methods) {
				dirty = Utilities.convertTargetInstruction(className, UPDATE_METHOD.getKey(), UPDATE_METHOD.getValue(), className, TRUE_ITICKABLE_UPDATE, method.instructions) || dirty;
				dirty = Utilities.convertSuperInstructions(UPDATE_METHOD.getKey(), UPDATE_METHOD.getValue(), TRUE_ITICKABLE_UPDATE, method.instructions) || dirty;
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
