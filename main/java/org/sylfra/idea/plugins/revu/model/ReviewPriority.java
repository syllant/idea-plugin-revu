package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewPriority implements Serializable, Comparable<ReviewPriority>
{
  private byte order;
  private String name;

  public ReviewPriority(byte order, String name)
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

  public int compareTo(ReviewPriority o)
  {
    return (order > o.order) ? -1 : ((order == o.order) ? 0 : -1);
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

    ReviewPriority priority = (ReviewPriority) o;

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
