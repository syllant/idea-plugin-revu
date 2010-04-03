package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssuePriority extends AbstractRevuEntity<IssuePriority> implements Comparable<IssuePriority>,
  IRevuUniqueNameHolderEntity<IssuePriority>
{
  private byte order;
  private String name;

  public IssuePriority()
  {
  }

  public IssuePriority(byte order, String name)
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
  public int compareTo(IssuePriority o)
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

    IssuePriority priority = (IssuePriority) o;

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
