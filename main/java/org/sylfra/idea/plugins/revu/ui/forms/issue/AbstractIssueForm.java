package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractIssueForm extends AbstractUpdatableForm<Issue>
{
  @NotNull
  protected final Project project;

  public AbstractIssueForm(@NotNull Project project)
  {
    this.project = project;
  }
}
