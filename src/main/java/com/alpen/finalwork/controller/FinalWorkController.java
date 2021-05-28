package com.alpen.finalwork.controller;

import com.alpen.finalwork.service.BooleanExpressionService;
import com.alpen.finalwork.service.CircuitCheckService;
import com.alpen.finalwork.service.ModelCheckService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class FinalWorkController {

//    @Autowired
//    public BooleanExpressionService fwService;

    @RequestMapping("/index")
    public String index() {
        return "../static/index";
    }

    @RequestMapping("/1")
    public String one() {
        return "1";
    }

    @RequestMapping("/2")
    public String two() {
        return "2";
    }

    @RequestMapping("/3")
    public String three() {
        return "3";
    }

    @RequestMapping("/4")
    public String four() {
        return "4";
    }

    @RequestMapping(value = "/1/circuit-check",method = RequestMethod.POST)
    public String circuitCheck(Map<String,Object> map, HttpServletRequest request) {
        String input1 = request.getParameter("inputVariables");
        String input2 = request.getParameter("outputVariables");

        if (input1.trim().length()==0||input2.trim().length()==0){
            map.put("circuitResult","输入不完整，请重新输入");
            return "1";
        }
        System.out.println("接收输入："+input1+" 以及 "+input2);

        map.put("circuitResult","输入:"+input1+"  输出:"+input2);
//        ArrayList<String[]> heads = new ArrayList<>();
//        String[] temp = {"123","456","789","666666"};
//        heads.add(temp);
//        ArrayList<int[]> rows = new ArrayList<>();
//        int[] temp2 = {1,0,1,1};
//        int[] temp3 = {0,1,0,0};
//        rows.add(temp2);
//        rows.add(temp3);
        ArrayList<String[]> heads = CircuitCheckService.getHeads(input1,input2);
        ArrayList<int[]> rows = CircuitCheckService.getRows(input2);

        map.put("heads",heads);
        map.put("rows",rows);
        return "1";
    }

    @RequestMapping(value = "/2/boolean-expression",method = RequestMethod.POST)
    public String booleanExpression(Map<String,Object> map, HttpServletRequest request) {
        String expressionStr = request.getParameter("booleanExpression");

        if (expressionStr.trim().length()==0){
            map.put("booleanExpressionResult","输入为空，请重新输入");
            return "2";
        }
        System.out.println("接收输入："+expressionStr);

        String result = BooleanExpressionService.boolExCalculate(expressionStr);
        map.put("booleanExpressionResult","布尔表达式："+expressionStr+"\n"+result);
        return "2";
    }

    @RequestMapping(value = "/3/model-check",method = RequestMethod.POST)
    public String modelCheckExpression(Map<String,Object> map, HttpServletRequest request) {
        String input1 = request.getParameter("booleanVariables");
        String input2 = request.getParameter("stateVariables");
        String input3 = request.getParameter("transRelationship");
        String input4 = request.getParameter("ctlFormula");

        if (input1.trim().length()==0||input2.trim().length()==0||input3.trim().length()==0||input4.trim().length()==0){
            map.put("modelCheckResult","输入不完整，请重新输入");
            return "3";
        }
        System.out.println("接收输入："+input1+" 以及 "+input2+" 以及 "+input3+" 以及 "+input4);

        String satStates = ModelCheckService.getSatisfyingStates(input1,input2,input3,input4);

        map.put("modelCheckResult","布尔变量："+input1+"  状态变量："+input2+"\n迁移关系："+input3+"\nCTL公式："+input4);
        map.put("satStatesResult","满足 CTL 公式的状态集："+satStates);
        map.put("svgPath1","../../../../images/DotOutput1.svg");
        map.put("svgPath2","../images/DotOutput2.svg");
        return "3";
    }
}
