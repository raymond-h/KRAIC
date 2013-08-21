package se.kayarr.ircclient.exceptions;

public class WrongOwnerException extends Exception {
	private static final long serialVersionUID = -7180393892719808326L;
	
	public WrongOwnerException() {
		super();
	}
	
	public WrongOwnerException(String message) {
		super(message);
	}
}
