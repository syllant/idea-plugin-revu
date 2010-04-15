package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.util.PasswordPromptDialog;
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
    PasswordPromptDialog dialog = new PasswordPromptDialog(RevuBundle.message("dialog.updatePassword.password.label"),
      RevuBundle.message("dialog.updatePassword.title"), "");
    dialog.show();
    if (dialog.isOK())
    {
      String password = dialog.getPassword().trim();
      if (password == null)
      {
        password = null;
      }
      passwordReceiver.setPassword(password);
    }
  }

  public static interface IPasswordReceiver
  {
    void setPassword(@Nullable String password);
  }
}
