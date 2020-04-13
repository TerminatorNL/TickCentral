package com.github.terminatornl.tickcentral.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null) {
			return null;
		}

		ClassReader reader = new ClassReader(basicClass);
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		CallMethodVisitor visitor = new CallMethodVisitor(writer);
		reader.accept(visitor, 0);

		if (visitor.isModified) {
			return writer.toByteArray();
		} else {
			return basicClass;
		}
	}

	public static class CallMethodVisitor extends ClassVisitor {

		private String className = null;
		private boolean isModified = false;

		private CallMethodVisitor(ClassVisitor cv) {
			super(Opcodes.ASM5, cv);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			this.className = name;
		}

		@Override
		public MethodVisitor visitMethod(int mAccess, final String mName, final String mDesc, String mSignature, String[] mExceptions) {
			return new MethodVisitor(Opcodes.ASM5, super.visitMethod(mAccess, mName, mDesc, mSignature, mExceptions)) {
				@Override
				public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isIntf) {
					switch (opcode) {
						case Opcodes.INVOKESTATIC:
						case Opcodes.INVOKEDYNAMIC:
						case Opcodes.INVOKEVIRTUAL:
							switch (owner) {
								case "net/minecraft/block/Block":
									switch (name) {
										case "updateTick":
											//isModified = true;
											System.out.println("Found reference to Block.updateTick(...) in class: " + className);
										case "randomTick":
											//isModified = true;
											System.out.println("Found reference to Block.randomTick(...) in class: " + className);
											break;
									}
								case "net/minecraft/util/ITickable":
									//isModified = true;
									System.out.println("Found reference to ITickable.update() in class: " + className);
							}

							break;
					}



					/*
					if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/System") && name.equals("exit") && desc.equals("(I)V")) {

						opcode = Opcodes.INVOKESTATIC;
						owner = CallMethodVisitor.callbackOwner;
						name = "runtimeHaltCalled";
						desc = "(Ljava/lang/Runtime;I)V";
					}*/

					super.visitMethodInsn(opcode, owner, name, desc, isIntf);
				}
			};
		}
	}
}
