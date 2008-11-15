package org.sylfra.idea.plugins.revu.settings.project;

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
  name = "reVuSettings",
  storages = {
    @Storage(
      id = "reVu",
      file = "$PROJECT_FILE$"
    )}
)
public class RevuProjectSettingsComponent extends AbstractRevuSettingsComponent<RevuProjectSettings>
  implements PersistentStateComponent<RevuProjectSettings>
{
  /**
   * {@inheritDoc}
   */
  public RevuProjectSettings buildDefaultSettings()
  {
    return new RevuProjectSettings();
  }

}
