package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTable extends TableView<Issue> implements DataProvider, OccurenceNavigator
{
  private final Project project;
  @Nullable
  private final Review review;
  private String filterValue;

  public IssueTable(@NotNull Project project, @NotNull List<Issue> items, @Nullable Review review)
  {
    super(new IssueTableModel(project, items, review));
    this.project = project;
    this.review = review;

    installListeners();
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
          ActionUtil.execute("revu.JumpToSource", e, IssueTable.this, "issueTable", 0);
        }
      }
    });
  }

  public void filter(@NotNull String filterValue)
  {
    // Filter must contain at least 3 chars
    if ((filterValue.length() < 3) && (filterValue.length() > 0))
    {
      return;
    }

    this.filterValue = filterValue;
    getTableViewModel().fireTableDataChanged();
    ((IssueTableModel) getModel()).filter(filterValue);
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column)
  {
    if (filterValue == null)
    {
      return super.getCellRenderer(row, column);
    }

    return new TableCellRenderer()
    {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column)
      {
        Component result = IssueTable.super.getCellRenderer(row, column).getTableCellRendererComponent(table,
          value, isSelected, hasFocus, row, column);

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

        return result;
      }
    };
  }

  public Object getData(@NonNls String dataId)
  {
    if (PlatformDataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId))
    {
      Issue currentItem = getSelectedObject();
      if (currentItem != null)
      {
        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, currentItem.getFile(),
          currentItem.getLineStart(), 0);
        return new Navigatable[]{fileDescriptor};
      }

      return null;
    }

    if (RevuDataKeys.ISSUE.getName().equals(dataId))
    {
      return getSelectedObject();
    }

    if (RevuDataKeys.ISSUE_ARRAY.getName().equals(dataId))
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
    return RevuBundle.message("action.next.description");
  }

  public String getPreviousOccurenceActionName()
  {
    return RevuBundle.message("action.previous.description");
  }

}
