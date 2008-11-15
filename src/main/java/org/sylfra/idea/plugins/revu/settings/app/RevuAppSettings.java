package org.sylfra.idea.plugins.revu.settings.app;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuAppSettings implements IRevuSettings
{
  private String login;
  private String password;

  public RevuAppSettings()
  {
  }

  public String getLogin()
  {
    return login;
  }

  public void setLogin(String login)
  {
    this.login = login;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }
}