package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

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
  private VirtualFile originalFile;
  private int originalLineStart;
  private int originalLineEnd;
  private String originalVcsRev;

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

  protected void internalUpdateUI(@Nullable ReviewItem data)
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    Collection<Review> reviews = reviewManager.getReviews(true, false);

    Review defaultReview = ((data == null) || (data.getReview() == null))
      ? ((reviews.size() == 1) ? reviews.iterator().next() : null)
      : data.getReview();

    originalFile = (data == null) ? null : data.getFile();
    originalLineStart = (data == null) ? -1 : data.getLineStart();
    originalLineEnd = (data == null) ? -1 : data.getLineEnd();
    originalVcsRev = (data == null) ? null : data.getVcsRev();

    cbReview.setSelectedItem(defaultReview);

    taDesc.setText((data == null) ? "" : data.getDesc());
    taTitle.setText((data == null) ? "" : data.getSummary());
    cbPriority.setSelectedItem((data == null) ? null : data.getPriority());
    cbCategory.setSelectedItem((data == null) ? null : data.getCategory());
    cbResolutionStatus.setSelectedItem((data == null) ? null : data.getResolutionStatus());
    cbResolutionType.setSelectedItem((data == null) ? null : data.getResolutionType());

    ReviewItem.LocationType locationType = (data == null) ? null : data.getLocationType();
    rbLocationFile.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType)
      && !ReviewItem.LocationType.FILE.equals(locationType));
    updateLocation(locationType);
  }

  protected void internalUpdateData(@NotNull ReviewItem data)
  {
    Review review = (Review) cbReview.getSelectedItem();

    data.setReview(review);

    data.setDesc(taDesc.getText());
    data.setSummary(taTitle.getText());
    data.setPriority((ItemPriority) cbPriority.getSelectedItem());
    data.setCategory((ItemCategory) cbCategory.getSelectedItem());
    data.setResolutionStatus((ItemResolutionStatus) cbResolutionStatus.getSelectedItem());
    data.setResolutionType((ItemResolutionType) cbResolutionType.getSelectedItem());

    // Location
    if (rbLocationGlobal.isSelected())
    {
      data.setFile(null);
      data.setLineStart(-1);
    }
    else if (rbLocationFile.isSelected())
    {
      data.setLineStart(-1);
    }
  }

  public boolean isModified(@NotNull ReviewItem data)
  {
    if (!checkEquals(taDesc.getText(), data.getDesc()))
    {
      return true;
    }

    if (!checkEquals(taTitle.getText(), data.getSummary()))
    {
      return true;
    }

    if (!checkEquals(cbReview.getSelectedItem(), data.getReview()))
    {
      return true;
    }

    if (!checkEquals(cbPriority.getSelectedItem(), data.getPriority()))
    {
      return true;
    }

    if (!checkEquals(cbCategory.getSelectedItem(), data.getCategory()))
    {
      return true;
    }

    if (!checkEquals(bgLocation.getSelection().getActionCommand(), data.getLocationType().toString()))
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
      User user = review.getDataReferential().getUser(appSettings.getLogin(), true);

      updateRequiredError(cbReview, user == null);
    }
  }

  private void updateLocation(@Nullable ReviewItem.LocationType locationType)
  {
    if (locationType == null)
    {
      rbLocationGlobal.setSelected(true);
      lbLocation.setText("");
    }

    String filePath;
    String locationPath;
    switch (locationType)
    {
      case GLOBAL:
        rbLocationGlobal.setSelected(true);
        locationPath = RevuBundle.message("form.reviewitem.main.location.global.text");
        break;

      case FILE:
        rbLocationFile.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, originalFile);
        locationPath = (originalVcsRev == null)
          ? filePath
          : RevuBundle.message("form.reviewitem.main.location.pathWithVcsRev.text", filePath, originalVcsRev); 
        break;

      default:
        rbLocationLineRange.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, originalFile);
        String filePathWithVcsRev = (originalVcsRev == null)
          ? filePath
          : RevuBundle.message("form.reviewitem.main.location.pathWithVcsRev.text", filePath, originalVcsRev);
        locationPath = RevuBundle.message("form.reviewitem.main.location.range.path.text",
          filePathWithVcsRev, (originalLineStart + 1) , (originalLineEnd + 1));
    }

    lbLocation.setText(locationPath);
  }

  private void configureUI()
  {
    RevuUtils.configureTextAreaAsStandardField(taDesc, taTitle);

    RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();

    cbResolutionStatus.setModel(new DefaultComboBoxModel(buildComboItemsArray(
      Arrays.asList(ItemResolutionStatus.values()), true)));

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    cbReview.setModel(new ReviewComboBoxModel(buildComboItemsArray(reviewManager.getReviews(null, true,
      false, appSettings.getLogin()), true)));
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
            new TreeSet<ItemPriority>(referential.getItemPrioritiesByName(true).values()), true)));

          cbCategory.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemCategory>(referential.getItemCategoriesByName(true).values()), true)));

          cbResolutionType.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemResolutionType>(referential.getItemResolutionTypesByName(true).values()), false)));
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
