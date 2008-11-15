package org.sylfra.idea.plugins.revu.settings.project.workspace;

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
      file = "$WORKSPACE_FILE$"
    )}
)
public class RevuWorkspaceSettingsComponent extends AbstractRevuSettingsComponent<RevuWorkspaceSettings>
  implements PersistentStateComponent<RevuWorkspaceSettings>
{
  /**
   * {@inheritDoc}
   */
  public RevuWorkspaceSettings buildDefaultSettings()
  {
    return new RevuWorkspaceSettings();
  }
}