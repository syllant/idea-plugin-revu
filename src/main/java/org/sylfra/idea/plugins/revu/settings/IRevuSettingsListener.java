package org.sylfra.idea.plugins.revu.settings;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: IRevuSettingsListener.java 7 2008-11-15 09:20:32Z syllant $
 */
public interface IRevuSettingsListener<T extends IRevuSettings>
{
  void settingsChanged(T oldSettings, T newSettings);
}
