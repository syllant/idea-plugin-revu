package org.sylfra.idea.plugins.revu.ui.forms.settings.app;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.actions.UpdataPasswordActionListener;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuAppSettingsForm implements ApplicationComponent, Configurable
{
  private JPanel contentPane;
  private JTextField tfLogin;
  private JButton bnUpdatePassword;
  private String password;

  public RevuAppSettingsForm()
  {
    UpdataPasswordActionListener updataPasswordActionListener = new UpdataPasswordActionListener(
      new UpdataPasswordActionListener.IPasswordReceiver()
      {
        public void setPassword(@Nullable String password)
        {
          RevuAppSettingsForm.this.password = password;
        }
      });
    bnUpdatePassword.addActionListener(updataPasswordActionListener);
  }

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".ProjectSettingsConfigurable";
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
    return "reVu";
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public Icon getIcon()
  {
    return RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU_LARGE);
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
    return contentPane;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isModified()
  {
    RevuAppSettings appSettings = retrieveAppSettings();

    return (!tfLogin.getText().equals(appSettings.getLogin()))
      || (!RevuUtils.z(password).equals(appSettings.getPassword()));
  }

  /**
   * {@inheritDoc}
   */
  public void apply() throws ConfigurationException
  {
    RevuAppSettings appSettings = retrieveAppSettings();

    appSettings.setLogin(tfLogin.getText());
    if (password != null)
    {
      appSettings.setPassword(RevuUtils.z(password));
    }
    ServiceManager.getService(RevuAppSettingsComponent.class).loadState(appSettings);
  }

  /**
   * {@inheritDoc}
   */
  public void reset()
  {
    RevuAppSettings appSettings = retrieveAppSettings();

    tfLogin.setText(appSettings.getLogin());
  }

  private RevuAppSettings retrieveAppSettings()
  {
    return ServiceManager.getService(RevuAppSettingsComponent.class).getState();
  }

  /**
   * {@inheritDoc}
   */
  public void disposeUIResources()
  {
  }

}