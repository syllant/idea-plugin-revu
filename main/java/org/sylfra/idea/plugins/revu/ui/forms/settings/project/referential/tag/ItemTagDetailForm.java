package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.tag;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemTagDetailForm extends AbstractNameHolderDetailForm<IssueTag>
{
  protected ItemTagDetailForm(TableView<IssueTag> table)
  {
    super(table);
  }
}