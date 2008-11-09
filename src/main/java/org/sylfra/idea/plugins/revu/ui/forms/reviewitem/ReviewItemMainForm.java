package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.ReviewCategory;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.model.ReviewPriority;
import org.sylfra.idea.plugins.revu.model.ReviewReferential;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemMainForm extends AbstractReviewItemForm
{
  private JPanel contentPane;
  private JTextArea taDesc;
  private JComboBox cbPriority;
  private JTextField tfTitle;
  private JComboBox cbCategory;
  private JComboBox cbStatus;
  private JLabel lbLocation;

  public ReviewItemMainForm(@NotNull Project project)
  {
    super(project);
    cbPriority.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus)
      {
        if (value != null)
        {
          value = ((ReviewPriority) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbCategory.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus)
      {
        if (value != null)
        {
          value = ((ReviewCategory) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbStatus.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus)
      {
        if (value != null)
        {
          ReviewItem.Status status = (ReviewItem.Status) value;
          value = RevuBundle.message("general.status." + status.toString().toLowerCase() + ".text");
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI()
  {
    ReviewReferential referential = (reviewItem == null)
      ? null : reviewItem.getReview().getReviewReferential();

    Object[] items = (referential == null) ? new Object[]{} :
      referential.getPrioritiesByName().values().toArray();
    cbPriority.setModel(new DefaultComboBoxModel(items));

    items = (referential == null) ? new Object[]{} :
      referential.getCategoriesByName().values().toArray();
    cbCategory.setModel(new DefaultComboBoxModel(items));
    cbStatus.setModel(new DefaultComboBoxModel(ReviewItem.Status.values()));

    taDesc.setText((reviewItem == null) ? RevuBundle.EMPTY_FIELD : reviewItem.getDesc());
    tfTitle.setText((reviewItem == null) ? RevuBundle.EMPTY_FIELD : reviewItem.getTitle());
    cbPriority.setSelectedItem((reviewItem == null) ? RevuBundle.EMPTY_FIELD : reviewItem.getPriority());
    cbCategory.setSelectedItem((reviewItem == null) ? RevuBundle.EMPTY_FIELD : reviewItem.getCategory());
    cbStatus.setSelectedItem((reviewItem == null) ? RevuBundle.EMPTY_FIELD : reviewItem.getStatus());

    String location = (reviewItem == null) ? RevuBundle.EMPTY_FIELD : (RevuUtils.buildRelativePath(project, reviewItem)
      + " [" + reviewItem.getLineStart()
      + " - " + reviewItem.getLineEnd()
      + "]");
    lbLocation.setText((reviewItem == null) ? RevuBundle.EMPTY_FIELD : location);
  }

  protected void internalUpdateData(ReviewItem reviewItemToUpdate)
  {
    ReviewReferential referential = reviewItemToUpdate.getReview().getReviewReferential();

    reviewItemToUpdate.setDesc(taDesc.getText());
    reviewItemToUpdate.setTitle(tfTitle.getText());
    reviewItemToUpdate.setPriority(referential.getPriority(cbPriority.getSelectedItem().toString()));
    reviewItemToUpdate.setCategory(referential.getCategory(cbCategory.getSelectedItem().toString()));
    reviewItemToUpdate.setStatus(ReviewItem.Status.valueOf(cbStatus.getSelectedItem().toString().toUpperCase()));
  }

  public boolean isModified(ReviewItem data)
  {
    if (taDesc.getText() != null ? !taDesc.getText().equals(data.getDesc()) :
      data.getDesc() != null)
    {
      return true;
    }
    if (tfTitle.getText() != null ? !tfTitle.getText().equals(data.getTitle()) :
      data.getTitle() != null)
    {
      return true;
    }
    return false;
  }
}
