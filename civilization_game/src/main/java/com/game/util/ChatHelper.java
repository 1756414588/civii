package com.game.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatHelper {

	/**
	 * 是否是中文
	 * 
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isCorrect(String correct) {
		String regEx = "[^`~!@#$%ㄒ^&*╮╭3ε≧▽≦‖｜￣∶丶·§№☆★ 	○●◎◇◆□■△▲※→←↑↓〓＃＆＠＼＾＿┌┍┎┏┐┑┒┓—┄┈├┝┞┟┠┡┢┣|┆┊┬┭┮┯┰┱┲┳┼┽┾┿╀╂╁╃≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∝∞∧∨∑∏∪∩∈∵∴⊥‖∠⌒⊙≌∽√ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ①②③④⑤⑥⑦⑧⑨⑩︻︼︽︾〒↑↓☉⊙●〇◎¤★☆■▓「」『』◆◇▲△▼▽◣◥◢◣◤ ◥№↑↓→←↘↙Ψ※㊣∑⌒∩【】〖〗＠ξζω□∮〓※》∏卐√ ╳々♀♂∞①ㄨ≡╬╭╮╰╯╱╲ ▂ ▂ ▃ ▄ ▅ ▆ ▇ █ ▂▃▅▆█ ▁▂▃▄▅▆▇█▇▆▅▄▃▂▁╰╯∩(（）)+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\p{P}a-zA-Z0-9\\u4e00-\\u9fa5]+";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(correct);
		if (m.find())
			return true;
		else
			return false;
	}

}
