package org.sbmlsqueezer.io;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * 
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 * 
 */
public class String2TeX {

	public StringBuffer getEquation(String equation) {
		int i = 0;
		equation = equation.replaceAll("_", "");
		String newEquation = "";
		while (i < equation.length()) {
			if (equation.charAt(i) == '*')
				newEquation += "\\cdot ";
			else
				newEquation += equation.charAt(i);
			i++;
		}
		equation = newEquation.replaceAll("  ", " ");
		while (equation.contains("/")) {
			equation = bruch(equation);
		}

		while (equation.contains("^(")) {
			equation = hoch(equation);
		}

		return new StringBuffer(equation);
	}

	public String getOldEquation(PluginModel model, PluginReaction reaction) {
		String equation = reaction.getKineticLaw().getFormula();

		int reactionNum = 0;
		while ((reactionNum < model.getNumReactions())
				&& (!model.getReaction(reactionNum).getId().equals(
						reaction.getId())))
			reactionNum++;

		return equation;
	}

	private String bruch(String equation) {
		String sub1;
		String sub2;
		String sub3_1 = "";
		String sub4_1 = "";
		String sub3 = null;
		String sub4 = null;

		int i;
		i = equation.indexOf("/");
		// i = equation.lastIndexOf("/");
		sub1 = equation.substring(0, i);
		sub2 = equation.substring((i + 1));
		// System.out.println("sub1:" + sub1);
		// System.out.println("sub2:" + sub2);

		// zaehler
		while (sub1.charAt(i - 1) == ' ') {
			i = i - 1;
			sub1 = sub1.substring(0, i);
		}
		// System.out.println("-----------------" + sub1.charAt(i - 1));
		if (sub1.charAt(i - 1) == ')') {
			// System.out.println("z82: endlich----------------------------");
			int count = 0;
			int count1 = -1;
			// System.out.println("z84: count: " + count);
			// System.out.println("z85: count1: " + count1);
			for (int j = (i - 2); j >= 0; j--) {
				if (sub1.charAt(j) == '(') {
					count1 = count1 + 1;
					// System.out.println("z89: count1" + count1);
					if (count == 0 && count1 == 0) {
						// System.out.println("z94: count" + count);
						// System.out.println("z95: count1" + count1);
						sub3 = sub1.substring(j + 1, i - 1);
						// System.out.println("z90: sub3" + sub3);
						if (j != 0)
							sub3_1 = sub1.substring(0, j);
						// System.out.println("z93: sub3_1" + sub3_1);
						break;
					}
					if (count != 0) {
						count = count - 1;
						// System.out.println("z105: count" + count);
						// System.out.println("z106: count1" + count1);
					}
				}
				if (sub1.charAt(j) == ')') {

					count = count + 1;
					count1 = count1 - 1;
					// System.out.println("z113: count" + count);
					// System.out.println("z114: count1" + count1);
				}
			}
		} else {
			for (int j = (i - 2); j >= 0; j--) {
				if (sub1.charAt(j) == '+' || sub1.charAt(j) == '-'
						|| sub1.charAt(j) == '*' || sub1.charAt(j) == '/'
						|| sub1.charAt(j) == '(') {
					// System.out.println("z109: j: " + j);

					sub3 = sub1.substring(j + 1);
					sub3_1 = sub1.substring(0, j + 1);
					// System.out.println("z113: sub3: " + sub3);
					// System.out.println("z114: sub3_1: " + sub3_1);
					break;
				}
				if (j == 0)
					sub3 = sub1.substring(0);

			}
		}
		sub3 = sub3_1 + "\\frac{" + sub3 + "}{";
		// System.out.println("z122: sub3: " + sub3);
		// Nenner
		while (sub2.charAt(0) == ' ')
			sub2 = sub2.substring(1);
		if (sub2.charAt(0) == '(') {
			// System.out.println("z123: sub2: ");
			int count = 0;
			for (int j = 1; j < sub2.length(); j++) {
				// System.out.println("z126: j: " + j);
				if (sub2.charAt(j) == ')') {
					if (count == 0) {
						sub4 = sub2.substring(0, j + 1);
						// System.out.println("z139:sub4: " + sub4);
						if (j != (sub2.length() - 1)) {
							sub4_1 = sub2.substring(j + 1);
							// System.out.println("z143:sub4_1: " + sub4_1);
						}
						break;
					} else
						count = count - 1;
				}
				if (sub2.charAt(j) == '(')
					count = count + 1;
			}
		} else {
			for (int j = 0; j < sub2.length(); j++) {
				// System.out.println("z168: sub2: else");
				if (sub2.charAt(j) == '+' || sub2.charAt(j) == '-'
						|| sub2.charAt(j) == '*' || sub2.charAt(j) == '/'
						|| sub2.charAt(j) == ')') {
					sub4 = sub2.substring(0, j);
					// System.out.println("z172: sub4: " + sub4);
					if (j != (sub2.length() - 1)) {
						if (sub2.charAt(j) == ')')
							sub4_1 = sub2.substring(j);
						else
							sub4_1 = sub2.substring(j);
					}
					// System.out.println("z175: sub4_1: " + sub4_1);
					break;
				}
				if (j == (sub2.length() - 1))
					sub4 = sub2.substring(0);
			}
		}
		sub4 = sub4 + "}" + sub4_1;

		equation = sub3 + sub4;
		// System.out.println(equation);
		return equation;
	}

	private String hoch(String equation) {
		String sub;
		int c = equation.indexOf("^(");
		sub = equation.substring(0, c + 1); // vorstring mit ^
		// System.out.println("dd: " + sub);
		sub = sub + "{";
		int count = 0;
		for (int j = (c + 1); j < equation.length(); j++) {
			if (equation.charAt(j) == ')') {
				if (count == 0) {
					sub = sub + equation.substring(c + 2, j) + "}";
					equation = sub + equation.substring(j + 1);
					break;
				} else
					count = count - 1;
			}
			if (equation.charAt(j) == ')')
				count = count + 1;
		}
		return equation;
	}

}
