package dk.nota.oxygen.quickbase;

public class QuickbaseException extends Exception {

	public QuickbaseException(String string) {
		super(string);
	}

	public QuickbaseException(Throwable throwable) {
		super(throwable);
	}

	public QuickbaseException(String string, Throwable throwable) {
		super(string, throwable);
	}

}
