package com.alpen.finalwork.service;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.alpen.finalwork.BooleanExpression.PolishNotation.*;

public class CircuitCheckService {

    private static int situationNum;//用于统计序号
    private static int counter;//用于递归生成真值表函数的计数
    private static ArrayList<int[]> rows;//用于存放最后的输出（其中的int[]的元素个数比变量个数多，多了序号列和若干输出列）
    private static int inputNum = 0;//变量个数
    private static int finalRowElementNum = 0;//最终每一列的元素个数
    private static int[] finalRow;//最终每一列包含的元素
    private static BDD[] circuitOutputBDDs;//几个输出变量的BDD
    private static HashMap<String, BDD>[] outputFixedBDDList;//专门存放若干输出的BDD与其变量对应的字符串
    private static String[] specialProcess;//将输入变量排序后得到的数组

    public static ArrayList<String[]> getHeads(String input1, String input2) {
        ArrayList<String[]> heads = new ArrayList<>();
        StringBuffer headerElement = new StringBuffer("");

        //注意，下面几行代码是对输入进行特殊处理，因为在求布尔表达式的函数中用到了keyset()，因此需要排序一下
        specialProcess = input1.split("\\s+");
        Arrays.sort(specialProcess);
        StringBuffer specialResult = new StringBuffer("");
        for (String element : specialProcess) {
            specialResult.append(element + " ");
        }

        headerElement.append("序号 " + specialResult.toString() + " ");
        for (String element : input2.split(";")) {
            headerElement.append(element.trim().split("=")[0] + " ");
        }
        String[] onlyElement = headerElement.toString().split("\\s+");

        inputNum = input1.split("\\s+").length;
        finalRowElementNum = onlyElement.length;

        heads.add(onlyElement);//表头，只能有一列元素
        return heads;
    }

    public static ArrayList<int[]> getRows(String input2) {//接收第二个输入框的输入
        rows = new ArrayList<>();
        finalRow = new int[finalRowElementNum];
        counter = 0;
        situationNum = 0;

        String[] inputSplit = input2.split(";");
        int bddNum = inputSplit.length;
        circuitOutputBDDs = new BDD[bddNum];
        outputFixedBDDList = new HashMap[bddNum];
        for (int i = 0; i < bddNum; i++) {
            circuitOutputBDDs[i] = stringToBDD(inputSplit[i].split("=")[1].trim());
            outputFixedBDDList[i] = copyFixedBDDList();
        }

        getCombinations(finalRow);//再次注意，finalRow的长度=1(序号)+输入变量个数+输出变量个数

        return rows;
    }

    private static BDD stringToBDD(String expressionStr) {
        List<String> middleOrderExpression = toMiddleOrderExpression(expressionStr);
        List<String> reversePolishExpression = toReversePolishExpression(middleOrderExpression);
        handleRepeatedVar();
        return getBddDotOutput(reversePolishExpression);
    }

    private static void getCombinations(int[] input) {
        counter++;//注意这里finalRow[]下标从1开始，因为要留出situationNum的位置
        for (int i = 0; i < 2; i++) {
            if (counter <= inputNum) {
                input[counter] = i;
                getCombinations(input);
            } else {
                counter--;
                situationNum++;
                input[0] = situationNum;
                //此时已经生成到了最后一个变量，需要求输出的值并加在finalRow[]的最后
                for (int j = counter + 1; j < finalRowElementNum; j++) {
                    input[j] = getBDDResult(circuitOutputBDDs[j - counter - 1], Arrays.copyOfRange(input,1,counter+1), j - counter - 1);
                }
                rows.add(Arrays.copyOf(input, input.length));//注意这里的finalRow会一直变，所以要复制一份拷贝放到ArrayList中
                return;
            }
        }
        counter--;//非常重要，不能遗漏
    }

    //大概思路：现在表格中的变量的顺序应该是已经排序好了的字典序，现在需要看看这个BDD里的编序是从0开始的
    //还是从1开始的，从1开始说明布尔表达式包含not。然后遍历一下第二个参数input中的值，调用restrict()函数，最后判断真假即可。
    /**
     * @param bdd    要求值的BDD
     * @param input  输入参数的取值
     * @param number 应该在outputFixedBDDList的第几个hashmap中找restrict()的参数
     * @return BDD最终的值
     */
    private static int getBDDResult(BDD bdd, int[] input, int number) {
        if (bdd.var() == 0 || bdd.var() == 1) {//编序从0开始，说明布尔表达式不含not
            for (int i = 0; i < specialProcess.length; i++) {//specialProcess.length应该等于input.length
                if (outputFixedBDDList[number].keySet().contains(specialProcess[i])) {//这里很重要，这是因为有可能输出变量并不会包含所有的输入变量，因此要判断是否在keyset中
                    if (input[i] == 1) {
                        bdd = bdd.restrict(findBddByString(specialProcess[i], number));
                    } else if (input[i] == 0) {
                        bdd = bdd.restrict(findBddByString(specialProcess[i], number).not());
                    } else {
                        System.err.println("输入变量的取值有问题，为"+input[i]);
                        return -1;
                    }
                }
            }
        } else {
            System.err.println("布尔表达式的编序有问题");
            return -1;
        }
        BDDFactory tempFactory = BDDFactory.init(16, 16);
        if (bdd.equals(tempFactory.one())) {
            return 1;
        } else if (bdd.equals(tempFactory.zero())) {
            return 0;
        } else {
            System.err.println("输出的BDD未得到最终结果");
            return -1;
        }
    }

    public static BDD findBddByString(String input, int number) {
        return outputFixedBDDList[number].get(input);
    }
}
