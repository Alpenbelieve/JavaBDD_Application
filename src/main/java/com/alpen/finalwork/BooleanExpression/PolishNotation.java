package com.alpen.finalwork.BooleanExpression;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.util.*;

/**
 * @author :cde
 * @version V1.0
 * @ClassName: PolishNotation
 * @Description:
 * @Date :2019/4/26 15:14 Modification History: Date Author Version Discription
 *       -----------------------------------------------------------------------------------
 *       2019/4/26 Administrator 1.0 1.0
 * @Ref: https://www.cnblogs.com/chensongxian/p/7059802.html 
 * @Testcase1: not a and(not b or not c ) xor not d 
 * @Testcase2: a and b and c and d
 * @Testcase3: (A or C)and (B or A )
 * @Testcase4: (A or C)and ( (B or A ) and (D or C))
 */
public class PolishNotation {
	
	private static ArrayList<String> var_list;//存放变量的列表
	private static Map<String, Integer> var_counter_map;//和var_list配合统计变量数目
	private static Map<String, BDD> fixedBDDList;//存放每个变量对应的BDD,其中有的BDD因为变量出现不止一次，需要重复使用

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	//深拷贝一份hashmap，要不然当有多个输出变量时fixedBDDList就会被覆盖
	public static HashMap copyFixedBDDList(){
		HashMap<String, BDD> result = new HashMap<>();
		result.putAll(fixedBDDList);
		return result;
	}
//	public static void main(String[] args) {
//
//		Scanner sc = new Scanner(System.in);
//		System.out.println("请输入布尔函数表达式:");
//		String expressionStr = sc.nextLine();
//
//		List<String> middleOrderExpression = toMiddleOrderExpression(expressionStr);
//		System.out.println("中序表达式结果:" + middleOrderExpression.toString());
//
//		List<String> reversePolishExpression = toReversePolishExpression(middleOrderExpression);
//		System.out.println("逆波兰表达式结果:" + reversePolishExpression.toString());
//
//		//处理输入的布尔函数包含重复变量的情况
//		handleRepeatedVar();
//
//		System.out.println("\n真值表输出:");
//		BDD bddOutput = getBddDotOutput(reversePolishExpression);
//		bddOutput.printSet();
//	}

	/**
	 * 把字符串转换成中序表达式
	 *
	 * @param input
	 * @return
	 */
	public static List<String> toMiddleOrderExpression(String input) {
		var_list = new ArrayList();
		var_counter_map = new HashMap<>();
		fixedBDDList = new HashMap<>();
		List<String> result = new ArrayList<>();// 存储中序表达式
		StringBuffer buffer = new StringBuffer("");
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			// 如果输入是"and(x2"的形式，就把括号用空格隔开
			if (c == '(') {
				buffer.append(' ').append(c).append(' ');
			} else if (c == ')') {
				buffer.append(' ').append(c).append(' ');
			} else {
				buffer.append(c);
			}
		}
		// "[ ]+"表示一个或多个空格
		String[] slices = buffer.toString().split("[ ]+");
		for (String slice : slices) {
			if (!slice.equals("")) {
				result.add(slice);
			}
		}

		// 重要：因为not是单操作数运算符，所以在这里把它按双操作数运算符处理，处理方式是在not前面加个特殊的操作符
		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).equals("not")) {
				result.add(i, " ");
				i++;
			}

		}
		
		return result;
	}

	/**
	 * 转换成逆波兰表达式
	 *
	 * @param middleOrderExpression
	 * @return
	 */
	public static List<String> toReversePolishExpression(List<String> middleOrderExpression) {
		// 存放符号的栈
		Stack<String> symbolStack = new Stack<>();
		// 存放数字和符号的栈
		List<String> num_sym_list = new ArrayList<>();

		for (String element : middleOrderExpression) {
			if (element.equals("(")) {
				symbolStack.push(element);
			} else if (element.equals(")")) {
				while (!symbolStack.peek().equals("(")) {
					num_sym_list.add(symbolStack.pop());
				}
				symbolStack.pop();
			} else if (isBooleanSymbol(element)) {
				while (symbolStack.size() != 0
						&& Operation.getValue(symbolStack.peek()) >= Operation.getValue(element)) {
					num_sym_list.add(symbolStack.pop());
				}
				symbolStack.push(element);
			} else {// 如果是变量的情况
				num_sym_list.add(element);
				var_list.add(element);//注意var_list是专门存放变量的,之后会对变量出现的次数进行统计
			}
		}
		while (symbolStack.size() != 0) {
			num_sym_list.add(symbolStack.pop());
		}
		return num_sym_list;
	}

	public static Boolean isBooleanSymbol(String input) {
		// 正则表达式的意思是匹配11种运算符号
		if (input.matches("(?:and|or|not|diff|imp|biimp|invimp|less|nand|nor|xor)")) {
			return true;
		} else {
			return false;
		}
	}

	public static void handleRepeatedVar() {
		
		for(String element:var_list){
            Integer i=var_counter_map.get(element);
            if(i==null){
            	var_counter_map.put(element, 1);
            }else{
            	var_counter_map.put(element, i+1);
            }
        }
//		for(String key:var_counter_map.keySet()){
//			int counter = var_counter_map.get(key);
//            System.out.println(key+"出现了 " + counter +"次");
//        }
	}
	
	/**
	 * 通过逆波兰表达式计算真值表输出
	 *
	 * @param input
	 * @return
	 */
	public static BDD getBddDotOutput(List<String> input) {
		// BDD相关的初始化，数字设置得大点，以免jk冲突(理论上2倍size就够了)
		int size = input.size();
		BDDFactory factory = BDDFactory.init(1024, 1024);
		factory.setVarNum(size);

		// 存放BDD变量的栈
		Stack<BDD> bddStack = new Stack<>();
		
		//将各个变量的BDD都先生成好，存放在fixedBDDList中
		int tempCounter = 0;
		System.out.print("^");//作为截取输出流的字符，特别注意不能写println因为会对后面的输出解析带来麻烦

		for (String a : var_counter_map.keySet()) {
			BDD tempBDD = factory.ithVar(tempCounter);
			fixedBDDList.put(a, tempBDD);
			System.out.print(tempCounter+"="+a+"|");
			tempCounter++;
		}

		// j的作用是从前往后初始化变量BDD
		int j = 0;
		for (int i = 0; i < input.size(); i++) {
			String element = input.get(i);
			// 如果是变量，就在BDD的"序号-变量名"的对应表中加一条记录，然后从fixedBDDList取出事先生成好的BDD
			if (!isBooleanSymbol(element)) {
				bddStack.push(fixedBDDList.get(element));
			} else {
				BDD b = bddStack.pop();
				BDD a = bddStack.pop();
				BDD result = factory.ithVar(j);
				j++;
				if (element.equals("not")) {
					result = b.not();
				} else if (element.equals("and")) {
					result = a.apply(b, BDDFactory.and);
				} else if (element.equals("or")) {
					result = a.apply(b, BDDFactory.or);
				} else if (element.equals("diff")) {
					result = a.apply(b, BDDFactory.diff);
				} else if (element.equals("imp")) {
					result = a.apply(b, BDDFactory.imp);
				} else if (element.equals("biimp")) {
					result = a.apply(b, BDDFactory.biimp);
				} else if (element.equals("invimp")) {
					result = a.apply(b, BDDFactory.invimp);
				} else if (element.equals("less")) {
					result = a.apply(b, BDDFactory.less);
				} else if (element.equals("nand")) {
					result = a.apply(b, BDDFactory.nand);
				} else if (element.equals("nor")) {
					result = a.apply(b, BDDFactory.nor);
				} else if (element.equals("xor")) {
					result = a.apply(b, BDDFactory.xor);
				}
				bddStack.push(result);
			}
		}
		return bddStack.pop();
	}
}
