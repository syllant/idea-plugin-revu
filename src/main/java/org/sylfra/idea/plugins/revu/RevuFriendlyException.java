package org.sylfra.idea.plugins.revu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * General exception related to Revu error
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuFriendlyException extends RevuException
{
  private final String htmlTitle;
  private final String htmlDetails;

  public RevuFriendlyException(@NotNull String message, @Nullable String htmlTitle, @NotNull String htmlDetails,
    @Nullable Throwable cause)
  {
    super(message, cause);
    this.htmlTitle = htmlTitle;
    this.htmlDetails = htmlDetails;
  }

  public RevuFriendlyException(@NotNull String message, @NotNull String htmlDetails)
  {
    this(message, null, htmlDetails, null);
  }

  public String getHtmlTitle()
  {
    return htmlTitle;
  }

  public String getHtmlDetails()
  {
    return htmlDetails;
  }
}