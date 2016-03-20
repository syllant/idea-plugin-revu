package org.sylfra.idea.plugins.revu.settings.app;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.settings.AbstractRevuSettingsComponent;

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
  name = "revuAppSettings",
  storages = {
    @Storage(
      id = "reVu",
      file = "$APP_CONFIG$/reVu.xml"
    )}
)
public class RevuAppSettingsComponent extends AbstractRevuSettingsComponent<RevuAppSettings>
  implements PersistentStateComponent<RevuAppSettings>, ApplicationComponent
{
  /**
   * {@inheritDoc}
   */
  public RevuAppSettings buildDefaultSettings()
  {
    return new RevuAppSettings();
  }

  public RevuAppSettings getState()
  {
    return internalGetState();
  }

  public void loadState(RevuAppSettings state)
  {
    internalLoadState(state);
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
    return RevuPlugin.PLUGIN_NAME;
  }
}