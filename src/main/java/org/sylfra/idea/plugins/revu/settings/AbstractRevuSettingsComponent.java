package org.sylfra.idea.plugins.revu.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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
  public T internalGetState()
  {
    return settings;
  }

  /**
   * {@inheritDoc}
   */
  public void internalLoadState(T object)
  {
    settings = object;

    // Defensive copy against concurrent modifications
    List<IRevuSettingsListener<T>> copy = new ArrayList<IRevuSettingsListener<T>>(listeners);
    for (IRevuSettingsListener<T> listener : copy)
    {
      listener.settingsChanged(settings);
    }
  }

  public void addListener(@NotNull IRevuSettingsListener<T> listener)
  {
    listeners.add(listener);
  }

  public void removeListener(@NotNull IRevuSettingsListener<T> listener)
  {
    listeners.remove(listener);
  }
}
