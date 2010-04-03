package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.IssueBrowsingPane;
import org.sylfra.idea.plugins.revu.ui.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.multichooser.MultiChooserPopup;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class SelectColumnsAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    final Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    final IssueBrowsingPane currentBrowsingPane = project.getComponent(RevuToolWindowManager.class)
      .getSelectedReviewBrowsingForm();
    if (currentBrowsingPane == null)
    {
      return;
    }

    MultiChooserPopup<IssueColumnInfo> popup = new MultiChooserPopup<IssueColumnInfo>(project,
      RevuBundle.message("browsing.table.selectColumns.text"),
      RevuPlugin.PLUGIN_NAME + ".ColumnsChooser",
      new MultiChooserPopup.IPopupListener<IssueColumnInfo>()
      {
        public void apply(@NotNull List<IssueColumnInfo> markedElements)
        {
          currentBrowsingPane.getIssueTable().setColumnInfos(toArray(markedElements));

          RevuWorkspaceSettingsComponent workspaceSettingsComponent =
            project.getComponent(RevuWorkspaceSettingsComponent.class);
          RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();

          workspaceSettings.setBrowsingColNames(IssueColumnInfoRegistry.getColumnNames(markedElements));
          workspaceSettingsComponent.loadState(workspaceSettings);
        }
      },
      new MultiChooserPopup.IItemRenderer<IssueColumnInfo>()
      {
        public String getText(IssueColumnInfo item)
        {
          return item.getName();
        }
      });

    Component owner = (Component) e.getInputEvent().getSource();
    popup.show(owner, false,
      Arrays.asList(IssueColumnInfoRegistry.ALL_COLUMN_INFOS),
      asList(currentBrowsingPane.getIssueTable().getListTableModel().getColumnInfos()));
  }

  private List<IssueColumnInfo> asList(@NotNull ColumnInfo[] columnInfos)
  {
    List<IssueColumnInfo> result = new ArrayList<IssueColumnInfo>(columnInfos.length);
    for (ColumnInfo columnInfo : columnInfos)
    {
      result.add((IssueColumnInfo) columnInfo);
    }

    return result;
  }

  private ColumnInfo[] toArray(@NotNull List<IssueColumnInfo> columnInfos)
  {
    ColumnInfo[] result = new IssueColumnInfo[columnInfos.size()];
    for (int i = 0; i < columnInfos.size(); i++)
    {
      result[i] = columnInfos.get(i);
    }

    return result;
  }
}
