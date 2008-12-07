package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.ItemPriority;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemPriorityDetailForm extends AbstractNameHolderDetailForm<ItemPriority>
{
  protected ItemPriorityDetailForm(TableView<ItemPriority> table)
  {
    super(table);
  }
}