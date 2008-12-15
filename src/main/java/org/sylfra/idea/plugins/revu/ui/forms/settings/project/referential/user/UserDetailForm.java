package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.user;

import com.intellij.ui.table.TableView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.actions.UpdataPasswordActionListener;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialDetailForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class UserDetailForm extends AbstractReferentialDetailForm<User>
{
  private JPanel contentPane;
  private JTextField tfLogin;
  private JTextField tfDisplayName;
  private JCheckBox ckAdmin;
  private JCheckBox ckReviewer;
  private JCheckBox ckAuthor;
  private JButton bnUpdatePassword;
  private String password;

  protected UserDetailForm(TableView<User> table)
  {
    super(table);

    UpdataPasswordActionListener updataPasswordActionListener = new UpdataPasswordActionListener(
      new UpdataPasswordActionListener.IPasswordReceiver()
      {
        public void setPassword(@Nullable String password)
        {
          UserDetailForm.this.password = password;
        }
      });
    bnUpdatePassword.addActionListener(updataPasswordActionListener);

    ckAdmin.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (ckAdmin.isSelected())
        {
          ckReviewer.setSelected(true);
          ckAuthor.setSelected(true);
        }
        ckReviewer.setEnabled(!ckAdmin.isSelected());
        ckAuthor.setEnabled(!ckReviewer.isSelected());
      }
    });

    ckReviewer.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (ckReviewer.isSelected())
        {
          ckAuthor.setSelected(true);
        }
        ckAuthor.setEnabled(!ckReviewer.isSelected());
      }
    });
  }

  public boolean isModified(@NotNull User data)
  {
    if (!checkEquals(tfLogin.getText(), data.getLogin()))
    {
      return true;
    }

    if (!checkEquals(tfDisplayName.getText(), data.getDisplayName()))
    {
      return true;
    }

    if (!checkEquals(password, data.getPassword()))
    {
      return true;
    }

    Set<User.Role> roles = data.getRoles();
    if ((ckAdmin.isSelected() != roles.contains(User.Role.ADMIN))
      || (ckReviewer.isSelected() != roles.contains(User.Role.REVIEWER))
      || (ckAuthor.isSelected() != roles.contains(User.Role.AUTHOR)))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    RevuUtils.setWriteAccess((user != null) && (user.hasRole(User.Role.ADMIN)), tfLogin, tfDisplayName);
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfLogin, "".equals(tfLogin.getText().trim()));
    updateRequiredError(tfDisplayName, "".equals(tfDisplayName.getText().trim()));
    updateError(tfLogin, checkAlreadyExist(tfLogin.getText(), 0), RevuBundle.message("general.valueAlreadExist.text"));
  }

  protected void internalUpdateUI(User data, boolean requestFocus)
  {
    tfLogin.setText((data == null) ? "" : data.getLogin());
    tfDisplayName.setText((data == null) ? "" : data.getDisplayName());
    password = (data == null) ? null : data.getPassword();

    ckAdmin.setSelected((data != null) && (data.hasRole(User.Role.ADMIN)));
    ckReviewer.setSelected((data != null) && (data.hasRole(User.Role.REVIEWER)));
    ckAuthor.setSelected(true);

    ckReviewer.setEnabled(!ckAdmin.isSelected());
    ckAuthor.setEnabled(!ckReviewer.isSelected());
  }

  protected void internalUpdateData(@NotNull User data)
  {
    data.setLogin(tfLogin.getText());
    data.setDisplayName(tfDisplayName.getText());

    if (password != null)
    {
      data.setPassword(password);
    }

    Set<User.Role> roles = new HashSet<User.Role>();
    if (ckAdmin.isSelected())
    {
      roles.add(User.Role.ADMIN);
    }
    if (ckReviewer.isSelected())
    {
      roles.add(User.Role.REVIEWER);
    }
    if (ckAuthor.isSelected())
    {
      roles.add(User.Role.AUTHOR);
    }
    data.setRoles(roles);
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfLogin;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
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
    contentPane.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
    final JLabel label1 = new JLabel();
    label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, label1.getFont().getSize()));
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.user.form.login.label"));
    contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    contentPane.add(spacer1,
      new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    tfLogin = new JTextField();
    contentPane.add(tfLogin,
      new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
        false));
    final JLabel label2 = new JLabel();
    label2.setFont(new Font(label2.getFont().getName(), Font.BOLD, label2.getFont().getSize()));
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.user.form.displayName.label"));
    contentPane.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    tfDisplayName = new JTextField();
    contentPane.add(tfDisplayName,
      new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
        false));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel1.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.user.form.roles.title")));
    ckAdmin = new JCheckBox();
    this.$$$loadButtonText$$$(ckAdmin,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("userRoles.admin.cklabel"));
    panel1.add(ckAdmin, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    panel1.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    ckReviewer = new JCheckBox();
    this.$$$loadButtonText$$$(ckReviewer,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "userRoles.reviewer.cklabel"));
    panel1.add(ckReviewer, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    ckAuthor = new JCheckBox();
    this.$$$loadButtonText$$$(ckAuthor,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("userRoles.author.cklabel"));
    panel1.add(ckAuthor, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    bnUpdatePassword = new JButton();
    this.$$$loadButtonText$$$(bnUpdatePassword,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.user.form.updatePassword.text"));
    bnUpdatePassword.setVisible(false);
    contentPane.add(bnUpdatePassword,
      new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
        null, null, null, 0, false));
    label1.setLabelFor(tfLogin);
    label2.setLabelFor(tfDisplayName);
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
