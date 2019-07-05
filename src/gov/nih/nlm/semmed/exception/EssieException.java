/**
 * 
 */
package gov.nih.nlm.semmed.exception;

/**
 * @author hkilicoglu
 *
 */
public class EssieException extends SemMedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public EssieException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public EssieException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EssieException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public EssieException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
