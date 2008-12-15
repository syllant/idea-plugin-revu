package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.TableUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
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
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueBrowsingPane implements Disposable
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
  private IIssueListener issueListener;

  public IssueBrowsingPane(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;

    $$$setupUI$$$();
    configureUI();

    installListeners();

    checkMessageInsteadOfPane();
    checkRowSelected();
  }

  private void createUIComponents()
  {
    final List<Issue> issues = retrieveIssues();

    issueTable = new IssueTable(project, issues, review);
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
    issueTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    IssueTableModel tableModel = (IssueTableModel) issueTable.getListTableModel();
    tableModel.addTableModelListener(new TableModelListener()
    {
      public void tableChanged(final TableModelEvent e)
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
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              issueTable.getSelectionModel().setSelectionInterval(e.getFirstRow(), e.getFirstRow());
              TableUtil.scrollSelectionToVisible(issueTable);
              checkMessageInsteadOfPane();
            }
          });
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
    tbTable = createToolbar(new SelectColumnsAction(issueTable)).getComponent();

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
    issueListener = new IIssueListener()
    {
      public void issueAdded(Issue issue)
      {
        updateMessageCount();

        // Let table add item so we may select it AFTER
      }

      public void issueDeleted(Issue issue)
      {
        updateMessageCount();

        // Let table remove item so we may select first row if possible
      }

      public void issueUpdated(final Issue issue)
      {
        // Don't waste time to update UI if form is not visible (but will have to update on show)
        if (!contentPane.isVisible())
        {
          return;
        }

        // Compare by identity since item content has changed
        if (issue == issueTable.getSelectedObject())
        {
          issuePane.updateUI(issue.getReview(), issue, false);
        }
      }
    };

    if (review == null)
    {
      for (Review review : reviewManager.getReviews(null, ReviewStatus.REVIEWING, ReviewStatus.FIXING))
      {
        review.addIssueListener(issueListener);
      }

      reviewManager.addReviewListener(new IReviewListener()
      {
        public void reviewChanged(Review review)
        {
          issueTable.getListTableModel().setItems(retrieveIssues());
          checkRowSelected();
          checkMessageInsteadOfPane();
          updateMessageCount();
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
          updateMessageCount();
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
    lbCount.setText(RevuBundle.message("browsing.count.text", issueTable.getListTableModel().getRowCount()));
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
      List<Review> reviews = project.getComponent(ReviewManager.class).getReviews(null, true);
      if (reviews.isEmpty())
      {
        message = RevuBundle.message("browsing.issues.noReview.text");
        messageClickHandler.setType(MessageClickHandler.Type.NO_REVIEW);
      }
      else
      {
        // No issue
        if (issueTable.getRowCount() == 0)
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

  private List<Issue> retrieveIssues()
  {
    final List<Issue> issues;

    if (review == null)
    {
      issues = new ArrayList<Issue>();
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewManager.getReviews(null, true))
      {
        issues.addAll(review.getIssues());
      }
    }
    else
    {
      issues = review.getIssues();
    }

    // @TODO Filter according to recipients
//    // Filter issues according to recipients when set
//    for (Iterator<Issue> it = issues.listIterator(); it.hasNext();)
//    {
//      Issue issue = it.next();
//      User user = RevuUtils.getCurrentUser(issue.getReview());
//      if ((user == null) || ((!user.hasRole(User.Role.REVIEWER)) && (!issue.getRecipients().contains(user))))
//      {
//        it.remove();
//      }
//    }

    return issues;
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

    List<Review> reviews = project.getComponent(ReviewManager.class).getReviews();
    for (Review review : reviews)
    {
      review.removeIssueListener(issueListener);
      // @TODO IssueTableModel should do it by itslef
      review.removeIssueListener((IIssueListener) issueTable.getListTableModel());
    }
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$()
  {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new CardLayout(0, 0));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new BorderLayout(0, 0));
    contentPane.add(panel1, "form");
    panel1.add(tbMain, BorderLayout.WEST);
    splitPane = new JSplitPane();
    splitPane.setResizeWeight(1.0);
    panel1.add(splitPane, BorderLayout.CENTER);
    splitPane.setRightComponent(issuePane.$$$getRootComponent$$$());
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new BorderLayout(0, 0));
    panel2.setMinimumSize(new Dimension(300, 120));
    panel2.setPreferredSize(new Dimension(600, 428));
    splitPane.setLeftComponent(panel2);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 0, 0));
    panel3.setPreferredSize(new Dimension(64, 24));
    panel2.add(panel3, BorderLayout.NORTH);
    panel3.add(tbTable,
      new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, 1, null, null, null,
        0, false));
    final Spacer spacer1 = new Spacer();
    panel3.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    panel3.add(filter, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, new Dimension(-1, 24), null,
      0, false));
    lbCount = new JLabel();
    this.$$$loadLabelText$$$(lbCount,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("browsing.count.text"));
    panel3.add(lbCount, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    panel2.add(scrollPane1, BorderLayout.CENTER);
    scrollPane1.setViewportView(issueTable);
    lbMessage = new JLabel();
    lbMessage.setHorizontalAlignment(0);
    lbMessage.setText("Label");
    contentPane.add(lbMessage, "label");
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
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
    private final IssueTable table;

    private SelectColumnsAction(final @NotNull IssueTable table)
    {
      super(null, RevuBundle.message("browsing.table.selectColumns.text"),
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.SELECT_COLUMNS));
      this.table = table;

      popup = new ElementsChooserPopup<IssueColumnInfo>(project,
        RevuBundle.message("browsing.table.selectColumns.text"),
        RevuPlugin.PLUGIN_NAME + ".ColumnsChooser",
        new ElementsChooserPopup.IPopupListener<IssueColumnInfo>()
        {
          public void apply(@NotNull List<IssueColumnInfo> markedElements)
          {
            table.setColumnInfos(toArray(markedElements));

            RevuWorkspaceSettingsComponent workspaceSettingsComponent =
              project.getComponent(RevuWorkspaceSettingsComponent.class);
            RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();

            workspaceSettings.setBrowsingColNames(IssueColumnInfoRegistry.getColumnNames(markedElements));
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
        asList(table.getListTableModel().getColumnInfos()));
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
