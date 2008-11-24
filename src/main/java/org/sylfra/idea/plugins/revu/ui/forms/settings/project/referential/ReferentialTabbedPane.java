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

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReferentialTabbedPane extends AbstractUpdatableForm<DataReferential>
{
  private final Project project;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private JButton bnImportLink;
  private JLabel lbLink;
  private UserReferentialForm userReferentialForm;
  private ItemCategoryReferentialForm itemCategoryReferentialForm;
  private ItemPriorityReferentialForm itemPriorityReferentialForm;
  private ItemResolutionTypeReferentialForm itemResolutionTypeReferentialForm;

  public ReferentialTabbedPane(Project project)
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
    return (userReferentialForm.isModified(new ArrayList<User>(data.getUsersByLogin().values())))
      || (itemCategoryReferentialForm.isModified(new ArrayList<ItemCategory>(data.getItemCategoriesByName().values())))
      || (itemPriorityReferentialForm.isModified(new ArrayList<ItemPriority>(data.getItemPrioritiesByName().values())))
      || (itemResolutionTypeReferentialForm.isModified(new ArrayList<ItemResolutionType>(data.getItemResolutionTypesByName().values())));
  }

  @Override
  public void dispose()
  {
    userReferentialForm.dispose();
    itemCategoryReferentialForm.dispose();
    itemPriorityReferentialForm.dispose();
    itemResolutionTypeReferentialForm.dispose();
  }

  protected void internalValidateInput()
  {
  }

  protected void internalUpdateUI(@NotNull DataReferential data)
  {
    userReferentialForm.updateUI(new ArrayList<User>(data.getUsersByLogin().values()));
    itemCategoryReferentialForm.updateUI(new ArrayList<ItemCategory>(data.getItemCategoriesByName().values()));
    itemPriorityReferentialForm.updateUI(new ArrayList<ItemPriority>(data.getItemPrioritiesByName().values()));
    itemResolutionTypeReferentialForm.updateUI(new ArrayList<ItemResolutionType>(data.getItemResolutionTypesByName().values()));
  }

  protected void internalUpdateData(@NotNull DataReferential data)
  {
    ArrayList<User> userList = new ArrayList<User>(data.getUsersByLogin().values());
    userReferentialForm.updateData(userList);
    data.setUsers(userList);

    ArrayList<ItemCategory> itemCategoryList = new ArrayList<ItemCategory>(data.getItemCategoriesByName().values());
    itemCategoryReferentialForm.updateData(itemCategoryList);
    data.setItemCategories(itemCategoryList);

    ArrayList<ItemPriority> itemPriorityList = new ArrayList<ItemPriority>(data.getItemPrioritiesByName().values());
    itemPriorityReferentialForm.updateData(itemPriorityList);
    data.setItemPriorities(itemPriorityList);

    ArrayList<ItemResolutionType> itemResolutionTypes = new ArrayList<ItemResolutionType>(data.getItemResolutionTypesByName().values());
    itemResolutionTypeReferentialForm.updateData(itemResolutionTypes);
    data.setItemResolutionTypes(itemResolutionTypes);
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
