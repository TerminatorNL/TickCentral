package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Utilities {

	/**
	 * Copies the method in a way that Java will see this method as identical, but without the body.
	 * @param node the methodnode
	 * @return a shiny new imitator
	 */
	public static MethodNode CopyMethodAppearance(MethodNode node){
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

	public static void convertTargetInstruction(String targetOwner, String targetName, String targetDesc, String newOwner, String newName, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
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
					}
					break;
			}
		}
	}

	public static void convertSuperInstructions(String targetName, String targetDesc, String newName, InsnList instructions) {
		Iterator<AbstractInsnNode> iterator = instructions.iterator();
		while(iterator.hasNext()){
			AbstractInsnNode node = iterator.next();
			if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
				MethodInsnNode methodNode = (MethodInsnNode) node;
				if (methodNode.name.equals(targetName) && methodNode.desc.equals(targetDesc)) {
					methodNode.name = newName;
				}
			}
		}
	}

	private static final HashSet<String> LOADED_CLASSES = new HashSet<>();

	/**
	 * Ensures all classes are loaded in order.
	 * This is required to determine object hierarchy which is used to determine if we modify a class or not.
	 * @param transformedName t
	 * @param reader r
	 * @param loader l
	 * @throws Throwable any error
	 */
	public static void ensureOrderedLoading(String transformedName, ClassReader reader, ClassLoader loader) throws Throwable{
		String superClass = reader.getSuperName().replace("/",".");
		if(LOADED_CLASSES.contains(superClass) == false){
			if(TickCentral.CONFIG.SHOW_FORCED_LOADING){
				TickCentral.LOGGER.info("Loading superclass: " + superClass + " referenced in " + reader.getClassName() + " (" + transformedName + ")");
			}
			Class.forName(superClass, false, loader);
			LOADED_CLASSES.add(superClass);
		}
		for (String iface : reader.getInterfaces()) {
			iface = iface.replace("/",".");
			if(LOADED_CLASSES.contains(iface) == false){
				if(TickCentral.CONFIG.SHOW_FORCED_LOADING) {
					TickCentral.LOGGER.info("Loading interface: " + iface + " referenced in " + reader.getClassName() + " (" + transformedName + ")");
				}
				Class.forName(iface, false, loader);
				LOADED_CLASSES.add(iface);
			}
		}
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
