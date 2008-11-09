package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemTabbedPane extends AbstractReviewItemForm
{
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private ReviewItemMainForm mainForm;
  private ReviewItemPreviewForm previewForm;
  private ReviewItemHistoryForm historyForm;

  public ReviewItemTabbedPane(@NotNull Project project)
  {
    super(project);
    tabbedPane.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        if (previewForm.getContentPane().equals(tabbedPane.getSelectedComponent()))
        {
          if ((reviewItem != null) && (!reviewItem.equals(previewForm.getReviewItem())))
          {
            previewForm.updateUI(reviewItem);
          }
        }
      }
    });
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI()
  {
    mainForm.updateUI(reviewItem);
    historyForm.updateUI(reviewItem);
    previewForm.updateUI(reviewItem);
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
    historyForm = new ReviewItemHistoryForm(project);
    previewForm = new ReviewItemPreviewForm(project);
  }
}
