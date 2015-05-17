package org.nullbool.api.obfuscation.refactor.test;

public class RefactorTestClass {

	protected boolean booleanField;
	protected int intField;
	protected byte byteField;
	protected short shortField;
	protected double doubleField;
	protected float floatField;
	protected long longField;
	protected String stringField;
	protected String[] stringsField;
	protected int[][][] intsField;
	
	public RefactorTestClass() {
		booleanField = false;
		intField = 1;
		byteField = 1;
		shortField = 1;
		doubleField = 1;
		floatField = 1;
		longField = 1;
		stringField = new String("testString");
		stringsField = new String[]{"val1", "val2", "val3"};
		
		intsField = new int[5][5][5];
		intsField[0][0] = new int[100];
	}
	
	public void voidMethod(){}
	
	public int primitiveMethod(){return 1;}
	
	public void voidWithPrimitive(long param){}

	public double doubleParam(float f){return 1D;}
	
	public String stringMethod(){return "sakdjaksd";};
	
	public String[] stringss(){
		return new String[5];
	}
	
	public String[][][] stirngsss(){
		return new String[1][1][1];
	}
	
	public Object[][][] objjssss(){
		return new Object[1][1][1];
	}
	
	public int[][][] intsss(){
		return new int[1][1][1];
	}
	
	public String castTest(Object o){
		return (String) o;
	}
	
	public String[] arrCast(Object o){
		return (String[]) o;
	}
	
	public int intCastTest(Object o){
		if(o instanceof Float)
			return -1;
		
		return (int) o;
	}
	
	public int[] intsCastTest(Object o){
		if(o instanceof String[][])
			return null;
		return (int[]) o;
	}
}