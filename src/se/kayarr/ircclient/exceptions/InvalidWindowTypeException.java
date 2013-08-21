package se.kayarr.ircclient.exceptions;

public class InvalidWindowTypeException extends Exception {
	private static final long serialVersionUID = 349329997083191470L;

	public InvalidWindowTypeException() {
		super();
	}
	
	public InvalidWindowTypeException(String message) {
		super(message);
	}
}