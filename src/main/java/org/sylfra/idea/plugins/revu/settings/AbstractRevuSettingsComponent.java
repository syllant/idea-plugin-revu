package org.sylfra.idea.plugins.revu.settings;

import com.intellij.openapi.components.PersistentStateComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractRevuSettingsComponent<T extends IRevuSettings> implements PersistentStateComponent<T>
{
  private T settings;
  protected List<IRevuSettingsListener<T>> listeners;

  public AbstractRevuSettingsComponent()
  {
    settings = buildDefaultSettings();
    listeners = new LinkedList<IRevuSettingsListener<T>>();
  }

  /**
   * Provided a settings bean with default values
   *
   * @return a settings bean with default values
   */
  protected abstract T buildDefaultSettings();

  /**
   * {@inheritDoc}
   */
  public T getState()
  {
    return settings;
  }

  /**
   * {@inheritDoc}
   */
  public void loadState(T object)
  {
    settings = object;

    for (IRevuSettingsListener<T> listener : listeners)
    {
      listener.settingsChanged(settings);
    }
  }

  public void addListener(IRevuSettingsListener<T> listener)
  {
    listeners.add(listener);
  }
}
