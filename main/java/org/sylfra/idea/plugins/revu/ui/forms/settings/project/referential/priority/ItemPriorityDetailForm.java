package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.IssuePriority;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemPriorityDetailForm extends AbstractNameHolderDetailForm<IssuePriority>
{
  protected ItemPriorityDetailForm(TableView<IssuePriority> table)
  {
    super(table);
  }
}