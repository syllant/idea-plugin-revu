package org.sylfra.idea.plugins.revu.settings.app;

import com.intellij.util.ui.UIUtil;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.settings.IRevuSettings;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

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
  private String tableSelectionBackgroundColor;
  private String tableSelectionForegroundColor;

  public RevuAppSettings()
  {
    issueStatusColors = new HashMap<IssueStatus, String>(IssueStatus.values().length);
    issueStatusColors.put(IssueStatus.TO_RESOLVE, "#EFB7A4");
    issueStatusColors.put(IssueStatus.RESOLVED, "#B7DFB5");
    issueStatusColors.put(IssueStatus.REOPENED, "#EF8059");
    issueStatusColors.put(IssueStatus.CLOSED, "#BFBFBF");

    tableSelectionBackgroundColor = RevuUtils.getHex(UIUtil.getTableSelectionBackground());
    tableSelectionForegroundColor = RevuUtils.getHex(UIUtil.getTableSelectionForeground());
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

  public String getTableSelectionBackgroundColor()
  {
    return tableSelectionBackgroundColor;
  }

  public void setTableSelectionBackgroundColor(String tableSelectionBackgroundColor)
  {
    this.tableSelectionBackgroundColor = tableSelectionBackgroundColor;
  }

  public String getTableSelectionForegroundColor()
  {
    return tableSelectionForegroundColor;
  }

  public void setTableSelectionForegroundColor(String tableSelectionForegroundColor)
  {
    this.tableSelectionForegroundColor = tableSelectionForegroundColor;
  }
}