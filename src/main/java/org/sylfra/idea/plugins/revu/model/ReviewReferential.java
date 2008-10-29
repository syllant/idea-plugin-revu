package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class ReviewReferential implements Serializable
{
  private List<ReviewPriority> priorities;
  private Map<String, ReviewPriority> prioritiesByName;
  private Set<User> users;
  private Map<User.Role, List<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public ReviewReferential()
  {
    priorities = new ArrayList<ReviewPriority>();
    prioritiesByName = new HashMap<String, ReviewPriority>();
    users = new HashSet<User>();
    usersByRole = new HashMap<User.Role, List<User>>();
    usersByLogin = new HashMap<String, User>();
  }

  @NotNull
  public Map<String, ReviewPriority> getPrioritiesByName()
  {
    return Collections.unmodifiableMap(prioritiesByName);
  }

  public void setPriorities(@NotNull List<ReviewPriority> priorities)
  {
    this.priorities = priorities;
  }

  @NotNull
  public Map<User.Role, List<User>> getUsersByRole()
  {
    return Collections.unmodifiableMap(usersByRole);
  }

  public void setUsers(@NotNull Set<User> users)
  {
    this.users = users;
  }

  @NotNull
  public Map<String, User> getUsersByLogin()
  {
    return Collections.unmodifiableMap(usersByLogin);
  }

  @Nullable
  public User getUser(@NotNull String login)
  {
    return usersByLogin.get(login);
  }

  @Nullable
  public ReviewPriority getPriority(@NotNull String priorityName)
  {
    return prioritiesByName.get(priorityName);
  }

  public void consolidate()
  {
    // Priorities
    prioritiesByName.clear();
    for (ReviewPriority priority : priorities)
    {
      prioritiesByName.put(priority.getName(), priority);
    }

    // Users
    usersByLogin.clear();
    usersByRole.clear();
    for (User user : users)
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

    ReviewReferential that = (ReviewReferential) o;

    if (prioritiesByName != null ? !prioritiesByName.equals(that.prioritiesByName) :
      that.prioritiesByName != null)
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
    int result = prioritiesByName != null ? prioritiesByName.hashCode() : 0;
    result = 31 * result + (usersByRole != null ? usersByRole.hashCode() : 0);
    result = 31 * result + (usersByLogin != null ? usersByLogin.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("prioritiesByName", prioritiesByName).
      append("usersByRole", usersByRole).
      append("usersByLogin", usersByLogin).
      toString();
  }
}
