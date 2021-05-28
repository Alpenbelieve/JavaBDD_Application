package com.alpen.finalwork.BooleanExpression;


/**
 * @author :cde
 * @version V1.0
 * @ClassName: Operation
 * @Description:
 * @Date :2019/4/26 15:13
 * Modification  History:
 * Date         Author        Version        Discription
 * -----------------------------------------------------------------------------------
 * 2019/4/26       Administrator         1.0             1.0
 * Why & What is modified: 创建
 */
public class Operation {

    private static int NOTOP=10;//非
    
    private static int ANDOP=9;//与
    private static int NANDOP=9;//与非
    private static int DIFFOP=9;//P∧非Q
    private static int LESSOP=9;//非P∧Q
    
    private static int OROP=8;//或
    private static int NOROP=8;//或非

    private static int IMPOP=7;//蕴含, ->
    private static int INVIMPOP=7;//反蕴含, <-
    
    private static int BIIMPOP=6;//同或, <-> ,相同就是1,不同就是0
    private static int XOROP=6;//异或,相同就是0,不同就是1

    public static int getValue(String operation){
        int result;
        switch (operation){
            case "not":
                result=NOTOP;
                break;
            case "and":
                result=ANDOP;
                break;
            case "or":
                result=OROP;
                break;
            case "diff":
                result=DIFFOP;
                break;
            case "imp":
                result=IMPOP;
                break;
            case "biimp":
                result=BIIMPOP;
                break;
            case "invimp":
                result=INVIMPOP;
                break;
            case "less":
                result=LESSOP;
                break;
            case "nand":
                result=NANDOP;
                break;
            case "nor":
                result=NOROP;
                break;
            case "xor":
                result=XOROP;
                break;
            default:
//                System.out.println("不存在该运算符");
                result=0;
        }
        return result;
    }
}
