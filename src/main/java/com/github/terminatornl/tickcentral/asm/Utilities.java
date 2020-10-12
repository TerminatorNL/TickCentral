package com.github.terminatornl.tickcentral.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Utilities {

	/**
	 * Copies the method in a way that Java will see this method as identical, but without the body.
	 * @param node the methodnode
	 * @return a shiny new imitator
	 */
	public static MethodNode CopyMethodAppearanceAndStripOtherFromFinal(MethodNode node){
		node.access &= ~Opcodes.ACC_FINAL;
		MethodNode newNode = new MethodNode();
		newNode.access = node.access;
		newNode.name = node.name;
		newNode.desc = node.desc;
		newNode.signature = node.signature;
		newNode.parameters = node.parameters == null ? null : CopyParameterNodes(node.parameters);
		newNode.exceptions = new LinkedList<>(node.exceptions);
		newNode.attrs = node.attrs == null ? null : new LinkedList<>(node.attrs);
		return newNode;
	}

	@Nonnull
	public static List<ParameterNode> CopyParameterNodes(@Nonnull List<ParameterNode> nodes){
		List<ParameterNode> list = new LinkedList<>();
		for (ParameterNode node : nodes) {
			list.add(CopyParameterNode(node));
		}
		return list;
	}

	public static ParameterNode CopyParameterNode(ParameterNode node){
		return new ParameterNode(node.name, node.access);
	}

	public static void convertAllInstructions(String newTarget, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			switch (node.getOpcode()){
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESPECIAL:
				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEVIRTUAL:
					MethodInsnNode methodNode = (MethodInsnNode) node;
					methodNode.name = newTarget;
			}
		}
	}

	public static boolean convertTargetInstruction(String targetOwner, String targetName, String targetDesc, String newOwner, String newName, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		boolean dirty = false;
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			switch (node.getOpcode()){
				case Opcodes.INVOKEINTERFACE:
				case Opcodes.INVOKESPECIAL:
				case Opcodes.INVOKESTATIC:
				case Opcodes.INVOKEVIRTUAL:
					MethodInsnNode methodNode = (MethodInsnNode) node;
					if(methodNode.owner.equals(targetOwner) && methodNode.name.equals(targetName) && methodNode.desc.equals(targetDesc)){
						methodNode.name = newName;
						methodNode.owner = newOwner;
						dirty = true;
					}
			}
		}
		return dirty;
	}

	public static boolean convertSuperInstructions(String targetName, String targetDesc, String newName, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		boolean dirty = false;
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (methodNode.name.equals(targetName) && methodNode.desc.equals(targetDesc)) {
					methodNode.name = newName;
					dirty = true;
				}
			}
		}
		return dirty;
	}

	public static boolean usesMethodInstruction(int opcode, String targetOwner, String targetName, String targetDesc, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() == opcode) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (methodNode.owner.equals(targetOwner) && methodNode.name.equals(targetName) && methodNode.desc.equals(targetDesc)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean usesConstant(InsnList instructions, Object constant){
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() == Opcodes.LDC) {
				LdcInsnNode ldc = (LdcInsnNode) node;
				if(ldc.cst == constant || constant.equals(ldc.cst)){
					return true;
				}
			}
		}
		return false;
	}
}
