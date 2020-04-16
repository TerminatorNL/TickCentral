package com.github.terminatornl.tickcentral.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * This class is capable of transforming classes that reference a method.
 * Can be used to track down callers of a method.
 */
public class ReverseCallingMethodVisitor extends ClassVisitor {

	private String className = null;

	public ReverseCallingMethodVisitor(ClassVisitor cv) {
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

										//TODO: IF BLOCK CALLING SUPER, MOVE TO TICKHUB TRUE TICKING

									case "randomTick":
										//isModified = true;
										System.out.println("Found reference to Block.randomTick(...) in class: " + className);


										//TODO: IF BLOCK CALLING SUPER, MOVE TO TICKHUB TRUE TICKING

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
