package org.sylfra.idea.plugins.revu.settings.app;

import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

import java.util.HashMap;
import java.util.Map;

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
  private Map<IssueStatus, String> issueStatusColors;

  public RevuAppSettings()
  {
    issueStatusColors = new HashMap<IssueStatus, String>(IssueStatus.values().length);
    issueStatusColors.put(IssueStatus.TO_RESOLVE, "#EFB7A4");
    issueStatusColors.put(IssueStatus.RESOLVED, "#B7DFB5");
    issueStatusColors.put(IssueStatus.REOPENED, "#EF8059");
    issueStatusColors.put(IssueStatus.CLOSED, "#BFBFBF");
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

  public Map<IssueStatus, String> getIssueStatusColors()
  {
    return issueStatusColors;
  }

  public void setIssueStatusColors(Map<IssueStatus, String> issueStatusColors)
  {
    this.issueStatusColors = issueStatusColors;
  }
}