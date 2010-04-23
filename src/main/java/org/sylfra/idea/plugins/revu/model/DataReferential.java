package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.*;

public class DataReferential extends AbstractRevuEntity<DataReferential>
{
  private transient Review review;
  private Map<String, IssueTag> issueTagsByName;
  private Map<String, IssuePriority> issuePrioritiesByName;
  private Map<User.Role, Set<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public DataReferential(@NotNull Review review)
  {
    this.review = review;
    issueTagsByName = new HashMap<String, IssueTag>();
    issuePrioritiesByName = new HashMap<String, IssuePriority>();
    usersByLogin = new HashMap<String, User>();
    usersByRole = new HashMap<User.Role, Set<User>>();
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
  public Map<String, IssuePriority> getIssuePrioritiesByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(issuePrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getIssuePrioritiesByName(true)), useLink);
  }

  @NotNull
  public List<IssuePriority> getIssuePriorities(boolean useLink)
  {
    return getListUsingLinkedReferential(issuePrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getIssuePriorities(true)), useLink);
  }

  public void setIssuePriorities(@NotNull Collection<IssuePriority> priorities)
  {
    issuePrioritiesByName = new HashMap<String, IssuePriority>(priorities.size());
    for (IssuePriority priority : priorities)
    {
      issuePrioritiesByName.put(priority.getName(), priority);
    }
  }

  @Nullable
  public IssuePriority getIssuePriority(@NotNull String priorityName)
  {
    return getIssuePrioritiesByName(true).get(priorityName);
  }

  @NotNull
  public Map<String, IssueTag> getIssueTagsByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(issueTagsByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getIssueTagsByName(true)), useLink);
  }

  @NotNull
  public List<IssueTag> getIssueTags(boolean useLink)
  {
    return getListUsingLinkedReferential(issueTagsByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getIssueTags(true)), useLink);
  }

  public void setIssueTags(@NotNull Collection<IssueTag> tags)
  {
    issueTagsByName = new HashMap<String, IssueTag>(tags.size());
    for (IssueTag tag : tags)
    {
      issueTagsByName.put(tag.getName(), tag);
    }
  }

  @Nullable
  public IssueTag getIssueTag(@NotNull String tagName)
  {
    return getIssueTagsByName(true).get(tagName);
  }

  @NotNull
  public Map<String, User> getUsersByLogin(boolean useLink)
  {
    return getMapUsingLinkedReferential(usersByLogin, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getUsersByLogin(true)), useLink);
  }

  @NotNull
  public Map<User.Role, Set<User>> getUsersByRole(boolean useLink)
  {
    return getMapUsingLinkedReferential(usersByRole, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getUsersByRole(true)), useLink);
  }

  @NotNull
  public List<User> getUsers(boolean useLink)
  {
    Map<String, User> result;
    if ((review.getExtendedReview() == null) || (!useLink))
    {
      result = usersByLogin;
    }
    else
    {
      Map<String, User> linkedUsers = review.getExtendedReview().getDataReferential().getUsersByLogin(true);
      result = new HashMap<String, User>(linkedUsers);
      result.putAll(usersByLogin);

      // Overwrite display name of current user: if display name was defined in extended review, let's use it
      String currentLogin = RevuUtils.getCurrentUserLogin();
      User user = usersByLogin.get(currentLogin);
      User linkedUser = linkedUsers.get(currentLogin);

      if ((user != null) && (linkedUser != null) && (user.getDisplayName().equals(currentLogin)))
      {
        user.setDisplayName(linkedUser.getDisplayName());
      }
    }

    return Collections.unmodifiableList(new ArrayList<User>(result.values()));
  }

  public void setUsers(@NotNull Collection<User> users)
  {
    usersByLogin = new HashMap<String, User>(users.size());
    usersByRole = new HashMap<User.Role, Set<User>>(users.size());
    for (User user : users)
    {
      addUser(user);
    }
  }

  public void addUser(@NotNull User user)
  {
    usersByLogin.put(user.getLogin(), user);

    // Add roles using role power, e.g. a user with reviewer role has also author role 
    for (User.Role role : User.Role.values())
    {
      if (user.hasRole(role))
      {
        Set<User> usersForRole = usersByRole.get(role);
        if (usersForRole == null)
        {
          usersForRole = new HashSet<User>();
          usersByRole.put(role, usersForRole);
        }
  
        usersForRole.add(user);
      }
    }
  }

  @Nullable
  public User getUser(@NotNull String login, boolean useLink)
  {
    return getUsersByLogin(useLink).get(login);
  }

  public void copyFrom(@NotNull DataReferential dataReferential)
  {
    issueTagsByName.putAll(dataReferential.getIssueTagsByName(false));
    issuePrioritiesByName.putAll(dataReferential.getIssuePrioritiesByName(false));
    usersByRole.putAll(dataReferential.getUsersByRole(false));
    usersByLogin.putAll(dataReferential.getUsersByLogin(false));
  }

  @Override
  public DataReferential clone()
  {
    DataReferential clone = super.clone();

    clone.setIssueTags(cloneList(clone.getIssueTags(false)));
    clone.setIssuePriorities(cloneList(clone.getIssuePriorities(false)));
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

    if (issueTagsByName != null ? !issueTagsByName.equals(that.issueTagsByName) :
      that.issueTagsByName != null)
    {
      return false;
    }
    if (issuePrioritiesByName != null ? !issuePrioritiesByName.equals(that.issuePrioritiesByName) :
      that.issuePrioritiesByName != null)
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
    int result = issueTagsByName != null ? issueTagsByName.hashCode() : 0;
    result = 31 * result + (issuePrioritiesByName != null ? issuePrioritiesByName.hashCode() : 0);
    result = 31 * result + (usersByRole != null ? usersByRole.hashCode() : 0);
    result = 31 * result + (usersByLogin != null ? usersByLogin.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("issueTagsByName", issueTagsByName).
      append("issuePrioritiesByName", issuePrioritiesByName).
      append("usersByRole", usersByRole).
      append("usersByLogin", usersByLogin).
      toString();
  }
}
