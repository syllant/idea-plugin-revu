package org.sylfra.idea.plugins.revu.ui.projectView;

import com.intellij.ide.scopeView.ScopeTreeViewPanel;
import com.intellij.openapi.project.Project;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuScopeTreeViewPanel extends ScopeTreeViewPanel
{
  public RevuScopeTreeViewPanel(Project project)
  {
    super(project);
  }

  public String getCurrentScopeName()
  {
    return super.getCurrentScope().getName();
  }
}
