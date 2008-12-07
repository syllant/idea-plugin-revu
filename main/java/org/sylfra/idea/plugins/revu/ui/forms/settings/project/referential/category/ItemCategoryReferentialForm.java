package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.category;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ItemCategory;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemCategoryReferentialForm extends AbstractNameHolderReferentialForm<ItemCategory>
{
  public ItemCategoryReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return false;
  }

  protected AbstractReferentialDetailForm<ItemCategory> buildNestedFormForDialog()
  {
    return new ItemCategoryDetailForm(table);
  }

  @Nls
  protected String getTitleKeyForDialog(boolean addMode)
  {
    return addMode
      ? "settings.project.review.referential.itemCategory.addDialog.title"
      : "settings.project.review.referential.itemCategory.editDialog.title";
  }

  @NotNull
  protected ItemCategory createDefaultDataForDialog()
  {
    return new ItemCategory();
  }
}