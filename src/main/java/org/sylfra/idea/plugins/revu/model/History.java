package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

public class History extends AbstractRevuEntity<History>
{
  private User createdBy;
  private User lastUpdatedBy;
  private Date createdOn;
  private Date lastUpdatedOn;

  public User getCreatedBy()
  {
    return createdBy;
  }

  public void setCreatedBy(User createdBy)
  {
    this.createdBy = createdBy;
  }

  public User getLastUpdatedBy()
  {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(User lastUpdatedBy)
  {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public Date getCreatedOn()
  {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn)
  {
    this.createdOn = createdOn;
  }

  public Date getLastUpdatedOn()
  {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(Date lastUpdatedOn)
  {
    this.lastUpdatedOn = lastUpdatedOn;
  }

  @Override
  public History clone()
  {
    History clone = super.clone();

    if (lastUpdatedBy != null)
    {
      clone.setLastUpdatedBy(lastUpdatedBy.clone());
    }

    if (createdBy != null)
    {
      clone.setCreatedBy(createdBy.clone());
    }

    return clone;
  }

  public int compareTo(History o)
  {
    return lastUpdatedBy.compareTo(o.getLastUpdatedBy());
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    History history = (History) o;

    if (createdBy != null ? !createdBy.equals(history.createdBy) : history.createdBy != null) return false;
    if (createdOn != null ? !createdOn.equals(history.createdOn) : history.createdOn != null) return false;
    if (lastUpdatedBy != null ? !lastUpdatedBy.equals(history.lastUpdatedBy) : history.lastUpdatedBy != null)
      return false;
    if (lastUpdatedOn != null ? !lastUpdatedOn.equals(history.lastUpdatedOn) : history.lastUpdatedOn != null)
      return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = createdBy != null ? createdBy.hashCode() : 0;
    result = 31 * result + (lastUpdatedBy != null ? lastUpdatedBy.hashCode() : 0);
    result = 31 * result + (createdOn != null ? createdOn.hashCode() : 0);
    result = 31 * result + (lastUpdatedOn != null ? lastUpdatedOn.hashCode() : 0);
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
