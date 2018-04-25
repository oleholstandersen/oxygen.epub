package dk.nota.dcs;

public class DcsException extends Exception {
	
	public DcsException(String string) {
		super(string);
	}
	
	public DcsException(Throwable throwable) {
		super(throwable);
	}
	
	public DcsException(String string, Throwable throwable) {
		super(string, throwable);
	}

}
