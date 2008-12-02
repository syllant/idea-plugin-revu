package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.category.ItemCategoryReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority.ItemPriorityReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.resolutiontype.ItemResolutionTypeReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.user.UserReferentialForm;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReferentialTabbedPane extends AbstractUpdatableForm<DataReferential>
{
  private final Project project;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private UserReferentialForm userReferentialForm;
  private ItemCategoryReferentialForm itemCategoryReferentialForm;
  private ItemPriorityReferentialForm itemPriorityReferentialForm;
  private ItemResolutionTypeReferentialForm itemResolutionTypeReferentialForm;

  public ReferentialTabbedPane(final Project project)
  {
    this.project = project;

    userReferentialForm = new UserReferentialForm(project);
    itemCategoryReferentialForm = new ItemCategoryReferentialForm(project);
    itemPriorityReferentialForm = new ItemPriorityReferentialForm(project);
    itemResolutionTypeReferentialForm = new ItemResolutionTypeReferentialForm(project);
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.user.title"),
      userReferentialForm.getContentPane());
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemCategory.title"),
      itemCategoryReferentialForm.getContentPane());
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemPriority.title"),
      itemPriorityReferentialForm.getContentPane());
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemResolutionType.title"),
      itemResolutionTypeReferentialForm.getContentPane());
  }


  public boolean isModified(@NotNull DataReferential data)
  {
    return ((userReferentialForm.isModified(new ReferentialListHolder<User>(getSortedValues(data.getUsers(true)), null)))
      || (itemCategoryReferentialForm.isModified(new ReferentialListHolder<ItemCategory>(getSortedValues(data.getItemCategories(
      true)), null)))
      || (itemPriorityReferentialForm.isModified(new ReferentialListHolder<ItemPriority>(getSortedValues(data.getItemPriorities(
      true)), null)))
      || (itemResolutionTypeReferentialForm.isModified(new ReferentialListHolder<ItemResolutionType>(getSortedValues(data.getItemResolutionTypes(
      true)), null))));
  }

  protected void internalValidateInput()
  {
    updateError(userReferentialForm.getContentPane(), !userReferentialForm.validateInput(), null);
    updateError(itemCategoryReferentialForm.getContentPane(), !itemCategoryReferentialForm.validateInput(), null);
    updateError(itemPriorityReferentialForm.getContentPane(), !itemPriorityReferentialForm.validateInput(), null);
    updateError(itemResolutionTypeReferentialForm.getContentPane(), !itemResolutionTypeReferentialForm.validateInput(), null);
  }

  protected void internalUpdateUI(DataReferential data)
  {
    ReferentialListHolder<User> userHolder = (data == null)
      ? new ReferentialListHolder<User>(new ArrayList<User>(), null)
      : new ReferentialListHolder<User>(getSortedValues(data.getUsers(true)),
      (data.getReview().getExtendedReview() == null)
        ? null : getSortedValues(data.getReview().getExtendedReview().getDataReferential().getUsers(true)));
    userReferentialForm.updateUI(userHolder);

    ReferentialListHolder<ItemCategory> categoryHolder = (data == null)
      ? new ReferentialListHolder<ItemCategory>(new ArrayList<ItemCategory>(), null)
      : new ReferentialListHolder<ItemCategory>(getSortedValues(data.getItemCategories(true)),
      (data.getReview().getExtendedReview() == null)
        ? null : getSortedValues(data.getReview().getExtendedReview().getDataReferential().getItemCategories(true)));
    itemCategoryReferentialForm.updateUI(categoryHolder);

    ReferentialListHolder<ItemPriority> priorityHolder = (data == null)
      ? new ReferentialListHolder<ItemPriority>(new ArrayList<ItemPriority>(), null)
      : new ReferentialListHolder<ItemPriority>(getSortedValues(data.getItemPriorities(true)),
      (data.getReview().getExtendedReview() == null)
        ? null : getSortedValues(data.getReview().getExtendedReview().getDataReferential().getItemPriorities(true)));
    itemPriorityReferentialForm.updateUI(priorityHolder);

    ReferentialListHolder<ItemResolutionType> resolutionTypeHolder = (data == null)
      ? new ReferentialListHolder<ItemResolutionType>(new ArrayList<ItemResolutionType>(), null)
      : new ReferentialListHolder<ItemResolutionType>(getSortedValues(data.getItemResolutionTypes(true)),
      (data.getReview().getExtendedReview() == null)
        ? null : getSortedValues(data.getReview().getExtendedReview().getDataReferential().getItemResolutionTypes(true)));
    itemResolutionTypeReferentialForm.updateUI(resolutionTypeHolder);
  }

  protected void internalUpdateData(@NotNull DataReferential data)
  {
    ReferentialListHolder<User> userHolder = new ReferentialListHolder<User>(new ArrayList<User>(), null);
    userReferentialForm.updateData(userHolder);
    data.setUsers(userHolder.getAllItems());

    ReferentialListHolder<ItemCategory> categoryHolder
      = new ReferentialListHolder<ItemCategory>(new ArrayList<ItemCategory>(), null);
    itemCategoryReferentialForm.updateData(categoryHolder);
    data.setItemCategories(categoryHolder.getAllItems());

    ReferentialListHolder<ItemPriority> priorityHolder
      = new ReferentialListHolder<ItemPriority>(new ArrayList<ItemPriority>(), null);
    itemPriorityReferentialForm.updateData(priorityHolder);
    data.setItemPriorities(priorityHolder.getAllItems());

    ReferentialListHolder<ItemResolutionType> resolutionTypeHolder
      = new ReferentialListHolder<ItemResolutionType>(new ArrayList<ItemResolutionType>(), null);
    itemResolutionTypeReferentialForm.updateData(resolutionTypeHolder);
    data.setItemResolutionTypes(resolutionTypeHolder.getAllItems());
  }

  private <T extends Comparable> List<T> getSortedValues(List<T> list)
  {
    list = new ArrayList<T>(list);
    Collections.sort(list);

    return list;
  }

  @Override
  public void dispose()
  {
    userReferentialForm.dispose();
    itemCategoryReferentialForm.dispose();
    itemPriorityReferentialForm.dispose();
    itemResolutionTypeReferentialForm.dispose();
  }

  public JComponent getPreferredFocusedComponent()
  {
    // @TODO
    return contentPane;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

}
