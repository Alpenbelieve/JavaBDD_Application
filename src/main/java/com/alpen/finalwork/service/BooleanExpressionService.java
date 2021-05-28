package com.alpen.finalwork.service;

import com.alpen.finalwork.BooleanExpression.PolishNotation;
import net.sf.javabdd.BDD;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import static com.alpen.finalwork.BooleanExpression.PolishNotation.*;

public class BooleanExpressionService {


    public static String boolExCalculate(String expressionStr){
        PolishNotation notation = new PolishNotation();
        List<String> middleOrderExpression = notation.toMiddleOrderExpression(expressionStr);
        List<String> reversePolishExpression = notation.toReversePolishExpression(middleOrderExpression);
        //处理输入的布尔函数包含重复变量的情况
        handleRepeatedVar();

		ByteArrayOutputStream baoStream = new ByteArrayOutputStream(100);
		PrintStream cacheStream = new PrintStream(baoStream);
		PrintStream oldStream = System.out;
		System.setOut(cacheStream);

        BDD bddOutput = getBddDotOutput(reversePolishExpression);
        bddOutput.printSet();

        String message = baoStream.toString();
        String result = message.substring(message.indexOf("^")+1).trim();
		System.setOut(oldStream);
		System.out.println(result);

		return printSetBeautify(result);
    }

    public static String printSetBeautify(String input){
        if (!input.contains("<")){
            return "该表达式恒为假，没有取值为真的组合";
        }
        String[] slices = input.split("\\|");
        int number = slices.length;
        HashMap<Integer,String> numWithString = new HashMap<>();
        for (int i=0;i<number-1;i++){
            String[] element = slices[i].split("=");
            numWithString.put(Integer.parseInt(element[0]),element[1]);
        }
        String printSetResult = slices[number-1];
        String[] solutions = printSetResult.split("<|><|>");
        int counter = 0;
        StringBuffer buffer = new StringBuffer("");
        for (String element:solutions){
            if (element.length()>0){
                counter++;
                buffer.append("情况"+counter+" : ");
                String[] tempList = element.split(", ");
                for (String value:tempList){
                    String[] temp = value.split(":");
                    buffer.append(numWithString.get(Integer.parseInt(temp[0]))+"="+temp[1]+"　");
                }
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }
}
