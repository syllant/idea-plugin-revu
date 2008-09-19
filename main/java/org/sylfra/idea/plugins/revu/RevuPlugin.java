package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.settings.RevuSettingsComponent;

/**
 * The main application component available as a singleton and providing convenient methods
 * to access services declared in plugin.xml
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuPlugin implements ApplicationComponent
{
  public static final String COMPONENT_NAME = "revu";
  public static final String PLUGIN_ID = "org.sylfra.idea.plugins.revu";

  private static final Logger LOGGER =
    Logger.getInstance(RevuPlugin.class.getName());

  public static RevuPlugin getInstance()
  {
    return ApplicationManager.getApplication().getComponent(RevuPlugin.class);
  }

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return COMPONENT_NAME;
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent()
  {
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent()
  {
  }

  /**
   * Provides settings component
   *
   * @return settings component
   */
  public RevuSettingsComponent getSettingsComponent()
  {
    return ServiceManager.getService(RevuSettingsComponent.class);
  }
}
