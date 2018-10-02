package br.com.swing.application.exception;

public class RequiredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RequiredException(String msg) {
		super(msg);
	}

}
