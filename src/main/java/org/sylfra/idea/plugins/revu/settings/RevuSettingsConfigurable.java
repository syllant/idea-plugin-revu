package org.sylfra.idea.plugins.revu.settings;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuIconManager;
import org.sylfra.idea.plugins.revu.RevuPlugin;

import javax.swing.*;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuSettingsConfigurable implements ApplicationComponent, Configurable
{
  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.COMPONENT_NAME + ".SettingsConfigurable";
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
   * {@inheritDoc}
   */
  @Nls
  public String getDisplayName()
  {
    return "revu";
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public Icon getIcon()
  {
    return RevuIconManager.getIcon(RevuIconManager.IconRef.revuLarge);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @NonNls
  public String getHelpTopic()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public JComponent createComponent()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isModified()
  {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void apply() throws ConfigurationException
  {
  }

  /**
   * {@inheritDoc}
   */
  public void reset()
  {
  }

  /**
   * {@inheritDoc}
   */
  public void disposeUIResources()
  {
  }
}