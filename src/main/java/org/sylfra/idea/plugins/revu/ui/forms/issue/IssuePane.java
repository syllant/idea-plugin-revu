package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueTable;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssuePane extends AbstractIssueForm
{
  private final IssueTable issueTable;
  private final boolean inDialog;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private IssueMainForm mainForm;
  private IssueRecipientsForm recipientsForm;
  private IssueNotesForm notesForm;
  private IssuePreviewForm previewForm;
  private HistoryForm<Issue> historyForm;
  private JComponent toolbar;
  private JButton bnResolve;
  private JButton bnReopen;
  private JButton bnClose;
  private JLabel lbStatus;
  private JPanel pnStatus;
  private Issue currentIssue;

  public IssuePane(@NotNull Project project, IssueTable issueTable, boolean inDialog)
  {
    super(project);
    this.issueTable = issueTable;
    this.inDialog = inDialog;

    $$$setupUI$$$();
    configureUI();
  }

  private void createUIComponents()
  {
    mainForm = new IssueMainForm(project, false, inDialog);
    recipientsForm = new IssueRecipientsForm(project);
    notesForm = new IssueNotesForm(project);
    previewForm = new IssuePreviewForm(project);

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.issueForm.toolbar");

    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);
    actionToolbar.setTargetComponent(issueTable);
    toolbar = actionToolbar.getComponent();
  }

  private void configureUI()
  {
    tabbedPane.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
        {
          previewForm.updateUI(getEnclosingReview(), currentIssue, true);
        }
      }
    });

    bnResolve.addActionListener(createStatusListener(IssueStatus.RESOLVED));
    bnClose.addActionListener(createStatusListener(IssueStatus.CLOSED));
    bnReopen.addActionListener(createStatusListener(IssueStatus.REOPENED));
  }

  private ActionListener createStatusListener(final IssueStatus status)
  {
    return new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        currentIssue.setStatus(status);
        updateData(currentIssue);
        updateUI(currentIssue.getReview(), currentIssue, true);
      }
    };
  }

  public JComponent getPreferredFocusedComponent()
  {
    return mainForm.getPreferredFocusedComponent();
  }

  public void internalValidateInput()
  {
    updateError(mainForm.getContentPane(), !mainForm.validateInput(), null);
    updateError(recipientsForm.getContentPane(), !recipientsForm.validateInput(), null);
    updateError(notesForm.getContentPane(), !notesForm.validateInput(), null);

    updateTabIcons(tabbedPane);
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    updateTabIcons(tabbedPane);

    currentIssue = data;

    pnStatus.setBackground(data == null ? null : RevuUtils.getIssueStatusColor(data.getStatus()));
    lbStatus.setText((data == null) ? "" : RevuUtils.buildIssueStatusLabel(data.getStatus()));

    mainForm.updateUI(getEnclosingReview(), data, requestFocus);
    recipientsForm.updateUI(getEnclosingReview(), data, requestFocus);
    notesForm.updateUI(getEnclosingReview(), data, requestFocus);
    historyForm.updateUI(getEnclosingReview(), data, requestFocus);

    if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
    {
      previewForm.updateUI(getEnclosingReview(), data, requestFocus);
    }
  }

  public void internalUpdateData(@NotNull Issue data)
  {
    mainForm.internalUpdateData(data);
    recipientsForm.internalUpdateData(data);
    notesForm.internalUpdateData(data);

    data.getHistory().setLastUpdatedBy(RevuUtils.getCurrentUser(data.getReview()));
    data.getHistory().setLastUpdatedOn(new Date());

    data.getReview().fireIssueUpdated(data);
  }

  public boolean isModified(Issue data)
  {
    return ((mainForm.isModified(data)) || (recipientsForm.isModified(data)) || (notesForm.isModified(data)));
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    IssueStatus status = (currentIssue == null) ? null : currentIssue.getStatus();

    RevuUtils.setWriteAccess((user != null)
      && (IssueStatus.TO_RESOLVE.equals(status) || IssueStatus.REOPENED.equals(status)),
      bnResolve);
    RevuUtils.setWriteAccess((user != null) && (IssueStatus.RESOLVED.equals(status))
      && (user.hasRole(User.Role.REVIEWER)), bnClose);
    RevuUtils.setWriteAccess((user != null) && (IssueStatus.CLOSED.equals(status))
      && (user.hasRole(User.Role.REVIEWER)), bnReopen);
  }

  @Override
  public void dispose()
  {
    mainForm.dispose();
    recipientsForm.dispose();
    notesForm.dispose();
    historyForm.dispose();
    previewForm.dispose();
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
    contentPane.setLayout(new BorderLayout(0, 0));
    tabbedPane = new JTabbedPane();
    contentPane.add(tabbedPane, BorderLayout.CENTER);
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.main.title"),
      panel1);
    panel1.add(mainForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.recipients.title"),
      panel2);
    panel2.add(recipientsForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.notes.title"),
      panel3);
    panel3.add(notesForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.preview.title"),
      panel4);
    panel4.add(previewForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.history.title"),
      panel5);
    historyForm = new HistoryForm();
    panel5.add(historyForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel6 = new JPanel();
    panel6.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel6, BorderLayout.NORTH);
    panel6.add(toolbar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel6.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    bnResolve = new JButton();
    this.$$$loadButtonText$$$(bnResolve,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.doResolve.text"));
    panel6.add(bnResolve,
      new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
        null, null, null, 0, false));
    bnReopen = new JButton();
    this.$$$loadButtonText$$$(bnReopen,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.doReopen.text"));
    panel6.add(bnReopen, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    bnClose = new JButton();
    this.$$$loadButtonText$$$(bnClose,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.doClose.text"));
    panel6.add(bnClose, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    final JPanel panel7 = new JPanel();
    panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel6.add(panel7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel7.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), null));
    pnStatus = new JPanel();
    pnStatus.setLayout(new BorderLayout(0, 0));
    panel7.add(pnStatus, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    pnStatus.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2), null));
    lbStatus = new JLabel();
    lbStatus.setFont(new Font(lbStatus.getFont().getName(), Font.BOLD, 9));
    lbStatus.setOpaque(false);
    lbStatus.setText("[Status]");
    pnStatus.add(lbStatus, BorderLayout.CENTER);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text)
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
      component.setMnemonic(mnemonic);
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
}
