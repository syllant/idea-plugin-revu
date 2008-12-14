package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.util.Collections;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueRecipientsForm extends AbstractIssueForm
{
  private JPanel contentPane;
  private ElementsChooser<User> elementsChooser;

  public IssueRecipientsForm(@NotNull Project project)
  {
    super(project);
  }

  private void createUIComponents()
  {
    elementsChooser = new ElementsChooser<User>(true)
    {
      @Override
      protected String getItemText(User user)
      {
        return user.getDisplayName();
      }
    };
    elementsChooser.setColorUnmarkedElements(false);
  }

  public JComponent getPreferredFocusedComponent()
  {
    return elementsChooser.getComponent();
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public boolean isModified(@NotNull Issue data)
  {
    return !elementsChooser.getMarkedElements().equals(data.getRecipients());
  }

  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    RevuUtils.setWriteAccess((user != null) && (user.hasRole(User.Role.REVIEWER)
      && ((currentIssue == null) || (IssueStatus.CLOSED != currentIssue.getStatus()))), elementsChooser);
  }

  protected void internalValidateInput()
  {
    // Nothing to validate
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    super.internalUpdateUI(data, requestFocus);

    elementsChooser.setElements(((data == null) || (data.getReview() == null))
      ? Collections.<User>emptyList()
      : data.getReview().getDataReferential().getUsers(true), false);
    elementsChooser.markElements((data == null) ? Collections.<User>emptyList() : data.getRecipients());
  }

  protected void internalUpdateData(@NotNull Issue data)
  {
    data.setRecipients(elementsChooser.getMarkedElements());
  }
}
