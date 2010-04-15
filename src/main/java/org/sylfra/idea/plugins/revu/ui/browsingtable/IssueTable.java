package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.ide.DataManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTable extends TableView<Issue> implements DataProvider, OccurenceNavigator
{
  private final Project project;
  @Nullable
  private final Review review;
  private String filterValue;

  public IssueTable(@NotNull Project project, @NotNull List<Issue> issues, @Nullable Review review)
  {
    super(new IssueTableModel(project, issues, review));
    this.project = project;
    this.review = review;

    setSizes();

    installListeners();
  }

  public IssueTableModel getIssueTableModel()
  {
    return (IssueTableModel) super.getListTableModel();
  }

  private void installListeners()
  {
    PopupHandler.installPopupHandler(this, "revu.issueTable.popup", "issueTable");

    // Double-Click
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          AnAction action = ActionManager.getInstance().getAction("revu.JumpToSource");
          DataContext context = DataManager.getInstance().getDataContext(IssueTable.this);

          Presentation presentation = (Presentation)action.getTemplatePresentation().clone();

          AnActionEvent event = new AnActionEvent(e, context, "issueTable", presentation,
            ActionManager.getInstance(), 0);

          action.update(event);

          if (event.getPresentation().isEnabled())
          {
            action.beforeActionPerformedUpdate(event);
            action.actionPerformed(event);
          }
        }
      }
    });

    // Background colors
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings settings)
      {
        repaint();
      }
    });
  }

  public void filter(@NotNull String filterValue)
  {
    this.filterValue = filterValue;
    getTableViewModel().fireTableDataChanged();
    getIssueTableModel().filter(filterValue);
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column)
  {
    return new TableCellRenderer()
    {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column)
      {
        JComponent result = (JComponent) IssueTable.super.getCellRenderer(row, column).getTableCellRendererComponent(
          table, value, isSelected, false, row, column);

        // Filter
        if (filterValue != null)
        {
          // Should always be true
          if (result instanceof JLabel)
          {
            JLabel jLabel = (JLabel) result;
            String newValue = SearchUtil.markup(jLabel.getText(), filterValue);
            if (!newValue.startsWith("<html>"))
            {
              newValue = "<html>" + newValue + "</html";
            }

            jLabel.setText(newValue);
          }
        }

        // Background
        if (isSelected)
        {
          RevuAppSettings appSettings = RevuUtils.getAppSettings();
          result.setBackground(Color.decode(appSettings.getTableSelectionBackgroundColor()));
          result.setForeground(Color.decode(appSettings.getTableSelectionForegroundColor()));
        }
        else
        {
          Issue issue = getIssueTableModel().getIssue(convertRowIndexToModel(row));
          result.setBackground(RevuUtils.getIssueStatusColor(issue.getStatus()));
        }

        return result;
      }
    };
  }

  public Object getData(@NonNls String dataId)
  {
    if (PlatformDataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId))
    {
      Issue currentIssue = getSelectedObject();
      if ((currentIssue != null) && (currentIssue.getFile() != null))
      {
        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, currentIssue.getFile(),
          currentIssue.getLineStart(), 0);
        return new Navigatable[]{fileDescriptor};
      }

      return null;
    }

    if (RevuDataKeys.ISSUE.getName().equals(dataId))
    {
      return getSelectedObject();
    }

    if (RevuDataKeys.ISSUE_LIST.getName().equals(dataId))
    {
      return getSelection();
    }

    if (RevuDataKeys.REVIEW.getName().equals(dataId))
    {
      // Dont return 'review' since it's null in all tab
      Issue issue = getSelectedObject();
      return (issue == null) ? null : review;
    }

    return null;
  }

  public JComponent buildFilterComponent()
  {
    return new FilterComponent("revuTableFilter", 5)
    {
      public void filter()
      {
        IssueTable.this.filter(getFilter().toLowerCase());
      }
    };
  }

  public void setColumnInfos(ColumnInfo[] columnInfos)
  {
    getListTableModel().setColumnInfos(columnInfos);
    setSizes();
  }

  public Color getRowBackground(@NotNull Issue issue)
  {
    return null;
  }

  // IDEA bug: com.intellij.ui.table.TableView#setSizes() not called when updating cols
  private void setSizes()
  {
    ColumnInfo[] columns = getListTableModel().getColumnInfos();
    for (int i = 0; i < columns.length; i++)
    {
      IssueColumnInfo columnInfo = (IssueColumnInfo) columns[i];
      TableColumn column = getColumnModel().getColumn(i);

      int preferredWidth = columnInfo.getWidth(this);
      int minWidth = columnInfo.getMinWidth(this);
      int maxWidth = columnInfo.getMaxWidth(this);

      if (preferredWidth > 0)
      {
        column.setPreferredWidth(preferredWidth);
      }
      if (minWidth > 0)
      {
        column.setMinWidth(minWidth);
      }
      if (maxWidth > 0)
      {
        column.setMaxWidth(maxWidth);
      }
    }
  }

  public boolean hasNextOccurence()
  {
    int currentRow = getSelectedRow();
    return ((currentRow > -1) && (currentRow < getRowCount() - 1));
  }

  public boolean hasPreviousOccurence()
  {
    return (getSelectedRow() > 0);
  }

  public OccurenceNavigator.OccurenceInfo goNextOccurence()
  {
    return buildOccurenceInfo(1);
  }

  public OccurenceNavigator.OccurenceInfo goPreviousOccurence()
  {
    return buildOccurenceInfo(-1);
  }

  private OccurenceNavigator.OccurenceInfo buildOccurenceInfo(final int offset)
  {
    Navigatable navigatable = new Navigatable()
    {
      public void navigate(boolean requestFocus)
      {
        int currentRow = getSelectedRow();
        int newRow = currentRow + offset;
        getSelectionModel().setSelectionInterval(newRow, newRow);
      }

      public boolean canNavigate()
      {
        return true;
      }

      public boolean canNavigateToSource()
      {
        return true;
      }
    };
    return new OccurenceNavigator.OccurenceInfo(navigatable, getSelectedRow(), getRowCount());
  }

  public String getNextOccurenceActionName()
  {
    return RevuBundle.message("browsing.issues.next.description");
  }

  public String getPreviousOccurenceActionName()
  {
    return RevuBundle.message("browsing.issues.previous.description");
  }
}
