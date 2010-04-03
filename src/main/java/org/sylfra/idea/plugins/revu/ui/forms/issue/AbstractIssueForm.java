package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractIssueForm extends AbstractUpdatableForm<Issue>
{
  @NotNull
  protected final Project project;
  protected Issue currentIssue;

  public AbstractIssueForm(@NotNull Project project)
  {
    this.project = project;
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    // This is not the standard behaviour used in other forms, but this one is not cancelable, so current review
    // item may be modified at any time, don't need to manage a copy before applying changes
    currentIssue = data;
  }
}
