/*
 * @author Gautham Narayanasamy
 */
package exceptions;

/**
 * The Class ArchiveException.
 */
public class ArchiveException extends RuntimeException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new archive exception.
	 *
	 * @param e the e
	 */
	public ArchiveException(Throwable e) {
		super(e);
	}

	public ArchiveException(String msg) {
		super(msg);
	}
}
