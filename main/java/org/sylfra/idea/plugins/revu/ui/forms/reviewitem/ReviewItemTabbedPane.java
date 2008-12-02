package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemTabbedPane extends AbstractReviewItemForm
{
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private ReviewItemMainForm mainForm;
  private ReviewItemPreviewForm previewForm;
  private HistoryForm historyForm;
  private ReviewItem currentReviewItem;

  public ReviewItemTabbedPane(@NotNull Project project)
  {
    super(project);
    tabbedPane.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
        {
          previewForm.updateUI(currentReviewItem);
        }
      }
    });
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

  public void internalUpdateUI(@Nullable ReviewItem data)
  {
    currentReviewItem = data;

    mainForm.updateUI(data);
    historyForm.updateUI(data);

    if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
    {
      previewForm.updateUI(data);
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

  private void createUIComponents()
  {
    mainForm = new ReviewItemMainForm(project);
    previewForm = new ReviewItemPreviewForm(project);
  }

}
