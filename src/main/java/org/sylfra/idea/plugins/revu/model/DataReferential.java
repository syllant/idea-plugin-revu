package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataReferential extends AbstractRevuEntity<DataReferential>
{
  private transient Review review;
  private Map<String, IssueTag> itemTagsByName;
  private Map<String, IssuePriority> itemPrioritiesByName;
  private Map<User.Role, List<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public DataReferential(@NotNull Review review)
  {
    this.review = review;
    itemTagsByName = new HashMap<String, IssueTag>();
    itemPrioritiesByName = new HashMap<String, IssuePriority>();
    usersByLogin = new HashMap<String, User>();
    usersByRole = new HashMap<User.Role, List<User>>();
  }

  public Review getReview()
  {
    return review;
  }

  public void setReview(Review review)
  {
    this.review = review;
  }

  @NotNull
  public Map<String, IssuePriority> getItemPrioritiesByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(itemPrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getItemPrioritiesByName(true)), useLink);
  }

  @NotNull
  public List<IssuePriority> getItemPriorities(boolean useLink)
  {
    return getListUsingLinkedReferential(itemPrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getItemPriorities(true)), useLink);
  }

  public void setItemPriorities(@NotNull Collection<IssuePriority> priorities)
  {
    itemPrioritiesByName = new HashMap<String, IssuePriority>(priorities.size());
    for (IssuePriority priority : priorities)
    {
      itemPrioritiesByName.put(priority.getName(), priority);
    }
  }

  @Nullable
  public IssuePriority getItemPriority(@NotNull String priorityName)
  {
    return getItemPrioritiesByName(true).get(priorityName);
  }

  @NotNull
  public Map<String, IssueTag> getItemTagsByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(itemTagsByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getItemTagsByName(true)), useLink);
  }

  @NotNull
  public List<IssueTag> getItemTags(boolean useLink)
  {
    return getListUsingLinkedReferential(itemTagsByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getItemTags(true)), useLink);
  }

  public void setItemTags(@NotNull Collection<IssueTag> tags)
  {
    itemTagsByName = new HashMap<String, IssueTag>(tags.size());
    for (IssueTag tag : tags)
    {
      itemTagsByName.put(tag.getName(), tag);
    }
  }

  @Nullable
  public IssueTag getItemTag(@NotNull String tagName)
  {
    return getItemTagsByName(true).get(tagName);
  }

  @NotNull
  public Map<String, User> getUsersByLogin(boolean useLink)
  {
    return getMapUsingLinkedReferential(usersByLogin, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getUsersByLogin(true)), useLink);
  }

  @NotNull
  public Map<User.Role, List<User>> getUsersByRole(boolean useLink)
  {
    return getMapUsingLinkedReferential(usersByRole, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getUsersByRole(true)), useLink);
  }

  @NotNull
  public List<User> getUsers(boolean useLink)
  {
    return getListUsingLinkedReferential(usersByLogin, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getUsers(true)), useLink);
  }

  public void setUsers(@NotNull Collection<User> users)
  {
    usersByLogin = new HashMap<String, User>(users.size());
    usersByRole = new HashMap<User.Role, List<User>>(users.size());
    for (User user : users)
    {
      this.usersByLogin.put(user.getLogin(), user);
      for (User.Role role : user.getRoles())
      {
        List<User> usersForRole = usersByRole.get(role);
        if (usersForRole == null)
        {
          usersForRole = new ArrayList<User>();
          usersByRole.put(role, usersForRole);
        }

        usersForRole.add(user);
      }
    }
  }

  public void addUser(@NotNull User user)
  {
    usersByLogin.put(user.getLogin(), user);

    for (User.Role role : user.getRoles())
    {
      List<User> usersForRole = usersByRole.get(role);
      if (usersForRole == null)
      {
        usersForRole = new ArrayList<User>();
        usersByRole.put(role, usersForRole);
      }

      usersForRole.add(user);
    }
  }

  @Nullable
  public User getUser(@NotNull String login, boolean useLink)
  {
    return getUsersByLogin(useLink).get(login);
  }

  public void copyFrom(@NotNull DataReferential dataReferential)
  {
    itemTagsByName.putAll(dataReferential.getItemTagsByName(false));
    itemPrioritiesByName.putAll(dataReferential.getItemPrioritiesByName(false));
    usersByRole.putAll(dataReferential.getUsersByRole(false));
    usersByLogin.putAll(dataReferential.getUsersByLogin(false));
  }

  @Override
  public DataReferential clone()
  {
    DataReferential clone = super.clone();

    clone.setItemTags(cloneList(clone.getItemTags(false)));
    clone.setItemPriorities(cloneList(clone.getItemPriorities(false)));
    clone.setUsers(cloneList(clone.getUsers(false)));

    return clone;
  }

  private <K, V> Map<K, V> getMapUsingLinkedReferential(Map<K, V> thisValues, Map<K, V> linkValues, boolean useLink)
  {
    Map<K, V> result;
    if ((review.getExtendedReview() == null) || (!useLink))
    {
      result = thisValues;
    }
    else
    {
      result = new HashMap<K, V>(linkValues);
      result.putAll(thisValues);
    }

    return Collections.unmodifiableMap(result);
  }

  private <K, V> List<V> getListUsingLinkedReferential(Map<K, V> thisValues, List<V> linkValues, boolean useLink)
  {
    List<V> result;
    if ((review.getExtendedReview() == null) || (!useLink))
    {
      result = new ArrayList<V>(thisValues.values());
    }
    else
    {
      result = new ArrayList<V>(linkValues);
      result.addAll(thisValues.values());
    }

    return Collections.unmodifiableList(result);
  }

  public int compareTo(DataReferential o)
  {
    return 0;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    DataReferential that = (DataReferential) o;

    if (itemTagsByName != null ? !itemTagsByName.equals(that.itemTagsByName) :
      that.itemTagsByName != null)
    {
      return false;
    }
    if (itemPrioritiesByName != null ? !itemPrioritiesByName.equals(that.itemPrioritiesByName) :
      that.itemPrioritiesByName != null)
    {
      return false;
    }
    if (usersByLogin != null ? !usersByLogin.equals(that.usersByLogin) : that.usersByLogin != null)
    {
      return false;
    }
    if (usersByRole != null ? !usersByRole.equals(that.usersByRole) : that.usersByRole != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = itemTagsByName != null ? itemTagsByName.hashCode() : 0;
    result = 31 * result + (itemPrioritiesByName != null ? itemPrioritiesByName.hashCode() : 0);
    result = 31 * result + (usersByRole != null ? usersByRole.hashCode() : 0);
    result = 31 * result + (usersByLogin != null ? usersByLogin.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("itemTagsByName", itemTagsByName).
      append("itemPrioritiesByName", itemPrioritiesByName).
      append("usersByRole", usersByRole).
      append("usersByLogin", usersByLogin).
      toString();
  }
}
