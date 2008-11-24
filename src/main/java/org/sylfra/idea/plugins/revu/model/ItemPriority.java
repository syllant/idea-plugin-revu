package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemPriority implements Serializable, INamedHolder, Comparable<ItemPriority>
{
  private byte order;
  private String name;

  public ItemPriority()
  {
  }

  public ItemPriority(byte order, String name)
  {
    this.order = order;
    this.name = name;
  }

  public byte getOrder()
  {
    return order;
  }

  public void setOrder(byte order)
  {
    this.order = order;
  }

  @NotNull
  public String getName()
  {
    return name;
  }

  public void setName(@NotNull String name)
  {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(ItemPriority o)
  {
    return order - o.order;
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

    ItemPriority priority = (ItemPriority) o;

    if (order != priority.order)
    {
      return false;
    }
    if (name != null ? !name.equals(priority.name) : priority.name != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = (int) order;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("order", order).
      append("name", name).
      toString();
  }
}
