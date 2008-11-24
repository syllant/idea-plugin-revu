package org.sylfra.idea.plugins.revu.settings;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractRevuSettingsComponent<T extends IRevuSettings>
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
