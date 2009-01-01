package org.sylfra.idea.plugins.revu.ui.forms.settings.app;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColorChooser;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.actions.UpdataPasswordActionListener;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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
  private JPanel pnToResolveColor;
  private JPanel pnResolvedColor;
  private JPanel pnClosedColor;
  private JPanel pnReopenedColor;
  private Map<IssueStatus, JPanel> pnIssueStatusColors;
  private String password;

  public RevuAppSettingsForm()
  {
    pnIssueStatusColors = new HashMap<IssueStatus, JPanel>(IssueStatus.values().length);
    pnIssueStatusColors.put(IssueStatus.TO_RESOLVE, pnToResolveColor);
    pnIssueStatusColors.put(IssueStatus.RESOLVED, pnResolvedColor);
    pnIssueStatusColors.put(IssueStatus.CLOSED, pnClosedColor);
    pnIssueStatusColors.put(IssueStatus.REOPENED, pnReopenedColor);

    installListeners();
  }

  private void installListeners()
  {
    UpdataPasswordActionListener updataPasswordActionListener = new UpdataPasswordActionListener(
      new UpdataPasswordActionListener.IPasswordReceiver()
      {
        public void setPassword(@Nullable String password)
        {
          RevuAppSettingsForm.this.password = RevuUtils.z(password, null);
        }
      });
    bnUpdatePassword.addActionListener(updataPasswordActionListener);

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

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$()
  {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new BorderLayout(0, 0));
    contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel1.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("appSettings.auth.title")));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    panel1.add(panel2, BorderLayout.CENTER);
    panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    final JLabel label1 = new JLabel();
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("appSettings.login.label"));
    panel2.add(label1);
    tfLogin = new JTextField();
    tfLogin.setColumns(20);
    panel2.add(tfLogin);
    final Spacer spacer1 = new Spacer();
    panel2.add(spacer1);
    bnUpdatePassword = new JButton();
    this.$$$loadButtonText$$$(bnUpdatePassword,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "appSettings.updatePassword.text"));
    bnUpdatePassword.setVisible(false);
    panel2.add(bnUpdatePassword);
    final JLabel label2 = new JLabel();
    label2.setHorizontalAlignment(4);
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "appSettings.passwordHint.text"));
    label2.setVisible(false);
    panel1.add(label2, BorderLayout.SOUTH);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel3.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("appSettings.colors.title")));
    final JLabel label3 = new JLabel();
    this.$$$loadLabelText$$$(label3,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "general.issueStatus.to_resolve.text"));
    panel3.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    panel3.add(spacer2, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    pnToResolveColor = new JPanel();
    pnToResolveColor.setLayout(new BorderLayout(0, 0));
    panel3.add(pnToResolveColor,
      new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20),
        new Dimension(20, 20), new Dimension(20, 20), 0, false));
    final JLabel label4 = new JLabel();
    this.$$$loadLabelText$$$(label4,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "general.issueStatus.closed.text"));
    panel3.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    pnClosedColor = new JPanel();
    pnClosedColor.setLayout(new BorderLayout(0, 0));
    panel3.add(pnClosedColor, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20), new Dimension(20, 20),
      new Dimension(20, 20), 0, false));
    final JLabel label5 = new JLabel();
    this.$$$loadLabelText$$$(label5,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "general.issueStatus.resolved.text"));
    panel3.add(label5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
    pnResolvedColor = new JPanel();
    pnResolvedColor.setLayout(new BorderLayout(0, 0));
    panel3.add(pnResolvedColor,
      new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20),
        new Dimension(20, 20), new Dimension(20, 20), 0, false));
    final JLabel label6 = new JLabel();
    this.$$$loadLabelText$$$(label6,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "general.issueStatus.reopened.text"));
    panel3.add(label6, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
    pnReopenedColor = new JPanel();
    pnReopenedColor.setLayout(new BorderLayout(0, 0));
    panel3.add(pnReopenedColor,
      new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(20, 20),
        new Dimension(20, 20), new Dimension(20, 20), 0, false));
    final Spacer spacer3 = new Spacer();
    contentPane.add(spacer3,
      new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    label1.setLabelFor(tfLogin);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}