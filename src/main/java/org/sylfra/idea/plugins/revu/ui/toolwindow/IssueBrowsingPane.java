package org.sylfra.idea.plugins.revu.ui.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.CustomAutoScrollToSourceHandler;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssuePane;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTree;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTreeModel;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilter;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilterListener;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl.PriorityIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.EventObject;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueBrowsingPane implements Disposable
{
  private JPanel contentPane;
  private final Project project;
  private final Review review;
  private JComponent tbMain;
  private IssuePane issuePane;
  private JSplitPane splitPane;
  private JLabel lbMessage;
  private JComponent fullTextFilterComponent;
  private JLabel lbCount;
  private IssueTree issueTree;
  private JPanel pnIssuePaneContainer;
  private JComponent toolbar;
  private Splitter splitFilter;
  private IRevuSettingsListener<RevuAppSettings> appSettingsListener;
  private MessageClickHandler messageClickHandler;
  private IIssueListener issueListener;
  private IIssueTreeFilterListener issueTreeFilterListener;

  public IssueBrowsingPane(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;

    configureUI();

    installListeners();

    checkMessageInsteadOfPane();
    checkRowSelected();
  }

  public Review getReview()
  {
    return review;
  }

  private void createUIComponents()
  {
    issueTree = new IssueTree(project, review, new PriorityIssueTreeGrouper());
    issueTree.setSelectionModel(new DefaultTreeSelectionModel()
    {
      @Override
      public void setSelectionPaths(TreePath[] pPaths)
      {
        if (saveIfModified())
        {
          super.setSelectionPaths(pPaths);
          updateUI(false);
        }
      }
    });
    issueTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    TreeUtil.expandAll(issueTree);

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.issueBrowsingPane");

    toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).getComponent();
    //@todo
//    IssueTableModel tableModel = (IssueTableModel) issueTree.getListTableModel();
//    tableModel.addTableModelListener(new TableModelListener()
//    {
//      public void tableChanged(final TableModelEvent e)
//      {
//        if (e.getType() == TableModelEvent.DELETE)
//        {
//          issueTree.getSelectionModel().clearSelection();
//          checkMessageInsteadOfPane();
//          SwingUtilities.invokeLater(new Runnable()
//          {
//            public void run()
//            {
//              checkRowSelected();
//            }
//          });
//        }
//        else if (e.getType() == TableModelEvent.INSERT)
//        {
//          SwingUtilities.invokeLater(new Runnable()
//          {
//            public void run()
//            {
//              issueTree.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getFirstRow());
//              TableUtil.scrollSelectionToVisible(issueTree);
//              checkMessageInsteadOfPane();
//            }
//          });
//        }
//      }
//    });

    issuePane = new IssuePane(project, issueTree, false);

    RevuWorkspaceSettingsComponent workspaceSettingsComponent = project.getComponent(
      RevuWorkspaceSettingsComponent.class);

    CustomAutoScrollToSourceHandler autoScrollToSourceHandler
      = new CustomAutoScrollToSourceHandler(workspaceSettingsComponent.getState());
    autoScrollToSourceHandler.install(issueTree);

    tbMain = createToolbar("revu.toolWindow").getComponent();

    new TreeSpeedSearch(issueTree, new Convertor<TreePath, String>()
    {
      public String convert(TreePath o)
      {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o.getLastPathComponent();
        return node.getUserObject().toString();
      }
    });
    fullTextFilterComponent = issueTree.buildFilterComponent();
  }

  private void configureUI()
  {
    // Later this label might display distinct message depending on app settings
    lbMessage.setIcon(Messages.getInformationIcon());
    lbMessage.setIconTextGap(20);
    messageClickHandler = new MessageClickHandler(project);
    lbMessage.addMouseListener(messageClickHandler);
    
    splitFilter.setFirstComponent(null);
    splitFilter.setSecondComponent(new JScrollPane(issueTree));
  }

  public IssueTree getIssueTree()
  {
    return issueTree;
  }

  @Nullable
  public Review getSelectedReview()
  {
    Issue issue = issueTree.getSelectedIssue();

    return (issue == null) ? null : issue.getReview();
  }

  private void checkRowSelected()
  {
    if ((issueTree.getRowCount() > 0) && (issueTree.getSelectionPath() == null))
    {
//      @todo
//      issueTree.getSelectionModel().setSelectionInterval(0, 0);
//      updateUI(false);
    }
  }

  private void installListeners()
  {
    issueTreeFilterListener = new IIssueTreeFilterListener()
    {
      public void valueChanged(@NotNull EventObject event, @NotNull Object value)
      {
        IssueTreeModel treeModel = issueTree.getIssueTreeModel();
        treeModel.filter(value);
      }
    };

    // Issues
    issueListener = new IIssueListener()
    {
      public void issueAdded(Issue issue)
      {
        updateMessageCount();
        issueTree.getIssueTreeModel().issueAdded(issue);
      }

      public void issueDeleted(Issue issue)
      {
        updateMessageCount();
        issueTree.getIssueTreeModel().issueDeleted(issue);
      }

      public void issueUpdated(final Issue issue)
      {
        // Don't waste time to update UI if form is not visible (but will have to update on show)
        if (!contentPane.isVisible())
        {
          return;
        }

        // Compare by identity since item content has changed
        if (issue == issueTree.getSelectedIssue())
        {
          issuePane.updateUI(issue.getReview(), issue, false);
        }

        issueTree.getIssueTreeModel().issueUpdated(issue);
      }
    };

    review.addIssueListener(issueListener);

    // App Settings
    appSettingsListener = new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings oldSettings, RevuAppSettings newSettings)
      {
        checkMessageInsteadOfPane();
      }
    };
    RevuAppSettingsComponent appSettingsComponent =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class);
    appSettingsComponent.addListener(appSettingsListener);
  }

  public JPanel getContentPane()
  {
    return contentPane;
  }

  public JSplitPane getSplitPane()
  {
    return splitPane;
  }

  public boolean saveIfModified()
  {
    Issue current = issueTree.getSelectedIssue();

    if (current == null)
    {
      return true;
    }

    // Already called in #updateData, but don't want to save review if item has not changed
    if (!issuePane.isModified(current))
    {
      return true;
    }

    if (issuePane.updateData(current))
    {
      project.getComponent(ReviewManager.class).saveSilently(current.getReview());
      return true;
    }

    return false;
  }

  public void updateUI(boolean requestFocus)
  {
    updateMessageCount();
    Issue current = issueTree.getSelectedIssue();
    if (current == null)
    {
      ((CardLayout) pnIssuePaneContainer.getLayout()).show(pnIssuePaneContainer, "message");
    }
    else
    {
      ((CardLayout) pnIssuePaneContainer.getLayout()).show(pnIssuePaneContainer, "issuePane");
      issuePane.updateUI(current.getReview(), current, requestFocus);
    }
  }

  private void updateMessageCount()
  {
    lbCount.setText(RevuBundle.message("browsing.count.text", issueTree.getIssueTreeModel().getIssueCount()));
  }

  public void updateReview()
  {
    updateUI(false);
  }

  private void checkMessageInsteadOfPane()
  {
    String message = null;

    // Login set
    RevuAppSettings appSettings = RevuUtils.getAppSettings();

    if ((appSettings.getLogin() == null) || (appSettings.getLogin().trim().length() == 0))
    {
      message = RevuBundle.message("general.form.noLogin.text");
      messageClickHandler.setType(MessageClickHandler.Type.NO_LOGIN);
    }
    else
    {
      // No review
      Collection<Review> reviews = project.getComponent(ReviewManager.class).getReviews(null, true);
      if (reviews.isEmpty())
      {
        message = RevuBundle.message("browsing.issues.noReview.text");
        messageClickHandler.setType(MessageClickHandler.Type.NO_REVIEW);
      }
      else
      {
        // No issue
        if (issueTree.getRowCount() == 0)
        {
          message = RevuBundle.message((review == null)
            ? "browsing.issues.noIssueForAll.text" : "browsing.issues.noIssueForThis.text");
          messageClickHandler.setType(MessageClickHandler.Type.NO_ISSUE);
        }
      }
    }

    CardLayout cardLayout = (CardLayout) contentPane.getLayout();
    if (message != null)
    {
      lbMessage.setText(message);
      cardLayout.show(contentPane, "label");
    }
    else
    {
      cardLayout.show(contentPane, "form");
    }
  }

  private ActionToolbar createToolbar(@NotNull String toolbarId)
  {
    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(toolbarId);

    return createToolbar(actionGroup);
  }

  private ActionToolbar createToolbar(@NotNull ActionGroup actionGroup)
  {
    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
    actionToolbar.setTargetComponent(issueTree);
    return actionToolbar;
  }

  public void dispose()
  {
    issuePane.dispose();
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .removeListener(appSettingsListener);

    Collection<Review> reviews = project.getComponent(ReviewManager.class).getReviews();
    for (Review review : reviews)
    {
      review.removeIssueListener(issueListener);
      // @TODO IssueTreeModel should do it by itslef
      review.removeIssueListener(issueTree.getIssueTreeModel());
    }
  }

  public void showFilter(@Nullable IIssueTreeFilter issueTreeFilter)
  {
    IssueTreeModel treeModel = issueTree.getIssueTreeModel();
    if (treeModel.getIssueTreeFilter() != null)
    {
      treeModel.getIssueTreeFilter().removeListener(issueTreeFilterListener);
    }

    treeModel.setIssueTreeFilter(issueTreeFilter);
    if (issueTreeFilter == null)
    {
      splitFilter.setFirstComponent(null);
      treeModel.filter(null);
    }
    else
    {
      issueTreeFilter.addListener(issueTreeFilterListener);
      splitFilter.setFirstComponent(issueTreeFilter.buildUI());
    }
    splitFilter.validate();
  }

  private static class MessageClickHandler extends MouseAdapter
  {
    enum Type
    {
      NO_LOGIN,
      NO_REVIEW,
      NO_ISSUE
    }

    private final Project project;
    private Type type;

    public MessageClickHandler(Project project)
    {
      this.project = project;
    }

    public void setType(Type type)
    {
      this.type = type;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
      switch (type)
      {
        case NO_LOGIN:
          RevuUtils.editAppSettings(project);
          break;

        case NO_REVIEW:
          RevuUtils.editProjectSettings(project, null);
          break;

        case NO_ISSUE:
          break;
      }
    }
  }
}
