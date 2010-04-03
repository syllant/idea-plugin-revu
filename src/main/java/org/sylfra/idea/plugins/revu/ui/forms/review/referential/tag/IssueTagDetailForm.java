package org.sylfra.idea.plugins.revu.ui.forms.review.referential.tag;

import com.intellij.ui.table.TableView;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.AbstractNameHolderDetailForm;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTagDetailForm extends AbstractNameHolderDetailForm<IssueTag>
{
  protected IssueTagDetailForm(TableView<IssueTag> table)
  {
    super(table);
  }
}