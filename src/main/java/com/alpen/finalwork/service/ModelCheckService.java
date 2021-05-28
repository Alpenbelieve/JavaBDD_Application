package com.alpen.finalwork.service;

import com.alpen.finalwork.CTL_implement.SemanticTree;

import java.util.regex.Pattern;

import static com.alpen.finalwork.CTL_implement.CTL_implement_v2.*;
import static com.alpen.finalwork.CTL_implement.CTL_parser.*;

public class ModelCheckService {

    /**
     *
     * @param input1 布尔变量
     * @param input2 状态变量
     * @param input3 迁移关系
     * @param input4 CTL公式
     * @return 具体有哪几个状态满足公式，输出格式为“s0 s1 s2”
     */
    public static String getSatisfyingStates(String input1, String input2, String input3, String input4) {

        draw_CTL(input1,input2,input3);
        get_states_bdd();
        S_bdd = get_S_bdd();
        TRANS_bdd = get_TRANS_bdd();
        BDDPairingConfig();

        SemanticTree sTree = CTL_formula_parser(input4);
        //生成CTL公式的语义树
        SemanticTreeToDOT(sTree);
        //得到满足该CTL公式的状态集
        if (sTree==null){
            return "CTL公式有问题，无法被解析，请重新输入";
        }else{
            return extractStatesFromBDD(getSatStatesBySemanticTree(sTree)).toString();
        }
    }
}
