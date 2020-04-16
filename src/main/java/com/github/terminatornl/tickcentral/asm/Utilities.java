package com.github.terminatornl.tickcentral.asm;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

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
		newNode.parameters = CopyParameterNodes(node.parameters);
		newNode.exceptions = new LinkedList<>(node.exceptions);
		newNode.attrs = node.attrs == null ? null : new LinkedList<>(node.attrs);
		return newNode;
	}

	public static List<ParameterNode> CopyParameterNodes(List<ParameterNode> nodes){
		List<ParameterNode> list = new LinkedList<>();
		for (ParameterNode node : nodes) {
			list.add(CopyParameterNode(node));
		}
		return list;
	}

	public static ParameterNode CopyParameterNode(ParameterNode node){
		return new ParameterNode(node.name, node.access);
	}
}
