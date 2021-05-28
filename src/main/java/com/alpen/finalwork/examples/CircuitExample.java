package com.alpen.finalwork.examples;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class CircuitExample {
	public static void main(String[] args) {
		BDDFactory B;
		B = BDDFactory.init(100, 100);
		B.setVarNum(8);

		BDD x = B.ithVar(0);
		BDD y = B.ithVar(1);
		BDD ci = B.ithVar(2);
		BDD c0 = x.xor(y).and(ci).or(x.and(y));
		BDD s = x.xor(y).xor(ci);

		int xv = 1, yv = 1, cv = 1;
		System.out.println("X  Y  CI  C0  S");

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 2; k++) {
					BDD result = c0.restrict(x).restrict(y).restrict(ci);
					BDD result2 = s.restrict(x).restrict(y).restrict(ci);
					System.out.print(xv + "  " + yv + "   " + cv);
					if (result.isOne()) {
						System.out.print("   " + 1);
					} else if (result.isZero()) {
						System.out.print("   " + 0);
					}
					if (result2.isOne()) {
						System.out.println("  " + 1);
					} else if (result2.isZero()) {
						System.out.println("  " + 0);
					}
					ci = ci.not();
					cv = 1 - cv;
				}
				y = y.not();
				yv = 1 - yv;
			}
			x = x.not();
			xv = 1 - xv;
		}
	}
}
