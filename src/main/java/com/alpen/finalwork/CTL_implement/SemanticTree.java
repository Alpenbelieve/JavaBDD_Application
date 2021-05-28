package com.alpen.finalwork.CTL_implement;

//CTL语义树
public class SemanticTree {
	String nodeValue;
	int childNum;//如果是AU之类的双操作数符号，则为2；如果是AX、非之类的单操作符号，则为1，如果是纯变量，则为0
	SemanticTree leftChild;
	SemanticTree rightChild;//默认有左右两个子树，当成二叉树处理。若为单操作符，则右子树为空，只看左节点
	public SemanticTree(String input){
		nodeValue = input;
	}
	public SemanticTree() {
	}
}
