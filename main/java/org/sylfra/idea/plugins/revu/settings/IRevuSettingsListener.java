package org.sylfra.idea.plugins.revu.settings;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuSettingsListener<T extends IRevuSettings>
{
  void settingsChanged(T settings);
}
