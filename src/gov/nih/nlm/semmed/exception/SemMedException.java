/**
 * 
 */
package gov.nih.nlm.semmed.exception;

/**
 * @author hkilicoglu
 *
 */
public class SemMedException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SemMedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SemMedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SemMedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SemMedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
}
