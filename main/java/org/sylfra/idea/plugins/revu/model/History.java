package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class History
{
  private User createdBy;
  private User lastUpdatedBy;
  private long createdOn;
  private long lastUpdatedOn;

  @NotNull
  public User getCreatedBy()
  {
    return createdBy;
  }

  public void setCreatedBy(@NotNull User createdBy)
  {
    this.createdBy = createdBy;
  }

  @NotNull
  public User getLastUpdatedBy()
  {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(@NotNull User lastUpdatedBy)
  {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public long getCreatedOn()
  {
    return createdOn;
  }

  public void setCreatedOn(long createdOn)
  {
    this.createdOn = createdOn;
  }

  public long getLastUpdatedOn()
  {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(long lastUpdatedOn)
  {
    this.lastUpdatedOn = lastUpdatedOn;
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

    History history = (History) o;

    if (createdOn != history.createdOn)
    {
      return false;
    }
    if (lastUpdatedOn != history.lastUpdatedOn)
    {
      return false;
    }
    if (createdBy != null ? !createdBy.equals(history.createdBy) : history.createdBy != null)
    {
      return false;
    }
    if (lastUpdatedBy != null ? !lastUpdatedBy.equals(history.lastUpdatedBy) :
      history.lastUpdatedBy != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = createdBy != null ? createdBy.hashCode() : 0;
    result = 31 * result + (lastUpdatedBy != null ? lastUpdatedBy.hashCode() : 0);
    result = 31 * result + (int) (createdOn ^ (createdOn >>> 32));
    result = 31 * result + (int) (lastUpdatedOn ^ (lastUpdatedOn >>> 32));
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("createdBy", createdBy).
      append("lastUpdatedBy", lastUpdatedBy).
      append("createdOn", createdOn).
      append("lastUpdatedOn", lastUpdatedOn).
      toString();
  }
}
