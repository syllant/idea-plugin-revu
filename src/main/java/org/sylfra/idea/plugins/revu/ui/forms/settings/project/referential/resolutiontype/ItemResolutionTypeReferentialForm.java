package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.resolutiontype;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ItemResolutionType;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemResolutionTypeReferentialForm extends AbstractNameHolderReferentialForm<ItemResolutionType>
{
  public ItemResolutionTypeReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return false;
  }

  protected AbstractReferentialDetailForm<ItemResolutionType> buildNestedFormForDialog()
  {
    return new ItemResolutionTypeDetailForm(table);
  }

  @Nls
  protected String getTitleKeyForDialog(boolean addMode)
  {
    return addMode
      ? "settings.project.review.referential.itemResolutionType.addDialog.title"
      : "settings.project.review.referential.itemResolutionType.editDialog.title";
  }

  @NotNull
  protected ItemResolutionType createDefaultDataForDialog()
  {
    return new ItemResolutionType();
  }
}