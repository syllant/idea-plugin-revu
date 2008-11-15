package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    bnUpdatePassword.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        InputValidator inputValidator = new InputValidator()
        {
          public boolean checkInput(String inputString)
          {
            return true;
          }

          public boolean canClose(String inputString)
          {
            return true;
          }
        };

        password = Messages.showInputDialog(contentPane,
          RevuBundle.message("dialog.updatePassword.password.label"),
          RevuBundle.message("dialog.updatePassword.title"), null, "", inputValidator);
      }
    });
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