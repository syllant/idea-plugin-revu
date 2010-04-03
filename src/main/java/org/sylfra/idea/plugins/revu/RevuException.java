package org.sylfra.idea.plugins.revu;

/**
 * General exception related to Revu error
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuException extends Exception
{
  public RevuException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RevuException(Throwable cause)
  {
    super(cause);
  }

  public RevuException(String message)
  {
    super(message);
  }
}
