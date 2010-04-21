package org.sylfra.idea.plugins.revu.settings.project.workspace;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.AbstractReviewFilesRevuSettingsComponent;

/**
 * Manage plugin settings
 *
 * NB: has to declare implementing PersistentStateComponent even if parent class already does because of IDEA
 * introspection mechanism ({@link com.intellij.util.ReflectionUtil#getRawType(java.lang.reflect.Type)}
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
@State(
  name = "revuWorkspaceSettings",
  storages = {
    @Storage(
      id = "reVu",
      file = "$WORKSPACE_FILE$"
    )}
)
public class RevuWorkspaceSettingsComponent extends AbstractReviewFilesRevuSettingsComponent<RevuWorkspaceSettings>
  implements PersistentStateComponent<RevuWorkspaceSettings>, ProjectComponent
{
  private final Project project;

  public RevuWorkspaceSettingsComponent(Project project)
  {
    this.project = project;
  }

  /**
   * {@inheritDoc}
   */
  public RevuWorkspaceSettings buildDefaultSettings()
  {
    return new RevuWorkspaceSettings();
  }

  public RevuWorkspaceSettings getState()
  {
    return internalGetState();
  }

  public void loadState(RevuWorkspaceSettings state)
  {
    internalLoadState(state);
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
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  public void initComponent()
  {
    project.getComponent(ReviewManager.class).addReviewListener(new IReviewListener()
    {
      public void reviewChanged(Review review)
      {
      }

      public void reviewAdded(Review review)
      {
      }

      // Review being reviewed is deleted -> remove it from settings
      public void reviewDeleted(Review review)
      {
        RevuWorkspaceSettings state = getState();
        String activeReviewName = state.getReviewingReviewName();
        if ((activeReviewName != null) && (activeReviewName.equals(review.getName())))
        {
          state.setReviewingReviewName(null);
          loadState(state);
        }
      }
    });
  }

  public void disposeComponent()
  {
  }
}