package org.sylfra.idea.plugins.revu.settings.project.workspace;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuWorkspaceSettings implements IRevuSettings
{
  private boolean autoScrollToSource;

  public RevuWorkspaceSettings()
  {
  }

  public boolean isAutoScrollToSource()
  {
    return autoScrollToSource;
  }

  public void setAutoScrollToSource(boolean autoScrollToSource)
  {
    this.autoScrollToSource = autoScrollToSource;
  }
}