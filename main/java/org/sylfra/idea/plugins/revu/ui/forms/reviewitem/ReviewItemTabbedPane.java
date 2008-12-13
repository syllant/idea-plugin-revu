package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.ItemResolutionStatus;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.browsingtable.ReviewItemsTable;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemTabbedPane extends AbstractReviewItemForm
{
  private final ReviewItemsTable reviewItemsTable;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private ReviewItemMainForm mainForm;
  private ReviewItemPreviewForm previewForm;
  private HistoryForm historyForm;
  private JComponent toolbar;
  private JButton bnResolve;
  private JButton bnReopen;
  private JButton bnClose;
  private JLabel lbStatus;
  private ReviewItem currentReviewItem;

  public ReviewItemTabbedPane(@NotNull Project project, ReviewItemsTable reviewItemsTable)
  {
    super(project);
    this.reviewItemsTable = reviewItemsTable;

    configureUI();
  }

  private void createUIComponents()
  {
    mainForm = new ReviewItemMainForm(project, false);
    previewForm = new ReviewItemPreviewForm(project);

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.reviewItemForm.toolbar");

    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true);
    actionToolbar.setTargetComponent(reviewItemsTable);
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
          previewForm.updateUI(getEnclosingReview(), currentReviewItem, true);
        }
      }
    });

    bnResolve.addActionListener(createStatusListener(ItemResolutionStatus.RESOLVED));
    bnClose.addActionListener(createStatusListener(ItemResolutionStatus.CLOSED));
    bnReopen.addActionListener(createStatusListener(ItemResolutionStatus.REOPENED));
  }

  private ActionListener createStatusListener(final ItemResolutionStatus status)
  {
    return new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        currentReviewItem.setResolutionStatus(status);
        updateData(currentReviewItem);
        updateUI(currentReviewItem.getReview(), currentReviewItem, true);
      }
    };
  }

  public JComponent getPreferredFocusedComponent()
  {
    return mainForm.getPreferredFocusedComponent();
  }

  public void internalValidateInput()
  {
    mainForm.internalValidateInput();
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI(@Nullable ReviewItem data, boolean requestFocus)
  {
    currentReviewItem = data;

    lbStatus.setText((data == null) ? "" : RevuBundle.message("form.reviewitem.status.label",
      RevuUtils.buildStatusLabel(data.getResolutionStatus())));

    mainForm.updateUI(getEnclosingReview(), data, requestFocus);
    historyForm.updateUI(getEnclosingReview(), data, requestFocus);

    if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
    {
      previewForm.updateUI(getEnclosingReview(), data, requestFocus);
    }
  }

  public void internalUpdateData(@NotNull ReviewItem data)
  {
    mainForm.internalUpdateData(data);
  }

  public boolean isModified(ReviewItem data)
  {
    return mainForm.isModified(data);
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    ItemResolutionStatus status = (currentReviewItem == null) ? null : currentReviewItem.getResolutionStatus();

    RevuUtils.setWriteAccess((user != null)
      && (ItemResolutionStatus.TO_RESOLVE.equals(status) || ItemResolutionStatus.REOPENED.equals(status)),
      bnResolve);
    RevuUtils.setWriteAccess((user != null) && (ItemResolutionStatus.RESOLVED.equals(status))
      && (user.hasRole(User.Role.REVIEWER)), bnClose);
    RevuUtils.setWriteAccess((user != null) && (ItemResolutionStatus.CLOSED.equals(status))
      && (user.hasRole(User.Role.REVIEWER)), bnReopen);
  }

  @Override
  public void dispose()
  {
    mainForm.dispose();
    historyForm.dispose();
    previewForm.dispose();
  }

}
