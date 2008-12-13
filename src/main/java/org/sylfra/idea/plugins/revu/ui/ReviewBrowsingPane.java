package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.browsingtable.*;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssuePane;
import org.sylfra.idea.plugins.revu.ui.forms.settings.app.RevuAppSettingsForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.RevuProjectSettingsForm;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewBrowsingPane implements Disposable
{
  private JPanel contentPane;
  private final Project project;
  private final Review review;
  private IssueTable issueTable;
  private JComponent tbMain;
  private IssuePane issuePane;
  private JSplitPane splitPane;
  private JLabel lbMessage;
  private JComponent tbTable;
  private JComponent filter;
  private JLabel lbCount;
  private IRevuSettingsListener<RevuAppSettings> appSettingsListener;
  private MessageClickHandler messageClickHandler;

  public ReviewBrowsingPane(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;

    configureUI();

    installListeners();

    checkMessageInsteadOfPane();
    checkRowSelected();
  }

  private void createUIComponents()
  {
    final List<Issue> items = retrieveIssues();

    issueTable = new IssueTable(project, items, review);
    issueTable.setSelectionModel(new DefaultListSelectionModel()
    {
      @Override
      public void setSelectionInterval(int index0, int index1)
      {
        if (saveIfModified())
        {
          super.setSelectionInterval(index0, index1);
          updateUI(false);
        }
      }
    });

    IssueTableModel tableModel = (IssueTableModel) issueTable.getListTableModel();
    tableModel.addTableModelListener(new TableModelListener()
    {
      public void tableChanged(TableModelEvent e)
      {
        if (e.getType() == TableModelEvent.DELETE)
        {
          issueTable.getSelectionModel().clearSelection();
          checkMessageInsteadOfPane();
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              checkRowSelected();
            }
          });
        }
        else if (e.getType() == TableModelEvent.INSERT)
        {
          issueTable.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getFirstRow());
          checkMessageInsteadOfPane();
        }
      }
    });

    issuePane = new IssuePane(project, issueTable);

    RevuWorkspaceSettingsComponent workspaceSettingsComponent = project.getComponent(
      RevuWorkspaceSettingsComponent.class);

    CustomAutoScrollToSourceHandler autoScrollToSourceHandler
      = new CustomAutoScrollToSourceHandler(workspaceSettingsComponent.getState());
    autoScrollToSourceHandler.install(issueTable);

    tbMain = createToolbar((review == null) ? "revu.toolWindow.allReviews" : "revu.toolWindow.review").getComponent();
    tbTable = createToolbar(new SelectColumnsAction(tableModel)).getComponent();

    new IssueTableSearchBar(issueTable);
    filter = issueTable.buildFilterComponent();
  }

  private void configureUI()
  {
    // Later this label might display distinct message depending on app settings
    lbMessage.setIcon(Messages.getInformationIcon());
    lbMessage.setIconTextGap(20);
    messageClickHandler = new MessageClickHandler(project);
    lbMessage.addMouseListener(messageClickHandler);

    RevuWorkspaceSettings workspaceSettings = project.getComponent(RevuWorkspaceSettingsComponent.class).getState();
    splitPane.setOrientation(Integer.parseInt(workspaceSettings.getToolWindowSplitOrientation()));
    splitPane.setDividerLocation(0.5d);
  }

  @Nullable
  public Review getSelectedReview()
  {
    Issue issue = issueTable.getSelectedObject();

    return (issue == null) ? null : issue.getReview();
  }

  private void checkRowSelected()
  {
    if ((issueTable.getRowCount() > 0) && (issueTable.getSelectedRow() == -1))
    {
      issueTable.getSelectionModel().setSelectionInterval(0, 0);
      updateUI(false);
    }
  }

  private void installListeners()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    // Issues
    final IIssueListener issueListener = new IIssueListener()
    {
      public void itemAdded(Issue item)
      {
        updateMessageCount();

        // Let table add item so we may select it AFTER
      }

      public void itemDeleted(Issue item)
      {
        updateMessageCount();

        // Let table remove item so we may select first row if possible
      }

      public void itemUpdated(final Issue item)
      {
        // Don't waste time to update UI if form is not visible (but will have to update on show)
        if (!contentPane.isVisible())
        {
          return;
        }

        // Compare by identity since item content has changed
        if (item == issueTable.getSelectedObject())
        {
          issuePane.updateUI(review, item, false);
        }
      }
    };

    if (review == null)
    {
      for (Review review : reviewManager.getReviews(true, false))
      {
        review.addIssueListener(issueListener);
      }

      reviewManager.addReviewListener(new IReviewListener()
      {
        public void reviewChanged(Review review)
        {
        }

        public void reviewAdded(Review review)
        {
          review.addIssueListener(issueListener);
          issueTable.getListTableModel().setItems(retrieveIssues());
          checkRowSelected();
          checkMessageInsteadOfPane();
          updateMessageCount();
        }

        public void reviewDeleted(Review review)
        {
          issueTable.getListTableModel().setItems(retrieveIssues());
          checkRowSelected();
          checkMessageInsteadOfPane();
        }
      });
    }
    else
    {
      review.addIssueListener(issueListener);
    }

    // App Settings
    appSettingsListener = new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings settings)
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
    Issue current = issueTable.getSelectedObject();

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
      project.getComponent(ReviewManager.class).save(current.getReview());
      return true;
    }

    return false;
  }

  public void updateUI(boolean requestFocus)
  {
    checkRowSelected();
    updateMessageCount();
    Issue current = issueTable.getSelectedObject();
    if (current != null)
    {
      issuePane.updateUI(current.getReview(), current, requestFocus);
    }
  }

  private void updateMessageCount()
  {
    lbCount.setText(RevuBundle.message("browsing.count.text", issueTable.getRowCount()));
  }

  public void updateReview()
  {
    updateUI(false);
  }

  private void checkMessageInsteadOfPane()
  {
    String message = null;

    // Login set
    RevuAppSettings appSettings = ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .getState();

    if ((appSettings.getLogin() == null) || (appSettings.getLogin().trim().length() == 0))
    {
      message = RevuBundle.message("general.form.noLogin.text");
      messageClickHandler.setType(MessageClickHandler.Type.NO_LOGIN);
    }
    else
    {
      // No review
      List<Review> reviews = project.getComponent(ReviewManager.class).getReviews(true, false);
      if (reviews.isEmpty())
      {
        message = RevuBundle.message("toolwindow.noReview.text");
        messageClickHandler.setType(MessageClickHandler.Type.NO_REVIEW);
      }
      else
      {
        // No issue
        if (issueTable.getRowCount() == 0)
        {
          message = RevuBundle.message((review == null)
            ? "toolwindow.noIssueForAll.text" : "toolwindow.noIssueForThis.text");
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

  private List<Issue> retrieveIssues()
  {
    final List<Issue> items;

    if (review == null)
    {
      items = new ArrayList<Issue>();
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewManager.getReviews(true, false))
      {
        items.addAll(review.getItems());
      }
    }
    else
    {
      items = review.getItems();
    }

    return items;
  }

  private ActionToolbar createToolbar(@NotNull String toolbarId)
  {
    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(toolbarId);

    return createToolbar(actionGroup);
  }

  private ActionToolbar createToolbar(@NotNull AnAction... actions)
  {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    for (AnAction action : actions)
    {
      actionGroup.add(action);
    }
    return createToolbar(actionGroup);
  }

  private ActionToolbar createToolbar(@NotNull ActionGroup actionGroup)
  {
    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
    actionToolbar.setTargetComponent(issueTable);
    return actionToolbar;
  }

  public void dispose()
  {
    issuePane.dispose();
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .removeListener(appSettingsListener);
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
          ShowSettingsUtil.getInstance().showSettingsDialog(project, RevuAppSettingsForm.class);
          break;

        case NO_REVIEW:
          ShowSettingsUtil.getInstance().showSettingsDialog(project, RevuProjectSettingsForm.class);
          break;

        case NO_ISSUE:
          break;
      }
    }
  }

  private class SelectColumnsAction extends AnAction
  {
    private ElementsChooserPopup<IssueColumnInfo> popup;
    private final IssueTableModel tableModel;

    private SelectColumnsAction(final IssueTableModel tableModel)
    {
      super(null, RevuBundle.message("browsing.table.selectColumns.text"),
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.SELECT_COLUMNS));
      this.tableModel = tableModel;

      popup = new ElementsChooserPopup<IssueColumnInfo>(RevuBundle.message("browsing.table.selectColumns.text"),
        new ElementsChooserPopup.IPopupListener<IssueColumnInfo>()
        {
          public void apply(@NotNull List<IssueColumnInfo> markedElements)
          {
            tableModel.setColumnInfos(toArray(markedElements));

            RevuWorkspaceSettingsComponent workspaceSettingsComponent =
              project.getComponent(RevuWorkspaceSettingsComponent.class);
            RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();

            List<String> colNames = new ArrayList<String>(markedElements.size());
            for (IssueColumnInfo columnInfo : markedElements)
            {
              colNames.add(columnInfo.getName());
            }

            workspaceSettings.setBrowsingColNames(colNames);
            workspaceSettingsComponent.loadState(workspaceSettings);
          }
        },
        new ElementsChooserPopup.IItemRenderer<IssueColumnInfo>()
        {
          public String getText(IssueColumnInfo item)
          {
            return item.getName();
          }
        });
    }

    public void actionPerformed(AnActionEvent e)
    {
      Component owner = (Component) e.getInputEvent().getSource();
      popup.show(owner, false,
        Arrays.asList(IssueColumnInfoRegistry.ALL_COLUMN_INFOS),
        asList(tableModel.getColumnInfos()));
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
}
