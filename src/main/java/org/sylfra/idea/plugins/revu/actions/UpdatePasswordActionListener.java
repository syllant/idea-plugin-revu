package org.sylfra.idea.plugins.revu.actions;

import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class UpdatePasswordActionListener implements ActionListener
{
  private IPasswordReceiver passwordReceiver;

  public UpdatePasswordActionListener(IPasswordReceiver passwordReceiver)
  {
    this.passwordReceiver = passwordReceiver;
  }

  public void actionPerformed(ActionEvent e)
  {
    String password = Messages.showPasswordDialog(RevuBundle.message("dialog.updatePassword.password.label"),
      RevuBundle.message("dialog.updatePassword.title"));
    if (password != null)
    {
      passwordReceiver.setPassword(password.trim());
    }
  }

  public static interface IPasswordReceiver
  {
    void setPassword(@Nullable String password);
  }
}
