package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemHistoryForm extends AbstractReviewItemForm
{
  public static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(
    DateFormat.LONG, DateFormat.LONG);

  private JPanel contentPane;
  private JLabel lbCreatedBy;
  private JLabel lbCreatedOn;
  private JLabel lbLastUpdatedBy;
  private JLabel lbLastUpdatedOn;

  public ReviewItemHistoryForm(@NotNull Project project)
  {
    super(project);
  }

  protected boolean isModified(@NotNull ReviewItem reviewItem)
  {
    return false;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI()
  {
    lbCreatedBy.setText((reviewItem == null)
      ? RevuBundle.EMPTY_FIELD : reviewItem.getHistory().getCreatedBy().getDisplayName());
    lbCreatedOn.setText((reviewItem == null)
      ? RevuBundle.EMPTY_FIELD : DATE_FORMATTER.format(new Date(reviewItem.getHistory().getCreatedOn())));
    lbLastUpdatedBy.setText((reviewItem == null)
      ? RevuBundle.EMPTY_FIELD : reviewItem.getHistory().getLastUpdatedBy().getDisplayName());
    lbLastUpdatedOn.setText((reviewItem == null)
      ? RevuBundle.EMPTY_FIELD : DATE_FORMATTER.format(new Date(reviewItem.getHistory().getLastUpdatedOn())));
  }

  public JComponent getPreferredFocusedComponent()
  {
    return null;
  }

  public void internalValidateInput()
  {
  }

  protected void internalUpdateData(@Nullable ReviewItem reviewItemToUpdate)
  {
  }
}
