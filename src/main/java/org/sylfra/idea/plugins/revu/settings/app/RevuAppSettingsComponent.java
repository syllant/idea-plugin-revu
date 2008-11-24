package org.sylfra.idea.plugins.revu.settings.app;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.sylfra.idea.plugins.revu.settings.AbstractRevuSettingsComponent;

/**
 * Manage plugin settings
 *
 * NB: has to declare implementing PersistentStateComponent even if parent class already does because of IDEA
 * introspection mechanism ({@link com.intellij.util.ReflectionUtil#getRawType(java.lang.reflect.Type)}
 * 
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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
  implements PersistentStateComponent<RevuAppSettings>
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
}