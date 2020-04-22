package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.ClassDebugger;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class HubAPITransformer implements IClassTransformer {


	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			if (basicClass == null || transformedName.equals("com.github.terminatornl.tickcentral.api.TickHub") == false) {
				return basicClass;
			}
			ClassReader reader = new ClassReader(basicClass);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, 0);

			for (MethodNode method : classNode.methods) {
				switch (method.name) {
					case "trueRandomTick":
						Utilities.convertAllInstructions(BlockTransformer.TRUE_RANDOM_TICK_NAME, method.instructions);
						break;
					case "trueUpdateTick":
						Utilities.convertAllInstructions(BlockTransformer.TRUE_UPDATE_TICK_NAME, method.instructions);
						break;
					case "trueUpdate":
						Utilities.convertAllInstructions(ITickableTransformer.TRUE_ITICKABLE_UPDATE, method.instructions);
						break;
					case "trueOnUpdate":
						Utilities.convertAllInstructions(EntityTransformer.TRUE_ONUPDATE_TICK_NAME, method.instructions);
						break;
				}
			}
			return ClassDebugger.WriteClass(classNode, transformedName);
		} catch (Throwable e) {
			TickCentral.LOGGER.fatal("An error has occurred",e);
			FMLCommonHandler.instance().exitJava(1, false);
			throw new RuntimeException(e);
		}
	}


}
