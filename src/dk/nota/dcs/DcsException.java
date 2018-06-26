package dk.nota.dcs;

import java.io.IOException;

public class DcsException extends IOException {
	
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