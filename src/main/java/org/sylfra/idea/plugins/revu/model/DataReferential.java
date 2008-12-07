package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DataReferential extends AbstractRevuEntity<DataReferential>
{
  private Review review;
  private Map<String, ItemCategory> itemCategoriesByName;
  private Map<String, ItemResolutionType> itemResolutionTypesByName;
  private Map<String, ItemPriority> itemPrioritiesByName;
  private Map<User.Role, List<User>> usersByRole;
  private Map<String, User> usersByLogin;

  public DataReferential(@NotNull Review review)
  {
    this.review = review;
    itemCategoriesByName = new HashMap<String, ItemCategory>();
    itemResolutionTypesByName = new HashMap<String, ItemResolutionType>();
    itemPrioritiesByName = new HashMap<String, ItemPriority>();
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
  public Map<String, ItemPriority> getItemPrioritiesByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(itemPrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getItemPrioritiesByName(true)), useLink);
  }

  @NotNull
  public List<ItemPriority> getItemPriorities(boolean useLink)
  {
    return getListUsingLinkedReferential(itemPrioritiesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getItemPriorities(true)), useLink);
  }

  public void setItemPriorities(@NotNull Collection<ItemPriority> priorities)
  {
    itemPrioritiesByName = new HashMap<String, ItemPriority>(priorities.size());
    for (ItemPriority priority : priorities)
    {
      itemPrioritiesByName.put(priority.getName(), priority);
    }
  }

  @Nullable
  public ItemPriority getItemPriority(@NotNull String priorityName)
  {
    return getItemPrioritiesByName(true).get(priorityName);
  }

  @NotNull
  public Map<String, ItemCategory> getItemCategoriesByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(itemCategoriesByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getItemCategoriesByName(true)), useLink);
  }

  @NotNull
  public List<ItemCategory> getItemCategories(boolean useLink)
  {
    return getListUsingLinkedReferential(itemCategoriesByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getItemCategories(true)), useLink);
  }

  public void setItemCategories(@NotNull Collection<ItemCategory> categories)
  {
    itemCategoriesByName = new HashMap<String, ItemCategory>(categories.size());
    for (ItemCategory category : categories)
    {
      itemCategoriesByName.put(category.getName(), category);
    }
  }

  @Nullable
  public ItemCategory getItemCategory(@NotNull String categoryName)
  {
    return getItemCategoriesByName(true).get(categoryName);
  }

  @NotNull
  public Map<String, ItemResolutionType> getItemResolutionTypesByName(boolean useLink)
  {
    return getMapUsingLinkedReferential(itemResolutionTypesByName, ((review.getExtendedReview() == null) ? null :
        review.getExtendedReview().getDataReferential().getItemResolutionTypesByName(true)), useLink);
  }

  @NotNull
  public List<ItemResolutionType> getItemResolutionTypes(boolean useLink)
  {
    return getListUsingLinkedReferential(itemResolutionTypesByName, ((review.getExtendedReview() == null)
      ? null : review.getExtendedReview().getDataReferential().getItemResolutionTypes(true)), useLink);
  }

  public void setItemResolutionTypes(@NotNull Collection<ItemResolutionType> itemResolutionTypes)
  {
    itemResolutionTypesByName = new HashMap<String, ItemResolutionType>(itemResolutionTypes.size());
    for (ItemResolutionType category : itemResolutionTypes)
    {
      this.itemResolutionTypesByName.put(category.getName(), category);
    }
  }

  @Nullable
  public ItemResolutionType getItemResolutionType(@NotNull String resolutionTypeName)
  {
    return getItemResolutionTypesByName(true).get(resolutionTypeName);
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
    itemCategoriesByName.putAll(dataReferential.getItemCategoriesByName(false));
    itemResolutionTypesByName.putAll(dataReferential.getItemResolutionTypesByName(false));
    itemPrioritiesByName.putAll(dataReferential.getItemPrioritiesByName(false));
    usersByRole.putAll(dataReferential.getUsersByRole(false));
    usersByLogin.putAll(dataReferential.getUsersByLogin(false));
  }

  @Override
  public DataReferential clone()
  {
    DataReferential clone = super.clone();

    clone.setItemCategories(cloneList(clone.getItemCategories(false)));
    clone.setItemPriorities(cloneList(clone.getItemPriorities(false)));
    clone.setItemResolutionTypes(cloneList(clone.getItemResolutionTypes(false)));
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
