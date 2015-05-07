package org.nullbool.api.analysis;

/**
 * @author Bibl (don't ban me pls)
 * @created 4 May 2015
 */
public class AnalysisException extends Exception {

	private static final long serialVersionUID = -2771200719043216335L;

	public AnalysisException(String msg) {
		super(msg);
	}

	public AnalysisException(Exception e) {
		super(e);
	}

	public AnalysisException(String msg, Exception e) {
		super(msg, e);
	}
}