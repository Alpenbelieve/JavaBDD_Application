package com.alpen.finalwork.CTL_implement;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import com.alpen.finalwork.Graphviz.GraphViz;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.springframework.util.ResourceUtils;


/**
 * CTL实现类的第二个版本，先将公式拆分成语义树，然后从叶子节点开始处理，直到根节点。可以处理复合或带括号的CTL公式
 * 
 * @author cde
 *
 */
public class CTL_implement_v2 {

	private static String[] S;// 存放所有状态的集合
	private static BDD[] state_bdds;// 存放所有状态对应的BDD，index和S一致
	private static ArrayList<transition> TRANS;// 存放所有的迁移关系
	private static Map<String, String[]> L;// 存放所有的L函数
	private static String[] booleanVector;// 存放所有的布尔元素，若干布尔元素组合成一个状态
	private static BDDFactory B;

	public static BDD S_bdd;// 状态集合的最终BDD表示
	public static BDD TRANS_bdd;// 迁移关系的最终BDD表示
	private static BDDPairing adjustOddOrder;// 转换编序，将0,1,2,3转为迁移关系的end端编序1,3,5,7
	private static BDDPairing adjustEvenOrder;// 转换编序，将0,1,2,3转为迁移关系的start端编序0,2,4,6
	private static BDDPairing adjustEvenOrderBack;// 将迁移关系的start端编序转换回来，将0,2,4,6转为迁移关系的end端编序0,1,2,3


	public static BDD getSatStatesBySemanticTree(SemanticTree sTree) {

		//节点的两个子节点可能有三种情况：1.两个子节点都存在；2.只有左子节点存在；3.两个子节点都不存在
		if (sTree.childNum == 2) {
			if (sTree.nodeValue.equals("AU")) {
				return bdd_SUB_bdd(S_bdd,bdd_OR_bdd(SAT_EU(bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.rightChild)),bdd_AND_bdd(bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.leftChild)),bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.rightChild)))),SAT_EG(bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.rightChild)))));
			} else if (sTree.nodeValue.equals("EU")) {
				return SAT_EU(getSatStatesBySemanticTree(sTree.leftChild),getSatStatesBySemanticTree(sTree.rightChild));
			} else if (sTree.nodeValue.equals("→")) {
				return bdd_OR_bdd(bdd_SUB_bdd(S_bdd, getSatStatesBySemanticTree(sTree.leftChild)), getSatStatesBySemanticTree(sTree.rightChild));
			} else if (sTree.nodeValue.equals("v")) {
				return bdd_OR_bdd(getSatStatesBySemanticTree(sTree.leftChild),getSatStatesBySemanticTree(sTree.rightChild));
			} else if (sTree.nodeValue.equals("^")) {
				return bdd_AND_bdd(getSatStatesBySemanticTree(sTree.leftChild),getSatStatesBySemanticTree(sTree.rightChild));
			} else {
				System.err.println("sTree.childNum == 2 的情况有问题");
			}
		} else if (sTree.childNum == 1) {
			if (sTree.nodeValue.equals("AX")) {
				return bdd_SUB_bdd(S_bdd, SAT_EX(bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.leftChild))));
			} else if (sTree.nodeValue.equals("EX")) {
				return SAT_EX(getSatStatesBySemanticTree(sTree.leftChild));
			} else if (sTree.nodeValue.equals("AF")) {
				return bdd_SUB_bdd(S_bdd, SAT_EG(bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.leftChild))));
			} else if (sTree.nodeValue.equals("EF")) {
				return SAT_EU(S_bdd,getSatStatesBySemanticTree(sTree.leftChild));
			} else if (sTree.nodeValue.equals("AG")) {
				return bdd_SUB_bdd(S_bdd,SAT_EU(S_bdd,bdd_SUB_bdd(S_bdd,getSatStatesBySemanticTree(sTree.leftChild))));
			} else if (sTree.nodeValue.equals("EG")) {
				return SAT_EG(getSatStatesBySemanticTree(sTree.leftChild));
			} else if (sTree.nodeValue.equals("﹁")) {
				return getSatStatesBySemanticTree(sTree.leftChild).not();
			} else {
				System.err.println("sTree.childNum == 1 的情况有问题");
			}
		} else {//sTree.childNum==0
			if (sTree.nodeValue.equals("\u22a4")) {
				return S_bdd;
			} else if (sTree.nodeValue.equals("⊥")) {
				return S_bdd.not();
			} else if (Pattern.matches("\\w*", sTree.nodeValue)) {
				return get_atomic_bdd(sTree.nodeValue);
			} else {
				System.err.println("sTree.childNum == 0 的情况有问题");
			}
		}
		//到了这里说明有问题
		System.err.println("getSatStatesBySemanticTree 出错了");
		return S_bdd.not();
	}


	// 输入为无法再拆分的表示布尔原子字符串，返回S中满足该布尔变量的状态
	private static BDD get_atomic_bdd(String input) {
		List<Integer> states = new ArrayList<>();// 存放S中满足条件的状态的index
		for (int i = 0; i < S.length; i++) {
			if (isInStringList(input, L.get(S[i]))) {
				states.add(i);
			}
		}
		BDD result = B.zero();
		if (states.size() > 0) {
			for (int i : states) {
				result = result.or(state_bdds[i]);
			}
		}
		return result;
	}

	private static BDD SAT_EX(BDD input) {
		// 以下的代码使用BDD实现EX功能
		BDD X = input;
		X = X.replace(adjustOddOrder);// 调整编序，比如说如果X的BDD有3个节点，调整为[1,3,5]
		BDD result = get_pre_states(X);

		return result.replace(adjustEvenOrderBack);// 把编序调整回来
	}

	/**
	 *
	 * @param input
	 *            状态集合(其实就是SAT()的返回值)，注意，为了避免重复调用replace，传入的参数的编序是1,3,5,...
	 * @return 这些状态集合的前一个状态的集合，注意，为了避免重复调用replace，返回的结果的编序是0,2,4,...
	 */
	private static BDD get_pre_states(BDD input) {
		int nodeCount = input.nodeCount();
		if (nodeCount == 0) {
			return B.zero();
		} else if (nodeCount == booleanVector.length) {
			return TRANS_bdd.restrict(input);
		} else {// 这个时候采取一个特殊的策略：遍历所有的状态，看它是不是“有可能”是目前的状态集合的前一个
			BDD result = B.zero();
			// 注意这里需要遍历input的每一条到终点1的路线，因为restrict()中如果出现or可能会导致未知错误。这样其实会降低一些效率
			for (Object object : input.allsat()) {
				if (object instanceof byte[]) {
					BDD anAssignment = fromByteListToBDD((byte[]) object);
					for (int i = 0; i < state_bdds.length; i++) {
						BDD temp = state_bdds[i].replace(adjustEvenOrder);
						BDD restricted = TRANS_bdd.restrict(anAssignment).restrict(temp);
						if (restricted.pathCount() > 0.0 || restricted.equals(B.one())) {
							result = result.or(temp);
						}
					}
				} else {
					System.err.println("allsat()转化成byte[]时出错！");
				}
			}
			return result;
		}
	}

	/**
	 * 输入元素的长为B的setVar的值，出来的图片上只有1,3,5,...等节点
	 */
	private static BDD fromByteListToBDD(byte[] assignment) {
		BDD result = B.one();
		for (int i = 0; i < assignment.length; i++) {
			byte element = assignment[i];
			if (element == 0) {
				result = result.and(B.ithVar(i).not());
			} else if (element == 1) {
				result = result.and(B.ithVar(i));
			}
		}
		return result;
	}

	// 本质上是计算最小不动点
	private static BDD SAT_EU(BDD input1, BDD input2) {
		BDD W, X, Y;
		W = input1;
		X = S_bdd.id();// 复制一份，免得产生的修改影响了代表S的BDD
		Y = input2;

		if (X.equals(Y)) {
			return Y;
		}
		while (!X.equals(Y.replace(adjustOddOrder))) {
			X = Y.replace(adjustOddOrder);// 调整编序，比如说如果X的BDD有3个节点，调整为[1,3,5]
			BDD pre_states = get_pre_states(X);//此时pre_states的编序为0,2,4,...
			Y = bdd_OR_bdd(Y, bdd_AND_bdd(W, pre_states.replace(adjustEvenOrderBack)));
		}
		return Y;
	}

	// 本质上是计算最大不动点
	private static BDD SAT_EG(BDD input) {
		BDD X, Y;
		Y = input;
		X = B.zero();

		if (X.equals(Y)) {
			return Y;
		}
		while (!(X.equals(Y.replace(adjustOddOrder)))) {
			X = Y.replace(adjustOddOrder);// 调整编序，比如说如果X的BDD有3个节点，调整为[1,3,5]
			BDD pre_states = get_pre_states(X);//此时pre_states的编序为0,2,4,...
			Y = bdd_AND_bdd(Y, pre_states.replace(adjustEvenOrderBack));
		}
		return Y;
	}

	// 求两个集合的并集
	private static BDD bdd_OR_bdd(BDD bdd1, BDD bdd2) {
		return bdd1.or(bdd2);
	}

	// 求两个集合的交集
	private static BDD bdd_AND_bdd(BDD bdd1, BDD bdd2) {
		return bdd1.and(bdd2);
	}

	// 求两个集合的差集
	// A-B = A ∧ ﹁B
	private static BDD bdd_SUB_bdd(BDD bdd1, BDD bdd2) {
		return bdd1.and(bdd2.not());
	}

	/**
	 *
	 * @param input1 布尔变量
	 * @param input2 状态变量
	 * @param input3 迁移关系
	 */
	public static void draw_CTL(String input1, String input2, String input3) {
		booleanVector = input1.split(" ");// 输入格式：x1 x2
		B = BDDFactory.init(1024, 1024);
		B.setVarNum(booleanVector.length * 2);// 非常重要，因为要考虑到修改编序时需要更多的变量

		String[] stateAndL = input2.split(";|;\\s+");// 输入格式：s0:p q; s1:q r; s2:r
		S = new String[stateAndL.length];
		L = new HashMap<>();
		for (int i = 0; i < stateAndL.length; i++) {
			String[] temp = stateAndL[i].split(":");
			S[i] = temp[0].trim();
			L.put(S[i], temp[1].trim().split(" "));
		}
		state_bdds = new BDD[S.length];

		TRANS = string2trans(input3.split(" "));// 输入格式：s0,s1 s1,s2 s2,s0 s2,s2

		getDotOutput();
	}

	public static void get_states_bdd() {
		for (int i = 0; i < S.length; i++) {
			BDD b = B.one();
			String[] l_valueset = L.get(S[i]);
			for (int j = 0; j < booleanVector.length; j++) {
				if (isInStringList(booleanVector[j], l_valueset)) {
					b = b.and(B.ithVar(j));
				} else {
					b = b.and(B.ithVar(j).not());
				}
			}
			state_bdds[i] = b;
		}
	}

	public static BDD get_S_bdd() {
		BDD result = B.zero();
		for (int i = 0; i < state_bdds.length; i++) {
			result = result.or(state_bdds[i]);
		}
		return result;
	}

	// 使用BDDdomain类来表示
	public static BDD get_TRANS_bdd() {
		int domainSize = (int) Math.pow(2, booleanVector.length);
		B.extDomain(new int[] { domainSize, domainSize });
		int[] d1 = B.getDomain(0).vars();
		int[] d2 = B.getDomain(1).vars();

		BDD result = B.zero();
		for (transition trans : TRANS) {
			BDD temp = B.one();// 重要！不能写在循环外面
			for (int i = 0; i < S.length; i++) {
				if ((trans.start).equals(S[i])) {
					String[] l_valueset = L.get(S[i]);

					for (int j = 0; j < booleanVector.length; j++) {
						if (isInStringList(booleanVector[j], l_valueset)) {
							temp = temp.and(B.ithVar(d1[j]));
						} else {
							temp = temp.and(B.ithVar(d1[j]).not());
						}
					}
					break;
				}
			}
			// 这里的for循环必须写两次，不能合并，否则s1->s0情况下的BDD的顺序就错了
			for (int i = 0; i < S.length; i++) {
				if ((trans.end).equals(S[i])) {
					String[] l_valueset = L.get(S[i]);

					for (int j = 0; j < booleanVector.length; j++) {
						if (isInStringList(booleanVector[j], l_valueset)) {
							temp = temp.and(B.ithVar(d2[j]));
						} else {
							temp = temp.and(B.ithVar(d2[j]).not());
						}
					}
					break;
				}
			}
			result = result.or(temp);
		}
		return result;
	}

	private static ArrayList<transition> string2trans(String[] input) {
		ArrayList<transition> result = new ArrayList<>();
		for (String element : input) {
			transition t = new transition();
			t.start = element.split(",")[0];
			t.end = element.split(",")[1];
			result.add(t);
		}
		return result;
	}

	private static void getDotOutput() {

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());

		for (String s : L.keySet()) {
			gv.add(s + "[label=\"");
			for (String s1 : L.get(s)) {
				gv.add(s1+" ");
			}
			gv.add("\"] " + "[xlabel=" + s + "]\n");
		}
		for (transition t : TRANS) {
			gv.add(t.toString() + "\n");
		}
		gv.addln(gv.end_graph());
		String type = "svg";
//		File out = new File("C:\\Users\\cde\\Desktop\\DotOutput1." + type);
//		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, GraphViz.DOT), out);
		File outDest = null;
		try {
			String path = ResourceUtils.getURL("classpath:").getPath();
			outDest = new File(path.replace("%20", " "), "static/images/DotOutput1." + type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, GraphViz.DOT), outDest);

	}

	private static boolean isInStringList(String input, String[] list) {
		if (list.length > 0) {
			for (String element : list) {
				if (input.equals(element)) {
					return true;
				}
			}
		}
		return false;
	}

	// 检验某个状态是否在BDD中
	private static boolean isInBDD(BDD input, String state) {
		if (state != null) {
			for (int i = 0; i < S.length; i++) {
				if (S[i].equals(state)) {
					return input.or(state_bdds[i].not()).equals(B.one());// 也可以使用restrict()
				}
			}
		}
		System.err.println("输入的状态不合法");
		return false;
	}

	// 从BDD中提取状态的集合
	public static ArrayList<String> extractStatesFromBDD(BDD input) {
		ArrayList<String> result = new ArrayList<>();
		for (int i = 0; i < S.length; i++) {
			if (input.restrict(state_bdds[i]).equals(B.one())) {
				result.add(S[i]);
			}
		}
		return result;
	}

	public static void BDDPairingConfig() {
		adjustOddOrder = B.makePair();// 0,1,2,3 -> 1,3,5,7
		int replaceNum = booleanVector.length;
		int[] index1 = new int[replaceNum];
		int[] index2 = new int[replaceNum];
		for (int i = replaceNum - 1; i >= 0; i--) {
			index1[i] = i;
			index2[i] = i * 2 + 1;
		}
		adjustOddOrder.set(index1, index2);

		adjustEvenOrder = B.makePair();// 0,1,2,3 -> 0,2,4,6
		int[] index7 = new int[replaceNum - 1];
		int[] index8 = new int[replaceNum - 1];
		for (int i = replaceNum - 2; i >= 0; i--) {
			index7[i] = i + 1;
			index8[i] = (i + 1) * 2;
		}
		adjustEvenOrder.set(index7, index8);

		adjustEvenOrderBack = B.makePair();// 0,2,4,6 -> 0,1,2,3
		int[] index3 = new int[replaceNum - 1];
		int[] index4 = new int[replaceNum - 1];
		for (int i = 1; i < replaceNum; i++) {
			index3[i - 1] = i * 2;
			index4[i - 1] = i;
		}
		adjustEvenOrderBack.set(index3, index4);
	}
}

class transition {
	String start;
	String end;

	@Override
	public String toString() {
		return start + "->" + end;
	}
}