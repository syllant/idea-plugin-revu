package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
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

  public ReviewItemTabbedPane(@NotNull Project project)
  {
    super(project);
    tabbedPane.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
        {
          ReviewItem reviewItem = getData();

          if (!reviewItem.equals(previewForm.getData()))
          {
            previewForm.updateUI(reviewItem);
          }
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

  public void internalUpdateUI(ReviewItem data)
  {
    ReviewItem reviewItem = getData();

    mainForm.updateUI(reviewItem);
    historyForm.updateUI(reviewItem);

    if (SwingUtilities.isDescendingFrom(previewForm.getContentPane(), tabbedPane.getSelectedComponent()))
    {
      previewForm.updateUI(reviewItem);
    }
  }

  public void internalUpdateData(@NotNull ReviewItem reviewItemToUpdate)
  {
    mainForm.internalUpdateData(reviewItemToUpdate);
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
