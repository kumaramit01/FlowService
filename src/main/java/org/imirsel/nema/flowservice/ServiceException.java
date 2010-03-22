package org.imirsel.nema.flowservice;

/**
 * Signals that a problem has occurred while performing some operation.
 * 
 * @author shirk
 * @since 0.6.0
 */
public class ServiceException extends RuntimeException {

   /**
    * The version of this class.
    */
   private static final long serialVersionUID = 7767483495396581519L;

   /**
    * Creates a new instance.
    */
   public ServiceException() { }

   /**
    * Creates a new instance with the specified message.
    * 
    * @param message Exception message
    */
   public ServiceException(String message) {
      super(message);
   }

   /**
    * Creates a new instance with the specified cause.
    * 
    * @param cause Cause of this exception.
    */
   public ServiceException(Throwable cause) {
      super(cause);
   }

   /**
    * Creates a new instance with the specified message and cause.
    * 
    * @param message Exception message.
    * @param cause Cause of this exception.
    */
   public ServiceException(String message, Throwable cause) {
      super(message, cause);
   }

}
