package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class DataReferential implements Serializable
{
  private File linkedFile;
  private Map<String, ItemCategory> itemCategoriesByName;
  private Map<String, ItemResolutionType> itemResolutionTypesByName;
  private Map<String, ItemPriority> itemPrioritiesByName;
  private Map<User.Role, List<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public DataReferential()
  {
    itemCategoriesByName = new HashMap<String, ItemCategory>();
    itemResolutionTypesByName = new HashMap<String, ItemResolutionType>();
    itemPrioritiesByName = new HashMap<String, ItemPriority>();
    usersByLogin = new HashMap<String, User>();
    usersByRole = new HashMap<User.Role, List<User>>();
  }

  public File getLinkedFile()
  {
    return linkedFile;
  }

  public void setLinkedFile(File linkedFile)
  {
    this.linkedFile = linkedFile;
  }

  @NotNull
  public Map<String, ItemPriority> getItemPrioritiesByName()
  {
    return Collections.unmodifiableMap(itemPrioritiesByName);
  }

  public void setItemPriorities(@NotNull Collection<ItemPriority> priorities)
  {
    this.itemPrioritiesByName.clear();
    for (ItemPriority priority : priorities)
    {
      this.itemPrioritiesByName.put(priority.getName(), priority);
    }
  }

  @Nullable
  public ItemPriority getItemPriority(@NotNull String priorityName)
  {
    return itemPrioritiesByName.get(priorityName);
  }

  @NotNull
  public Map<String, ItemCategory> getItemCategoriesByName()
  {
    return Collections.unmodifiableMap(itemCategoriesByName);
  }

  public void setItemCategories(@NotNull Collection<ItemCategory> categories)
  {
    this.itemCategoriesByName.clear();
    for (ItemCategory category : categories)
    {
      this.itemCategoriesByName.put(category.getName(), category);
    }
  }

  @Nullable
  public ItemCategory getItemCategory(@NotNull String categoryName)
  {
    return itemCategoriesByName.get(categoryName);
  }

  @NotNull
  public Map<String, ItemResolutionType> getItemResolutionTypesByName()
  {
    return Collections.unmodifiableMap(itemResolutionTypesByName);
  }

  public void setItemResolutionTypes(@NotNull Collection<ItemResolutionType> categories)
  {
    this.itemResolutionTypesByName.clear();
    for (ItemResolutionType category : categories)
    {
      this.itemResolutionTypesByName.put(category.getName(), category);
    }
  }

  @Nullable
  public ItemResolutionType getItemResolutionType(@NotNull String resolutionTypeName)
  {
    return itemResolutionTypesByName.get(resolutionTypeName);
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

  public void setUsers(@NotNull Collection<User> users)
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

    DataReferential that = (DataReferential) o;

    if (itemCategoriesByName != null ? !itemCategoriesByName.equals(that.itemCategoriesByName) :
      that.itemCategoriesByName != null)
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
    int result = itemCategoriesByName != null ? itemCategoriesByName.hashCode() : 0;
    result = 31 * result + (itemPrioritiesByName != null ? itemPrioritiesByName.hashCode() : 0);
    result = 31 * result + (usersByRole != null ? usersByRole.hashCode() : 0);
    result = 31 * result + (usersByLogin != null ? usersByLogin.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("itemPrioritiesByName", itemPrioritiesByName).
      append("usersByRole", usersByRole).
      append("usersByLogin", usersByLogin).
      toString();
  }
}
