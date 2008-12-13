package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.priority.ItemPriorityReferentialForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.resolutiontype.ItemResolutionTypeReferentialForm;
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
  private ItemResolutionTypeReferentialForm itemResolutionTypeReferentialForm;
  private JLabel lbNoUserForEmbeddedReviews;

  public ReferentialTabbedPane(final Project project)
  {
    this.project = project;

    userReferentialForm = new UserReferentialForm(project);
    itemTagReferentialForm = new ItemTagReferentialForm(project);
    itemPriorityReferentialForm = new ItemPriorityReferentialForm(project);
    itemResolutionTypeReferentialForm = new ItemResolutionTypeReferentialForm(project);

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
    tabbedPane.add(RevuBundle.message("settings.project.review.referential.itemResolutionType.title"),
      itemResolutionTypeReferentialForm.getContentPane());
  }


  public boolean isModified(@NotNull DataReferential data)
  {
    return ((userReferentialForm.isModified(new ReferentialListHolder<User>(data.getUsers(true), null)))
      || (itemTagReferentialForm.isModified(new ReferentialListHolder<ItemTag>(data.getItemTags(true), null)))
      || (itemPriorityReferentialForm.isModified(
      new ReferentialListHolder<ItemPriority>(data.getItemPriorities(true), null)))
      || (itemResolutionTypeReferentialForm.isModified(
      new ReferentialListHolder<ItemResolutionType>(data.getItemResolutionTypes(true), null))));
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
    updateError(itemResolutionTypeReferentialForm.getContentPane(), !itemResolutionTypeReferentialForm.validateInput(),
      null);
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

    ReferentialListHolder<ItemTag> tagHolder = buildItemTagsListHolder(data);
    itemTagReferentialForm.updateUI(review, tagHolder, requestFocus);

    ReferentialListHolder<ItemPriority> priorityHolder = buildItemPrioritiesListHolder(data);
    itemPriorityReferentialForm.updateUI(review, priorityHolder, requestFocus);

    ReferentialListHolder<ItemResolutionType> resolutionTypeHolder = buildItemResolutionTypesListHolder(data);
    itemResolutionTypeReferentialForm.updateUI(review, resolutionTypeHolder, requestFocus);
  }

  protected void internalUpdateData(@NotNull DataReferential data)
  {
    ReferentialListHolder<User> userHolder = new ReferentialListHolder<User>(data.getUsers(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getUsers(true));
    userReferentialForm.updateData(userHolder);
    data.setUsers(userHolder.getItems());

    ReferentialListHolder<ItemTag> tagHolder
      = new ReferentialListHolder<ItemTag>(data.getItemTags(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getItemTags(true));
    itemTagReferentialForm.updateData(tagHolder);
    data.setItemTags(tagHolder.getItems());

    ReferentialListHolder<ItemPriority> priorityHolder
      = new ReferentialListHolder<ItemPriority>(data.getItemPriorities(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getItemPriorities(true));
    itemPriorityReferentialForm.updateData(priorityHolder);
    data.setItemPriorities(priorityHolder.getItems());

    ReferentialListHolder<ItemResolutionType> resolutionTypeHolder
      = new ReferentialListHolder<ItemResolutionType>(data.getItemResolutionTypes(false),
      (data.getReview().getExtendedReview() == null)
        ? null : data.getReview().getExtendedReview().getDataReferential().getItemResolutionTypes(true));
    itemResolutionTypeReferentialForm.updateData(resolutionTypeHolder);
    data.setItemResolutionTypes(resolutionTypeHolder.getItems());
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

  private ReferentialListHolder<ItemTag> buildItemTagsListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<ItemTag>(new ArrayList<ItemTag>(), null);
    }

    List<ItemTag> thisTags = data.getItemTags(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<ItemTag>(thisTags, null);
    }

    List<ItemTag> extendedTags = new ArrayList<ItemTag>(
      data.getReview().getExtendedReview().getDataReferential().getItemTags(true));
    extendedTags.removeAll(thisTags);

    return new ReferentialListHolder<ItemTag>(thisTags, extendedTags);
  }

  private ReferentialListHolder<ItemPriority> buildItemPrioritiesListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<ItemPriority>(new ArrayList<ItemPriority>(), null);
    }

    List<ItemPriority> thisPriorities = data.getItemPriorities(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<ItemPriority>(thisPriorities,
        null);
    }

    List<ItemPriority> extendedPriorities = new ArrayList<ItemPriority>(
      data.getReview().getExtendedReview().getDataReferential().getItemPriorities(true));
    extendedPriorities.removeAll(thisPriorities);

    return new ReferentialListHolder<ItemPriority>(thisPriorities, extendedPriorities);
  }

  private ReferentialListHolder<ItemResolutionType> buildItemResolutionTypesListHolder(DataReferential data)
  {
    if (data == null)
    {
      return new ReferentialListHolder<ItemResolutionType>(new ArrayList<ItemResolutionType>(), null);
    }

    List<ItemResolutionType> thisResolutionTypes = data.getItemResolutionTypes(false);
    if (data.getReview().getExtendedReview() == null)
    {
      return new ReferentialListHolder<ItemResolutionType>(thisResolutionTypes, null);
    }

    List<ItemResolutionType> extendedResolutionTypes = new ArrayList<ItemResolutionType>(
      data.getReview().getExtendedReview().getDataReferential().getItemResolutionTypes(true));
    extendedResolutionTypes.removeAll(thisResolutionTypes);

    return new ReferentialListHolder<ItemResolutionType>(thisResolutionTypes, extendedResolutionTypes);
  }

  @Override
  public void dispose()
  {
    userReferentialForm.dispose();
    itemTagReferentialForm.dispose();
    itemPriorityReferentialForm.dispose();
    itemResolutionTypeReferentialForm.dispose();
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
