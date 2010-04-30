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
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssuePane extends AbstractIssueForm
{
  private final JComponent targetActionComponent;
  private final boolean inDialog;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private IssueMainForm mainForm;
  private IssueAssigneesForm assigneesForm;
  private IssueNotesForm notesForm;
  private IssuePreviewForm previewForm;
  private HistoryForm<Issue> historyForm;
  private JComponent toolbar;
  private JButton bnResolve;
  private JButton bnReopen;
  private JButton bnClose;
  private JLabel lbStatus;
  private JPanel pnStatus;
  private JPanel pnToolbar;
  private Issue currentIssue;

  public IssuePane(@NotNull Project project, JComponent targetActionComponent, boolean inDialog)
  {
    super(project);
    this.targetActionComponent = targetActionComponent;
    this.inDialog = inDialog;

    configureUI();
  }

  private void createUIComponents()
  {
    mainForm = new IssueMainForm(project, false, inDialog);
    assigneesForm = new IssueAssigneesForm(project);
    notesForm = new IssueNotesForm(project);
    previewForm = new IssuePreviewForm(project);

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.issueForm.toolbar");

    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);
    actionToolbar.setTargetComponent(targetActionComponent);
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
          previewForm.updateUI(getEnclosingReview(null), currentIssue, true);
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
        currentIssue.getReview().fireIssueUpdated(currentIssue);
      }
    };
  }

  public JComponent getPreferredFocusedComponent()
  {
    return mainForm.getPreferredFocusedComponent();
  }

  public void internalValidateInput(@Nullable Issue data)
  {
    updateError(mainForm.getContentPane(), (data != null) && !mainForm.validateInput(data), null);
    updateError(assigneesForm.getContentPane(), (data != null) && !assigneesForm.validateInput(data), null);
    updateError(notesForm.getContentPane(), (data != null) && !notesForm.validateInput(data), null);

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

    Review enclosingReview = getEnclosingReview(data);
    mainForm.updateUI(enclosingReview, data, requestFocus);
    assigneesForm.updateUI(enclosingReview, data, requestFocus);
    notesForm.updateUI(enclosingReview, data, requestFocus);
    historyForm.updateUI(enclosingReview, data, requestFocus);

    tabbedPane.setTitleAt(1,
      RevuBundle.message("issueForm.assignees.title", (data == null) ? 0 : data.getAssignees().size()));
    tabbedPane.setTitleAt(2,
      RevuBundle.message("issueForm.notes.title", (data == null) ? 0 : data.getNotes().size()));

    if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
    {
      previewForm.updateUI(enclosingReview, data, requestFocus);
    }
  }

  public void internalUpdateData(@NotNull Issue data)
  {
    mainForm.internalUpdateData(data);
    assigneesForm.internalUpdateData(data);
    notesForm.internalUpdateData(data);

    data.getHistory().setLastUpdatedBy(RevuUtils.getCurrentUser(data.getReview()));
    data.getHistory().setLastUpdatedOn(new Date());

    data.getReview().fireIssueUpdated(data);
  }

  public boolean isModified(Issue data)
  {
    return ((mainForm.isModified(data)) || (assigneesForm.isModified(data)) || (notesForm.isModified(data)));
  }

  @Override
  protected void internalUpdateWriteAccess(Issue data, @Nullable User user)
  {
    IssueStatus status = (currentIssue == null) ? null : currentIssue.getStatus();

    RevuUtils.setWriteAccess((user != null)
      && (IssueStatus.TO_RESOLVE.equals(status) || IssueStatus.REOPENED.equals(status)),
      bnResolve);
    RevuUtils.setWriteAccess((user != null) && (IssueStatus.RESOLVED.equals(status))
      && (user.hasRole(User.Role.REVIEWER)), bnClose);
    RevuUtils.setWriteAccess((user != null) 
      && (IssueStatus.RESOLVED.equals(status) || IssueStatus.CLOSED.equals(status)), bnReopen);
  }

  @Override
  public void dispose()
  {
    mainForm.dispose();
    assigneesForm.dispose();
    notesForm.dispose();
    historyForm.dispose();
    previewForm.dispose();
  }
}
