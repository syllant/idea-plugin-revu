package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueTable;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssuePane extends AbstractIssueForm
{
  private final IssueTable issueTable;
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
  private Issue currentIssue;

  public IssuePane(@NotNull Project project, IssueTable issueTable)
  {
    super(project);
    this.issueTable = issueTable;

    configureUI();
  }

  private void createUIComponents()
  {
    mainForm = new IssueMainForm(project, false);
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

    lbStatus.setText((data == null) ? "" : RevuBundle.message("issueForm.status.label",
      RevuUtils.buildIssueStatusLabel(data.getStatus())));

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

}
