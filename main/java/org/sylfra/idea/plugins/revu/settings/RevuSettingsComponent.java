package org.sylfra.idea.plugins.revu.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

/**
 * Manage plugin settings
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
@State(
  name = "reVuSettings",
  storages = {
    @Storage(
      id = "reVu",
      file = "$PROJECT_FILE$/../reVu.xml"
    )}
)
public class RevuSettingsComponent
  implements PersistentStateComponent<RevuSettings>
{
  private RevuSettings settings;

  public RevuSettingsComponent()
  {
    settings = getDefaultSettings();
  }

  /**
   * Provided a settings bean with default values
   *
   * @return a settings bean with default values
   */
  public RevuSettings getDefaultSettings()
  {
    RevuSettings defaultSettings = new RevuSettings();

    return defaultSettings;
  }

  /**
   * {@inheritDoc}
   */
  public RevuSettings getState()
  {
    return settings;
  }

  /**
   * {@inheritDoc}
   */
  public void loadState(RevuSettings object)
  {
    settings = object;
  }
}
