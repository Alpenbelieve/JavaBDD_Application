package com.alpen.finalwork.CTL_implement;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

import com.alpen.finalwork.CTL_implement.SemanticTree;
import com.alpen.finalwork.Graphviz.GraphViz;
import net.sf.javabdd.BDD;
import org.springframework.util.ResourceUtils;


public class CTL_parser {

//    private static String CTL_Formula;
    private static GraphViz gv;
    private static int DOT_Node_counter;

    public static SemanticTree CTL_formula_parser(String input) {

//        CTL_Formula = input;
        DOT_Node_counter = 0;
        SemanticTree result = new SemanticTree();

        if (Pattern.matches("A\\[.*", input)) {
            result.nodeValue = "AU";
            result.childNum = 2;
            String[] substrings = AU_or_EU_get_two_substring(input.substring(2));
            result.leftChild = CTL_formula_parser(substrings[0]);
            result.rightChild = CTL_formula_parser(substrings[1]);
            return result;
        } else if (Pattern.matches("E\\[.*", input)) {
            result.nodeValue = "EU";
            result.childNum = 2;
            String[] substrings = AU_or_EU_get_two_substring(input.substring(2));
            result.leftChild = CTL_formula_parser(substrings[0]);
            result.rightChild = CTL_formula_parser(substrings[1]);
            return result;
        } else if (Pattern.matches("AX.*", input)) {
            result.nodeValue = "AX";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("EX.*", input)) {
            result.nodeValue = "EX";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("AF.*", input)) {
            result.nodeValue = "AF";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("EF.*", input)) {
            result.nodeValue = "EF";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("AG.*", input)) {
            result.nodeValue = "AG";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("EG.*", input)) {
            result.nodeValue = "EG";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(2));
            return result;
        } else if (Pattern.matches("\\(.*", input)) {
            return CTL_formula_parser(get_string_between_brackets(input));
        }
//		else if (Pattern.matches("[.*", input)) {//不应该出现这种情况的
//			result.nodeValue = "";
//			result.childNum = 1;}
        else if (Pattern.matches("﹁.*", input)) {
            result.nodeValue = "﹁";
            result.childNum = 1;
            result.leftChild = CTL_formula_parser(input.substring(1));
            return result;
        } else if (Pattern.matches("\\w*\\^\\w*", input)) {//注意，这里不需要修改\w的范围，因为这种情况放在了最后，会先判断AF、EX等情况
            result.nodeValue = "^";
            result.childNum = 2;
            result.leftChild = CTL_formula_parser(input.substring(0, input.indexOf('^')));
            result.rightChild = CTL_formula_parser(input.substring(input.indexOf('^') + 1));
            return result;
        } else if (Pattern.matches("\\w*v\\w*", input)) {
            result.nodeValue = "v";
            result.childNum = 2;
            result.leftChild = CTL_formula_parser(input.substring(0, input.indexOf('v')));
            result.rightChild = CTL_formula_parser(input.substring(input.indexOf('v') + 1));
            return result;
        } else if (Pattern.matches("\\w*\u2192\\w*", input)) {
            result.nodeValue = "→";
            result.childNum = 2;
            result.leftChild = CTL_formula_parser(input.substring(0, input.indexOf('\u2192')));
            result.rightChild = CTL_formula_parser(input.substring(input.indexOf('\u2192') + 1));
            return result;
        } else if (Pattern.matches("\u22a4|⊥|\\w*", input)) {
            result.nodeValue = input;
            result.childNum = 0;
            return result;
        } else if (input == null) {
            System.err.println("输入为空");
            return null;
        }
        System.err.println(input + " 的输入不合法");
        return null;
    }

    private static String[] AU_or_EU_get_two_substring(String input) {
        String[] result = new String[2];//左子字符串和右子字符串
        int bracketCounter = 0;
        for (int i = 0; i < input.length(); i++) {
            char element = input.charAt(i);
            if (element == 'U') {
                if (bracketCounter == 0) {
                    result[0] = input.substring(0, i);
                    result[1] = input.substring(i + 1, input.length() - 1);
                    break;
                }
            } else if (element == '(' || element == '[') {
                bracketCounter++;
            } else if (element == ')' || element == ']') {
                bracketCounter--;
            }
        }
        return result;
    }

    private static String get_string_between_brackets(String input) {
        int counter = 1;
        for (int i = 1; i < input.length(); i++) {
            char element = input.charAt(i);
            if (element == '(') {
                counter++;
            } else if (element == ')') {
                counter--;
                if (counter == 0) {
                    return input.substring(1, i);
                }
            }
        }
        return null;
    }

    public static void SemanticTreeToDOT(SemanticTree input) {
        gv = new GraphViz();
        if (input == null) {
            System.out.println("CTL语义树为空");
        } else {

        }

        gv.addln(gv.start_graph());

        DFS(input, 0);

        gv.addln(gv.end_graph());
//        System.out.println(gv.getDotSource());
        String type = "svg";

//		File out = new File("C:\\Users\\cde\\Desktop\\DotOutput." + type);
        File outDest = null;
        try {
            String path = ResourceUtils.getURL("classpath:").getPath();
            outDest = new File(path.replace("%20", " "), "static/images/DotOutput2." + type);
            System.out.println("DotOutput2.svg在"+outDest.getAbsolutePath()+"产生");
//			copyFileUsingFileChannels(out,outDest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(gv.getDotSource());
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, GraphViz.DOT), outDest);
    }

    private static void DFS(SemanticTree root, int currentNodeNum) {
        if (root != null) {
            gv.add(DOT_Node_counter + "[label=\"" + root.nodeValue + "\"];\n");
            if (root.leftChild != null) {
                DOT_Node_counter++;
                currentNodeNum++;
                DFS(root.leftChild, currentNodeNum);
                gv.add((currentNodeNum - 1) + "->" + currentNodeNum + ";\n");
                currentNodeNum--;
            }
            if (root.rightChild != null) {
                int tempCurrentNodeNum = currentNodeNum;

                DOT_Node_counter++;
                currentNodeNum = DOT_Node_counter;
                DFS(root.rightChild, currentNodeNum);

                gv.add(tempCurrentNodeNum + "->" + currentNodeNum + ";\n");
                currentNodeNum = tempCurrentNodeNum;
            }
        }
    }

        private static void copyFileUsingFileChannels (File source, File dest)
			throws IOException {
            FileChannel inputChannel = null;
            FileChannel outputChannel = null;
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(source);
                fos = new FileOutputStream(dest);
                inputChannel = fis.getChannel();
                outputChannel = fos.getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            } finally {
                fis.close();
                fos.close();
                inputChannel.close();
                outputChannel.close();
            }
        }

        //涉及到的一些操作符:\u22a4(T) ⊥ ﹁ ^ v →
//	public static void main(String[] args) {
//		SemanticTree tree1 = new SemanticTree("au");
//		tree1.leftChild = new SemanticTree("ax");
//		tree1.leftChild.leftChild = new SemanticTree("not");
//		tree1.leftChild.rightChild = new SemanticTree("not-right");
//		tree1.leftChild.leftChild.leftChild = new SemanticTree("p");
//		tree1.rightChild = new SemanticTree("eu");
//		tree1.rightChild.leftChild = new SemanticTree("ex");
//		tree1.rightChild.rightChild = new SemanticTree("not");
//		SemanticTreeToDOT(tree1);

//		CTL_Formula = "A[AX﹁pUE[EX(p^q)U﹁p]]";
//		SemanticTreeToDOT(CTL_formula_parser("A[AX﹁pUE[EX(p^q)U﹁p]]"));
//	}

    }