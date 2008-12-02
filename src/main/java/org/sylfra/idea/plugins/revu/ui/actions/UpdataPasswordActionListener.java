package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.util.PasswordPromptDialog;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class UpdataPasswordActionListener implements ActionListener
{
  private IPasswordReceiver passwordReceiver;

  public UpdataPasswordActionListener(IPasswordReceiver passwordReceiver)
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
      passwordReceiver.setPassword(RevuUtils.z(dialog.getPassword()));
    }
  }

  public static interface IPasswordReceiver
  {
    void setPassword(@Nullable String password);
  }
}
