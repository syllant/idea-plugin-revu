package org.sylfra.idea.plugins.revu.ui.forms.settings;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColorChooser;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.actions.UpdatePasswordActionListener;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuAppSettingsForm implements ApplicationComponent, Configurable
{
  private JPanel contentPane;
  private JTextField tfLogin;
  private JButton bnUpdatePassword;
  private JPanel pnStatusToResolveColor;
  private JPanel pnStatusResolvedColor;
  private JPanel pnStatusClosedColor;
  private JPanel pnStatusReopenedColor;
  private JPanel pnSelectionBackgroundColor;
  private JPanel pnSelectionForegroundColor;
  private Map<IssueStatus, JPanel> pnIssueStatusColors;
  private String password;

  public RevuAppSettingsForm()
  {
    pnIssueStatusColors = new HashMap<IssueStatus, JPanel>(IssueStatus.values().length);
    pnIssueStatusColors.put(IssueStatus.TO_RESOLVE, pnStatusToResolveColor);
    pnIssueStatusColors.put(IssueStatus.RESOLVED, pnStatusResolvedColor);
    pnIssueStatusColors.put(IssueStatus.CLOSED, pnStatusClosedColor);
    pnIssueStatusColors.put(IssueStatus.REOPENED, pnStatusReopenedColor);

    installListeners();
  }

  private void installListeners()
  {
    UpdatePasswordActionListener updatePasswordActionListener = new UpdatePasswordActionListener(
      new UpdatePasswordActionListener.IPasswordReceiver()
      {
        public void setPassword(@Nullable String password)
        {
          RevuAppSettingsForm.this.password = RevuUtils.z(password, null);
        }
      });
    bnUpdatePassword.addActionListener(updatePasswordActionListener);

    MouseListener colorMouseListener = new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent e)
      {
        JComponent parent = (JComponent) e.getSource();
        parent.setBorder(BorderFactory.createLineBorder(Color.WHITE));
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        JComponent parent = (JComponent) e.getSource();
        parent.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      }

      @Override
      public void mouseClicked(MouseEvent e)
      {
        Component parent = (Component) e.getSource();
        Color color = ColorChooser.chooseColor(parent,
          RevuBundle.message("general.selectColor.text"), parent.getBackground());
        if (color != null)
        {
          parent.setBackground(color);
        }
      }
    };
    for (JPanel panel : pnIssueStatusColors.values())
    {
      panel.addMouseListener(colorMouseListener);
      panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    pnSelectionBackgroundColor.addMouseListener(colorMouseListener);
    pnSelectionBackgroundColor.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    pnSelectionForegroundColor.addMouseListener(colorMouseListener);
    pnSelectionForegroundColor.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
  }

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
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

    if ((!tfLogin.getText().equals(appSettings.getLogin()))
      || (!RevuUtils.z(password, null).equals(appSettings.getPassword())))
    {
      return true;
    }

    for (Map.Entry<IssueStatus, JPanel> entry : pnIssueStatusColors.entrySet())
    {
      if (!appSettings.getIssueStatusColors().get(entry.getKey()).equals(
        RevuUtils.getHex(entry.getValue().getBackground())))
      {
        return true;
      }
    }

    if (!appSettings.getTableSelectionBackgroundColor().equals(
      RevuUtils.getHex(pnSelectionBackgroundColor.getBackground())))
    {
      return true;
    }

    if (!appSettings.getTableSelectionForegroundColor().equals(
      RevuUtils.getHex(pnSelectionForegroundColor.getBackground())))
    {
      return true;
    }

    return false;
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
      appSettings.setPassword(RevuUtils.z(password, null));
    }

    for (Map.Entry<IssueStatus, JPanel> entry : pnIssueStatusColors.entrySet())
    {
      appSettings.getIssueStatusColors().put(entry.getKey(), RevuUtils.getHex(entry.getValue().getBackground()));
    }

    appSettings.setTableSelectionBackgroundColor(RevuUtils.getHex(pnSelectionBackgroundColor.getBackground()));
    appSettings.setTableSelectionForegroundColor(RevuUtils.getHex(pnSelectionForegroundColor.getBackground()));

    ServiceManager.getService(RevuAppSettingsComponent.class).loadState(appSettings);
  }

  /**
   * {@inheritDoc}
   */
  public void reset()
  {
    RevuAppSettings appSettings = retrieveAppSettings();

    tfLogin.setText(appSettings.getLogin());

    for (Map.Entry<IssueStatus, JPanel> entry : pnIssueStatusColors.entrySet())
    {
      entry.getValue().setBackground(Color.decode(appSettings.getIssueStatusColors().get(entry.getKey())));
    }

    pnSelectionBackgroundColor.setBackground(Color.decode(appSettings.getTableSelectionBackgroundColor()));
    pnSelectionForegroundColor.setBackground(Color.decode(appSettings.getTableSelectionForegroundColor()));
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