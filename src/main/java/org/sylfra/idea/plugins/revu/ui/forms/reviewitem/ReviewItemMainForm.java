package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemMainForm extends AbstractReviewItemForm
{
  private JPanel contentPane;
  private JTextArea taDesc;
  private JComboBox cbPriority;
  private JTextArea taTitle;
  private JComboBox cbCategory;
  private JComboBox cbResolutionStatus;
  private JLabel lbLocation;
  private JComboBox cbReview;
  private JRadioButton rbLocationFile;
  private JRadioButton rbLocationGlobal;
  private JRadioButton rbLocationLineRange;
  private JComboBox cbResolutionType;
  private ButtonGroup bgLocation;

  public ReviewItemMainForm(@NotNull Project project)
  {
    super(project);
    configureUI();
  }

  public JComponent getPreferredFocusedComponent()
  {
    return taTitle;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI(@NotNull ReviewItem reviewItem)
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    Collection<Review> reviews = reviewManager.getReviews(true);

    Review defaultReview = ((reviewItem.getReview() == null) && (reviews.size() == 1))
      ? reviews.iterator().next() : reviewItem.getReview();

    cbReview.setSelectedItem(defaultReview);

    taDesc.setText(reviewItem.getDesc());
    taTitle.setText(reviewItem.getSummary());
    cbPriority.setSelectedItem(reviewItem.getPriority());
    cbCategory.setSelectedItem(reviewItem.getCategory());
    cbResolutionStatus.setSelectedItem(reviewItem.getResolutionStatus());
    cbResolutionType.setSelectedItem(reviewItem.getResolutionType());

    ReviewItem.LocationType locationType = reviewItem.getLocationType();
    rbLocationFile.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType)
      && !ReviewItem.LocationType.FILE.equals(locationType));
    updateLocation(locationType);
  }

  private void updateLocation(ReviewItem.LocationType locationType)
  {
    ReviewItem reviewItem = getData();

    String locationPath;
    switch (locationType)
    {
      case GLOBAL:
        rbLocationGlobal.setSelected(true);
        locationPath = "[" + RevuBundle.message("form.reviewitem.main.location.global.text") + "]";
        break;

      case FILE:
        rbLocationFile.setSelected(true);
        locationPath = RevuUtils.buildRelativePath(project, reviewItem.getFile());
        break;

      default:
        rbLocationLineRange.setSelected(true);
        locationPath = RevuBundle.message("form.reviewitem.main.location.range.path.text",
          RevuUtils.buildRelativePath(project, reviewItem.getFile()), reviewItem.getLineStart(),
          reviewItem.getLineEnd());
    }

    lbLocation.setText(locationPath);
  }

  protected void internalUpdateData(ReviewItem reviewItemToUpdate)
  {
    Review review = (Review) cbReview.getSelectedItem();

    reviewItemToUpdate.setReview(review);

    reviewItemToUpdate.setDesc(taDesc.getText());
    reviewItemToUpdate.setSummary(taTitle.getText());
    reviewItemToUpdate.setPriority((ItemPriority) cbPriority.getSelectedItem());
    reviewItemToUpdate.setCategory((ItemCategory) cbCategory.getSelectedItem());
    reviewItemToUpdate.setResolutionStatus((ItemResolutionStatus) cbResolutionStatus.getSelectedItem());
    reviewItemToUpdate.setResolutionType((ItemResolutionType) cbResolutionType.getSelectedItem());

    // Location
    if (rbLocationGlobal.isSelected())
    {
      reviewItemToUpdate.setFile(null);
      reviewItemToUpdate.setLineStart(-1);
    }
    else if (rbLocationFile.isSelected())
    {
      reviewItemToUpdate.setLineStart(-1);
    }
  }

  public boolean isModified(ReviewItem reviewItem)
  {
    if (!checkEquals(taDesc.getText(), reviewItem.getDesc()))
    {
      return true;
    }

    if (!checkEquals(taTitle.getText(), reviewItem.getSummary()))
    {
      return true;
    }

    if (!checkEquals(cbReview.getSelectedItem(), reviewItem.getReview()))
    {
      return true;
    }

    if (!checkEquals(cbPriority.getSelectedItem(), reviewItem.getPriority()))
    {
      return true;
    }

    if (!checkEquals(cbCategory.getSelectedItem(), reviewItem.getCategory()))
    {
      return true;
    }

    if (!checkEquals(bgLocation.getSelection().getActionCommand(), reviewItem.getLocationType().toString()))
    {
      return true;
    }

    return false;
  }

  public void internalValidateInput()
  {
    updateRequiredError(taTitle, "".equals(taTitle.getText().trim()));
    updateRequiredError(cbReview, (!(cbReview.getSelectedItem() instanceof Review)));
    updateRequiredError(cbResolutionStatus, (!(cbResolutionStatus.getSelectedItem() instanceof ItemResolutionStatus)));

    // Check is user is declared in selected review
    if (cbReview.getSelectedItem() instanceof Review)
    {
      Review review = (Review) cbReview.getSelectedItem();

      RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();
      User user = review.getDataReferential().getUser(appSettings.getLogin());

      updateRequiredError(cbReview, user == null);
    }
  }

  private void configureUI()
  {
    RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();

    cbResolutionStatus.setModel(new DefaultComboBoxModel(buildComboItemsArray(
      Arrays.asList(ItemResolutionStatus.values()), true)));

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    cbReview.setModel(new ReviewComboBoxModel(buildComboItemsArray(reviewManager.getReviews(true,
      appSettings.getLogin()), true)));
    cbReview.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          value = ((Review) value).getTitle();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbPriority.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message(cbReview.getSelectedItem() == null
            ? "general.selectReviewBeforeFillingCombo.text" : "general.selectComboValue.text");
        }
        else
        {
          value = ((ItemPriority) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbCategory.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message(cbReview.getSelectedItem() == null
            ? "general.selectReviewBeforeFillingCombo.text" : "general.selectComboValue.text");
        }
        else
        {
          value = ((ItemCategory) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbResolutionStatus.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          ItemResolutionStatus status = (ItemResolutionStatus) value;
          value = RevuBundle.message("general.status." + status.toString().toLowerCase() + ".text");
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbResolutionType.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          value = ((ItemResolutionType) value).getName();
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbReview.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Object selectedReview = cbReview.getSelectedItem();
        if (selectedReview instanceof Review)
        {
          DataReferential referential = ((Review) selectedReview).getDataReferential();

          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemPriority>(referential.getItemPrioritiesByName().values()), true)));

          cbCategory.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemCategory>(referential.getItemCategoriesByName().values()), true)));

          cbResolutionType.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemResolutionType>(referential.getItemResolutionTypesByName().values()), false)));
        }
        else
        {
          // "[Select a value]" String is selected
          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));
          cbCategory.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));
          cbResolutionType.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), false)));
        }
      }
    });

    ActionListener locationTypeListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ReviewItem.LocationType locationType = (rbLocationGlobal.isSelected())
          ? ReviewItem.LocationType.GLOBAL
          : ((rbLocationFile.isSelected()) ? ReviewItem.LocationType.FILE : ReviewItem.LocationType.LINE_RANGE);
        updateLocation(locationType);
      }
    };
    rbLocationGlobal.addActionListener(locationTypeListener);
    rbLocationFile.addActionListener(locationTypeListener);
    rbLocationLineRange.addActionListener(locationTypeListener);
  }

  private class ReviewComboBoxModel extends DefaultComboBoxModel implements IReviewListener
  {
    private ReviewComboBoxModel(Object[] objects)
    {
      super(objects);
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(this);
    }

    public void reviewAdded(Review review)
    {
      super.addElement(review);
    }

    public void reviewChanged(Review review)
    {
    }

    public void reviewDeleted(Review review)
    {
      super.removeElement(review);
    }
  }
}
