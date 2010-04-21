package org.sylfra.idea.plugins.revu.ui.forms.review.referential.user;

import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.actions.UpdatePasswordActionListener;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.AbstractReferentialDetailForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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

    bnUpdatePassword.addActionListener(new UpdatePasswordActionListener(
      new UpdatePasswordActionListener.IPasswordReceiver()
      {
        public void setPassword(@Nullable String password)
        {
          UserDetailForm.this.password = password;
        }
      }));

    tfLogin.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (tfDisplayName.getText().length() == 0)
        {
          tfDisplayName.setText(tfLogin.getText());
        }
      }
    });
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
  protected void internalUpdateWriteAccess(User data, @Nullable User user)
  {
    RevuUtils.setWriteAccess((user != null) && (user.hasRole(User.Role.ADMIN)), tfLogin, tfDisplayName);
  }

  protected void internalValidateInput(@Nullable User data)
  {
    updateRequiredError(tfLogin, (data != null) && "".equals(tfLogin.getText().trim()));
    updateRequiredError(tfDisplayName, (data != null) && "".equals(tfDisplayName.getText().trim()));
    updateError(tfLogin, (data != null) && checkAlreadyExist(tfLogin.getText(), 0),
      RevuBundle.message("general.valueAlreadExist.text"));
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
}
