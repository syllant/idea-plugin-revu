package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.resolutiontype;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.ItemResolutionType;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemResolutionTypeDetailForm extends AbstractNameHolderDetailForm<ItemResolutionType>
{
  protected ItemResolutionTypeDetailForm(TableView<ItemResolutionType> table)
  {
    super(table);
  }
}