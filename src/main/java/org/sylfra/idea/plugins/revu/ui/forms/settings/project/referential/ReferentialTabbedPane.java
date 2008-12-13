package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority.ItemPriorityReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.tag.ItemTagReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.user.UserReferentialForm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReferentialTabbedPane extends AbstractUpdatableForm<DataReferential>
{
  private final Project project;
  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private UserReferentialForm userReferentialForm;
  private ItemTagReferentialForm itemTagReferentialForm;
  private ItemPriorityReferentialForm itemPriorityReferentialForm;
  private JLabel lbNoUserForEmbeddedReviews;

  public ReferentialTabbedPane(final Project project)
  {
    this.project = project;

    userReferentialForm = new UserReferentialForm(project);
    itemTagReferentialForm = new ItemTagReferentialForm(project);
    itemPriorityReferentialForm = new ItemPriorityReferentialForm(project);

    lbNoUserForEmbeddedReviews = new JLabel(
      RevuBundle.message("settings.project.review.referential.noUserForEmbeddedReviews.text"));
    lbNoUserForEmbeddedReviews.setHorizontalAlignment(SwingConstants.CENTER);
    CardLayout cardLayout = new CardLayout();
    JPanel pnUsers = new JPanel(cardLayout);
    pnUsers.add("table", userReferentialForm.getContentPane());
    pnUsers.add("label", lbNoUserForEmbeddedReviews);

    tabbedPane.add(RevuBundle.message("settings.project.review.referential.user.title"), pnUsers);
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemTag.title"),
      itemTagReferentialForm.getContentPane());
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemPriority.title"),
      itemPriorityReferentialForm.getContentPane());
  }


  public boolean isModified(@NotNull DataReferential data)
  {
    return ((userReferentialForm.isModified(new ReferentialListHolder<User>(data.getUsers(true), null)))
      || (itemTagReferentialForm.isModified(new ReferentialListHolder<IssueTag>(data.getItemTags(true), null)))
      || (itemPriorityReferentialForm.isModified(
            new ReferentialListHolder<IssuePriority>(data.getItemPriorities(true), null))));
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
  }

  protected void internalValidateInput()
  {
    updateError(userReferentialForm.getContentPane(), !userReferentialForm.validateInput(), null);
    updateError(itemTagReferentialForm.getContentPane(), !itemTagReferentialForm.validateInput(), null);
    updateError(itemPriorityReferentialForm.getContentPane(), !itemPriorityReferentialForm.validateInput(), null);
  }

  protected void internalUpdateUI(DataReferential data, boolean requestFocus)
  {
    Review review = getEnclosingReview();

    JPanel pnUsers = (JPanel) tabbedPane.getComponentAt(0);
    CardLayout userLayout = (CardLayout) pnUsers.getLayout();
    if ((data != null) && (data.getReview().isEmbedded()))
    {
      userLayout.show(pnUsers, "label");
    }
    else
    {
      userLayout.show(pnUsers, "table");
      ReferentialListHolder<User> userHolder = buildUsersListHolder(data);
      userReferentialForm.updateUI(review, userHolder, requestFocus);
    }

    ReferentialListHolder<IssueTag> tagHolder = buildItemTagsListHolder(data);
    itemTagReferentialForm.updateUI(review, tagHolder, requestFocus);

    ReferentialListHolder<IssuePriority> priorityHolder = buildItemPrioritiesListHolder(data);
    itemPriorityReferentialForm.updateUI(review, priorityHolder, requestFocus);
  }

  protected void internalUpdateData(@NotNull DataReferential data)
  {
    ReferentialListHolder<User> userHolder = new ReferentialListHolder<User>(data.getUsers(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getUsers(true));
    userReferentialForm.updateData(userHolder);
    data.setUsers(userHolder.getItems());

    ReferentialListHolder<IssueTag> tagHolder
      = new ReferentialListHolder<IssueTag>(data.getItemTags(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getItemTags(true));
    itemTagReferentialForm.updateData(tagHolder);
    data.setItemTags(tagHolder.getItems());

    ReferentialListHolder<IssuePriority> priorityHolder
      = new ReferentialListHolder<IssuePriority>(data.getItemPriorities(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getItemPriorities(true));
    itemPriorityReferentialForm.updateData(priorityHolder);
    data.setItemPriorities(priorityHolder.getItems());
  }

  private ReferentialListHolder<User> buildUsersListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<User>(new ArrayList<User>(), null);
    }

    List<User> thisUsers = data.getUsers(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<User>(thisUsers, null);
    }

    List<User> extendedUsers = new ArrayList<User>(data.getReview().getExtendedReview().getDataReferential()
      .getUsers(true));
    for (ListIterator<User> it = extendedUsers.listIterator(); it.hasNext();)
    {
      User user = it.next();
      if (data.getUser(user.getLogin(), false) != null)
      {
        it.remove();
      }
    }
    return new ReferentialListHolder<User>(thisUsers, extendedUsers);
  }

  private ReferentialListHolder<IssueTag> buildItemTagsListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<IssueTag>(new ArrayList<IssueTag>(), null);
    }

    List<IssueTag> thisTags = data.getItemTags(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<IssueTag>(thisTags, null);
    }

    List<IssueTag> extendedTags = new ArrayList<IssueTag>(
      data.getReview().getExtendedReview().getDataReferential().getItemTags(true));
    extendedTags.removeAll(thisTags);

    return new ReferentialListHolder<IssueTag>(thisTags, extendedTags);
  }

  private ReferentialListHolder<IssuePriority> buildItemPrioritiesListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<IssuePriority>(new ArrayList<IssuePriority>(), null);
    }

    List<IssuePriority> thisPriorities = data.getItemPriorities(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<IssuePriority>(thisPriorities,
        null);
    }

    List<IssuePriority> extendedPriorities = new ArrayList<IssuePriority>(
      data.getReview().getExtendedReview().getDataReferential().getItemPriorities(true));
    extendedPriorities.removeAll(thisPriorities);

    return new ReferentialListHolder<IssuePriority>(thisPriorities, extendedPriorities);
  }

  @Override
  public void dispose()
  {
    userReferentialForm.dispose();
    itemTagReferentialForm.dispose();
    itemPriorityReferentialForm.dispose();
  }

  public JComponent getPreferredFocusedComponent()
  {
    // @TODO
    return contentPane;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

}
