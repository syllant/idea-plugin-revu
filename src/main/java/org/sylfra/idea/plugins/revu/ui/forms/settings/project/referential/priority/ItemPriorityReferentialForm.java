package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ItemPriority;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.ReferentialListHolder;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemPriorityReferentialForm extends AbstractNameHolderReferentialForm<ItemPriority>
{
  public ItemPriorityReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return true;
  }

  protected AbstractUpdatableForm<ItemPriority> buildNestedFormForDialog()
  {
    return new ItemPriorityDetailForm();
  }

  @Nls
  protected String getTitleKeyForDialog(boolean addMode)
  {
    return addMode
      ? "settings.project.review.referential.itemPriority.addDialog.title"
      : "settings.project.review.referential.itemPriority.editDialog.title";
  }

  @NotNull
  protected ItemPriority createDefaultDataForDialog()
  {
    return new ItemPriority();
  }

  @Override
  protected void internalUpdateData(@NotNull ReferentialListHolder<ItemPriority> data)
  {
    super.internalUpdateData(data);

    List<ItemPriority> priorities = data.getAllItems();
    for (byte i = 0; i < priorities.size(); i++)
    {
      ItemPriority itemPriority = priorities.get(i);
      itemPriority.setOrder(i);
    }
  }

  @Override
  protected void internalUpdateUI(ReferentialListHolder<ItemPriority> data)
  {
    if (data != null)
    {
      Collections.sort(data.getAllItems());
    }
    super.internalUpdateUI(data);
  }
}