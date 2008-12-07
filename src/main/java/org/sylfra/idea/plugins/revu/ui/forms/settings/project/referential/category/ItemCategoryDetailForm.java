package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.category;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.ItemCategory;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemCategoryDetailForm extends AbstractNameHolderDetailForm<ItemCategory>
{
  protected ItemCategoryDetailForm(TableView<ItemCategory> table)
  {
    super(table);
  }
}