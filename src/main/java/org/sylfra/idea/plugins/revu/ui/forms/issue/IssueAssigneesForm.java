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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueAssigneesForm extends AbstractIssueForm
{
  private JPanel contentPane;
  private ElementsChooser<User> elementsChooser;
  private List<UpdatableEntityListListener<User>> updatableEntityListListeners;

  public IssueAssigneesForm(@NotNull Project project)
  {
    super(project);

    updatableEntityListListeners = new ArrayList<UpdatableEntityListListener<User>>();
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
    elementsChooser.addElementsMarkListener(new ElementsChooser.ElementsMarkListener<User>()
    {
      public void elementMarkChanged(User element, boolean isMarked)
      {
        for (UpdatableEntityListListener<User> updatableEntityListListener : updatableEntityListListeners)
        {
          if (isMarked)
          {
            updatableEntityListListener.entityAdded(elementsChooser.getMarkedElements(), element);
          }
          else
          {
            updatableEntityListListener.entityDeleted(elementsChooser.getMarkedElements(), element);
          }
        }
      }
    });
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
    return !elementsChooser.getMarkedElements().equals(data.getAssignees());
  }

  protected void internalUpdateWriteAccess(Issue data, @Nullable User user)
  {
    RevuUtils.setWriteAccess((user != null) && (user.hasRole(User.Role.REVIEWER)
      && ((currentIssue == null) || (IssueStatus.CLOSED != currentIssue.getStatus()))), elementsChooser);
  }

  protected void internalValidateInput(@Nullable Issue data)
  {
    // Nothing to validate
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    super.internalUpdateUI(data, requestFocus);

    elementsChooser.setElements(((data == null) || (data.getReview() == null))
      ? Collections.<User>emptyList()
      : data.getReview().getDataReferential().getUsers(true), false);
    elementsChooser.markElements((data == null) ? Collections.<User>emptyList() : data.getAssignees());
  }

  protected void internalUpdateData(@NotNull Issue data)
  {
    data.setAssignees(elementsChooser.getMarkedElements());
  }

  public void addUpdatableEntityListListener(UpdatableEntityListListener<User> listener)
  {
    updatableEntityListListeners.add(listener);
  }
}
