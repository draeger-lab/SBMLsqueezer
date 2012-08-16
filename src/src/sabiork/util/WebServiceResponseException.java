package sabiork.util;

/**
 * A class of exceptions that occurred during the processing of HTTP requests.
 * 
 * @author Matthias Rall
 */
@SuppressWarnings("serial")
public class WebServiceResponseException extends Exception {

	int responseCode;

	public WebServiceResponseException(int responseCode) {
		super();
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(String message, Throwable cause,
			int responseCode) {
		super(message, cause);
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(String message, int responseCode) {
		super(message);
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(Throwable cause, int responseCode) {
		super(cause);
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return responseCode;
	}

}
