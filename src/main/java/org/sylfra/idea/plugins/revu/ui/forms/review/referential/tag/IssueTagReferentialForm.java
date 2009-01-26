package org.sylfra.idea.plugins.revu.ui.forms.review.referential.tag;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.AbstractNameHolderReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.AbstractReferentialDetailForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTagReferentialForm extends AbstractNameHolderReferentialForm<IssueTag>
{
  public IssueTagReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return false;
  }

  protected AbstractReferentialDetailForm<IssueTag> buildNestedFormForDialog()
  {
    return new IssueTagDetailForm(table);
  }

  @Nls
  protected String getTitleKeyForDialog(boolean addMode)
  {
    return addMode
      ? "projectSettings.review.referential.issueTag.addDialog.title"
      : "projectSettings.review.referential.issueTag.editDialog.title";
  }

  @NotNull
  protected IssueTag createDefaultDataForDialog()
  {
    return new IssueTag();
  }
}