package com.alpen.finalwork.functions;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class BDDFunctions {
	public static boolean getBDDResult(BDD bdd, int[] input) {
		for (int i = 0; i < input.length; i++) {
			int j = input[i];
			if (j == 1) {
				bdd = bdd.high();
			} else if (j == 0) {
				bdd = bdd.low();
			} else {
				System.out.println("The input is invalid...");
				return false;
			}
		}
		if (bdd.isOne()) {
			return true;
		} else if (bdd.isZero()) {
			return false;
		} else {
			System.out.println("The input is not enough to get result...");
			return false;
		}
	}
}
