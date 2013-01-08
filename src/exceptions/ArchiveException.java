package exceptions;

public class ArchiveException extends RuntimeException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the property not found exception
	 * 
	 * @param propertyKey
	 *            Represents the invalid property key
	 */
	public ArchiveException(Throwable e) {
		super(e);
	}
}
