package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.user;

import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.actions.UpdataPasswordActionListener;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialDetailForm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
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
        ckReviewer.setEnabled(!ckAdmin.isSelected());
        ckAuthor.setEnabled(!ckAdmin.isSelected());
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

  protected void internalValidateInput()
  {
    updateRequiredError(tfLogin, "".equals(tfLogin.getText().trim()));
    updateRequiredError(tfDisplayName, "".equals(tfDisplayName.getText().trim()));
    updateError(tfLogin, checkAlreadyExist(tfLogin.getText(), 0), RevuBundle.message("general.valueAlreadExist.text"));
  }

  protected void internalUpdateUI(User data)
  {
    tfLogin.setText((data == null) ? "" : data.getLogin());
    tfDisplayName.setText((data == null) ? "" : data.getDisplayName());
    password = (data == null) ? null : data.getPassword();

    Set<User.Role> roles = (data == null) ? new HashSet<User.Role>() : data.getRoles();
    ckAdmin.setSelected(roles.contains(User.Role.ADMIN));
    ckReviewer.setSelected(roles.contains(User.Role.REVIEWER));
    ckAuthor.setSelected(roles.contains(User.Role.AUTHOR));
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
}
