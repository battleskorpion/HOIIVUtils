package hoi4utils.clausewitz_code.scope;

public class NotPermittedWithinScopeException extends Exception {
	public NotPermittedWithinScopeException() {
		super();
	}

	public NotPermittedWithinScopeException(String message) {
		super(message);
	}

	public NotPermittedWithinScopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotPermittedWithinScopeException(Throwable cause) {
		super(cause);
	}
}
