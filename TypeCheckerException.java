@SuppressWarnings("serial")
public class TypeCheckerException extends Exception {

	public TypeCheckerException() {
	}

	public TypeCheckerException(String message) {
		super(message);
	}

	public TypeCheckerException(Throwable cause) {
		super(cause);
	}

	public TypeCheckerException(String message, Throwable cause) {
		super(message, cause);
	}

}