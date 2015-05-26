package org.nullbool.api.obfuscation.cfg;

/**
 * @author Bibl (don't ban me pls)
 * @created 26 May 2015
 */
public class SwitchTest {

	private static final int mmm = 5918349;
	
	public void test() {
		int k = mmm * 10000;
		
		if(k == 9) {
			System.out.println("a0");
		} else {
			System.out.println("a1");
		}
		
		switch(k) {
			case 1:
				System.out.println("no");
				break;
			case 3:
				System.out.println("ya");
				break;
			case 9:
			case 10:
			case 100:
				System.out.println("dub");
				break;
			default:
				System.out.println("def");
		}
		
		System.out.println("fallth");
	}
}