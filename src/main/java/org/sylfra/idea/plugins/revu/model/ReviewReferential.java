package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class ReviewReferential implements Serializable
{
  private Map<String, ReviewCategory> categoriesByName;
  private Map<String, ReviewPriority> prioritiesByName;
  private Map<User.Role, List<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public ReviewReferential()
  {
    categoriesByName = new HashMap<String, ReviewCategory>();
    prioritiesByName = new HashMap<String, ReviewPriority>();
    usersByLogin = new HashMap<String, User>();
    usersByRole = new HashMap<User.Role, List<User>>();
  }

  @NotNull
  public Map<String, ReviewPriority> getPrioritiesByName()
  {
    return Collections.unmodifiableMap(prioritiesByName);
  }

  public void setPriorities(@NotNull Set<ReviewPriority> priorities)
  {
    this.prioritiesByName.clear();
    for (ReviewPriority priority : priorities)
    {
      this.prioritiesByName.put(priority.getName(), priority);
    }
  }

  @Nullable
  public ReviewPriority getPriority(@NotNull String priorityName)
  {
    return prioritiesByName.get(priorityName);
  }

  @NotNull
  public Map<String, ReviewCategory> getCategoriesByName()
  {
    return Collections.unmodifiableMap(categoriesByName);
  }

  public void setCategories(@NotNull Set<ReviewCategory> categories)
  {
    this.categoriesByName.clear();
    for (ReviewCategory category : categories)
    {
      this.categoriesByName.put(category.getName(), category);
    }
  }

  @Nullable
  public ReviewCategory getCategory(@NotNull String categoryName)
  {
    return categoriesByName.get(categoryName);
  }

  @NotNull
  public Map<String, User> getUsersByLogin()
  {
    return Collections.unmodifiableMap(usersByLogin);
  }

  @NotNull
  public Map<User.Role, List<User>> getUsersByRole()
  {
    return Collections.unmodifiableMap(usersByRole);
  }

  public void setUsers(@NotNull Set<User> users)
  {
    this.usersByLogin.clear();
    this.usersByRole.clear();
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

  @Nullable
  public User getUser(@NotNull String login)
  {
    return usersByLogin.get(login);
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

    if (categoriesByName != null ? !categoriesByName.equals(that.categoriesByName) :
      that.categoriesByName != null)
    {
      return false;
    }
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
    int result = categoriesByName != null ? categoriesByName.hashCode() : 0;
    result = 31 * result + (prioritiesByName != null ? prioritiesByName.hashCode() : 0);
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
