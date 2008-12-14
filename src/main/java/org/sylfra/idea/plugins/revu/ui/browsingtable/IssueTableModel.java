package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
public final class IssueTableModel extends ListTableModel<Issue> implements IIssueListener
{
  // ListTableModel does not expose issues list (#getIssues() provides a copy)
  private java.util.List<Issue> allItems;
  private java.util.List<Issue> visibleItems;

  public IssueTableModel(@NotNull Project project, @NotNull java.util.List<Issue> issues,
    @Nullable Review review)
  {
    super(retrieveColumnsFromSettings(project), issues, 0);
    visibleItems = issues;
    allItems = new ArrayList<Issue>(issues);

    setSortable(true);

    installListeners(project, review);
  }

  private void installListeners(final Project project, Review review)
  {
    if (review != null)
    {
      review.addIssueListener(this);
    }
    else
    {
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(new IReviewListener()
      {
        public void reviewChanged(Review review)
        {
        }

        public void reviewAdded(Review review)
        {
          allItems.addAll(review.getIssues());
          visibleItems.addAll(review.getIssues());
          resort(visibleItems);

          fireTableDataChanged();
          review.addIssueListener(IssueTableModel.this);
        }

        public void reviewDeleted(Review review)
        {
          review.removeIssueListener(IssueTableModel.this);
        }
      });
      Collection<Review> reviews = reviewManager.getReviews();
      for (Review aReview : reviews)
      {
        aReview.addIssueListener(this);
      }
    }

    project.getComponent(RevuWorkspaceSettingsComponent.class).addListener(new IRevuSettingsListener<RevuWorkspaceSettings>()
    {
      public void settingsChanged(RevuWorkspaceSettings settings)
      {
        List<String> currentColumnNames = IssueColumnInfoRegistry.getColumnNames(Arrays.asList(getColumnInfos()));
        if (!settings.getBrowsingColNames().equals(currentColumnNames))
        {
          setColumnInfos(retrieveColumnsFromSettings(project));
        }
      }
    });
  }

  @Override
  public void setItems(java.util.List<Issue> issues)
  {
    super.setItems(issues);
    visibleItems = issues;
    allItems = new ArrayList<Issue>(issues);
  }

  public void issueAdded(Issue issue)
  {
    allItems.add(issue);
    visibleItems.add(issue);

    resort(visibleItems);
    fireTableDataChanged();

    int index = visibleItems.indexOf(issue);
    fireTableRowsInserted(index, index);
  }

  public void issueDeleted(Issue issue)
  {
    allItems.remove(issue);

    int index = visibleItems.indexOf(issue);
    if (index > -1)
    {
      visibleItems.remove(issue);
      fireTableRowsDeleted(index, index);
    }
  }

  public void issueUpdated(Issue issue)
  {
    int index = visibleItems.indexOf(issue);
    if (index > -1)
    {
      fireTableRowsUpdated(index, index);
    }
  }

  // Already defined in ListTableModel, but private...
  private void resort(java.util.List<Issue> issues)
  {
    int sortedColumnIndex = getSortedColumnIndex();
    if ((sortedColumnIndex >= 0) && (sortedColumnIndex < IssueColumnInfoRegistry.ALL_COLUMN_INFOS.length))
    {
      final ColumnInfo columnInfo = IssueColumnInfoRegistry.ALL_COLUMN_INFOS[sortedColumnIndex];
      if (columnInfo.isSortable())
      {
        //noinspection unchecked
        columnInfo.sort(issues);
      }
    }
  }

  public void filter(@NotNull String filter)
  {
    visibleItems.clear();

    if (filter.length() == 0)
    {
      visibleItems.addAll(allItems);
    }
    else
    {
      for (Issue issue : allItems)
      {
        boolean match = false;
        for (ColumnInfo columnInfo : getColumnInfos())
        {
          IssueColumnInfo issueColumnInfo = (IssueColumnInfo) columnInfo;
          if (issueColumnInfo.matchFilter(issue, filter))
          {
            match = true;
            break;
          }
        }

        if (match)
        {
          visibleItems.add(issue);
        }
      }
    }
  }

  private static ColumnInfo[] retrieveColumnsFromSettings(@NotNull Project project)
  {
    RevuWorkspaceSettings workspaceSettings = project.getComponent(RevuWorkspaceSettingsComponent.class).getState();

    List<String> colNames = workspaceSettings.getBrowsingColNames();
    List<ColumnInfo> result = new ArrayList<ColumnInfo>(colNames.size());

    for (String colName : colNames)
    {
      IssueColumnInfo columnInfo = IssueColumnInfoRegistry.ALL_COLUMN_INFOS_BY_NAMES.get(colName);
      if (columnInfo != null)
      {
        result.add(columnInfo);
      }
    }
    
    return result.toArray(new ColumnInfo[result.size()]);
  }
}
