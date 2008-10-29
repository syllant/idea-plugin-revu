package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.settings.RevuSettingsComponent;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ComponentProvider
{
  private ComponentProvider()
  {
  }

  /**
   * Provides settings component
   *
   * @param project
   *
   * @return settings component
   */
  public static RevuSettingsComponent getSettingsComponent(Project project)
  {
    return ServiceManager.getService(project, RevuSettingsComponent.class);
  }

  public static ReviewManager getReviewManager(Project project)
  {
    return ServiceManager.getService(project, ReviewManager.class);
  }
}
