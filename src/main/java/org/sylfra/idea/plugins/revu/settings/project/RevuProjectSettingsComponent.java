package org.sylfra.idea.plugins.revu.settings.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
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
  name = "revuProjectSettings",
  storages = {
    @Storage(
      id = "reVu",
      file = "$PROJECT_FILE$"
    )}
)
public class RevuProjectSettingsComponent extends AbstractReviewFilesRevuSettingsComponent<RevuProjectSettings>
  implements PersistentStateComponent<RevuProjectSettings>, ProjectComponent
{
  /**
   * {@inheritDoc}
   */
  public RevuProjectSettings buildDefaultSettings()
  {
    return new RevuProjectSettings();
  }

  public RevuProjectSettings getState()
  {
    return internalGetState();
  }

  public void loadState(RevuProjectSettings state)
  {
    internalLoadState(state);
  }

  public void projectOpened()
  {

  }

  public void projectClosed()
  {

  }

  public void initComponent()
  {

  }

  public void disposeComponent()
  {

  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }
}
