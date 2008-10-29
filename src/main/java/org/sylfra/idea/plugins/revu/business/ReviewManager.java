package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.ComponentProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.RevuSettings;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManager implements ProjectComponent
{
  private Project project;

  public ReviewManager(Project project)
  {
    this.project = project;
  }

  public Review getActiveReview()
  {
    RevuSettings revuSettings = ComponentProvider.getSettingsComponent(project).getState();
    for (Review review : revuSettings.getReviews())
    {
      if (review.isActive())
      {
        return review;
      }
    }

    return null;
  }

  public void addReview(Review review)
  {
    RevuSettings revuSettings = ComponentProvider.getSettingsComponent(project).getState();
    revuSettings.getReviews().add(review);
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".ReviewManager";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }
}
