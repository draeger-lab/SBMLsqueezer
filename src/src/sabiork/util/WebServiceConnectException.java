package sabiork.util;

/**
 * A class of exceptions produced by failed web service connections.
 * 
 * @author Matthias Rall
 */
@SuppressWarnings("serial")
public class WebServiceConnectException extends Exception {

	public WebServiceConnectException() {
		super();
	}

	public WebServiceConnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public WebServiceConnectException(String message) {
		super(message);
	}

	public WebServiceConnectException(Throwable cause) {
		super(cause);
	}

}
