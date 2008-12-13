package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.tag;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractNameHolderReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemTagReferentialForm extends AbstractNameHolderReferentialForm<IssueTag>
{
  public ItemTagReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return false;
  }

  protected AbstractReferentialDetailForm<IssueTag> buildNestedFormForDialog()
  {
    return new org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.tag.ItemTagDetailForm(table);
  }

  @Nls
  protected String getTitleKeyForDialog(boolean addMode)
  {
    return addMode
      ? "settings.project.review.referential.itemTag.addDialog.title"
      : "settings.project.review.referential.itemTag.editDialog.title";
  }

  @NotNull
  protected IssueTag createDefaultDataForDialog()
  {
    return new IssueTag();
  }
}