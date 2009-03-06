package org.sylfra.idea.plugins.revu.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class Filter extends AbstractRevuEntity<Filter> implements IRevuUniqueNameHolderEntity<Filter>
{
  private String name;
  private String summary;
  private String fileRef;
  private Date createdBefore;
  private Date createdAfter;
  private Date lastUpdatedBefore;
  private Date lastUpdatedAfter;
  private List<String> prioritieNames;
  private List<String> recipientLogins;
  private List<String> resolverLogins;
  private List<IssueStatus> statuses;
  private List<String> tagNames;

  public Filter()
  {
    prioritieNames = new ArrayList<String>();
    recipientLogins = new ArrayList<String>();
    resolverLogins = new ArrayList<String>();
    statuses = new ArrayList<IssueStatus>();
    tagNames = new ArrayList<String>();
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getSummary()
  {
    return summary;
  }

  public void setSummary(String summary)
  {
    this.summary = summary;
  }

  public String getFileRef()
  {
    return fileRef;
  }

  public void setFileRef(String fileRef)
  {
    this.fileRef = fileRef;
  }

  public List<String> getPrioritieNames()
  {
    return prioritieNames;
  }

  public void setPrioritieNames(List<String> prioritieNames)
  {
    this.prioritieNames = prioritieNames;
  }

  public List<IssueStatus> getStatuses()
  {
    return statuses;
  }

  public void setStatuses(List<IssueStatus> statuses)
  {
    this.statuses = statuses;
  }

  public List<String> getRecipientLogins()
  {
    return recipientLogins;
  }

  public void setRecipientLogins(List<String> recipientLogins)
  {
    this.recipientLogins = recipientLogins;
  }

  public List<String> getResolverLogins()
  {
    return resolverLogins;
  }

  public void setResolverLogins(List<String> resolverLogins)
  {
    this.resolverLogins = resolverLogins;
  }

  public List<String> getTagNames()
  {
    return tagNames;
  }

  public void setTagNames(List<String> tagNames)
  {
    this.tagNames = tagNames;
  }

  public Date getCreatedBefore()
  {
    return createdBefore;
  }

  public void setCreatedBefore(Date createdBefore)
  {
    this.createdBefore = createdBefore;
  }

  public Date getCreatedAfter()
  {
    return createdAfter;
  }

  public void setCreatedAfter(Date createdAfter)
  {
    this.createdAfter = createdAfter;
  }

  public Date getLastUpdatedBefore()
  {
    return lastUpdatedBefore;
  }

  public void setLastUpdatedBefore(Date lastUpdatedBefore)
  {
    this.lastUpdatedBefore = lastUpdatedBefore;
  }

  public Date getLastUpdatedAfter()
  {
    return lastUpdatedAfter;
  }

  public void setLastUpdatedAfter(Date lastUpdatedAfter)
  {
    this.lastUpdatedAfter = lastUpdatedAfter;
  }

  public int compareTo(Filter o)
  {
    return name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof Filter))
    {
      return false;
    }

    Filter filter = (Filter) o;

    if (createdAfter != null ? !createdAfter.equals(filter.createdAfter) : filter.createdAfter != null)
    {
      return false;
    }
    if (createdBefore != null ? !createdBefore.equals(filter.createdBefore) : filter.createdBefore != null)
    {
      return false;
    }
    if (fileRef != null ? !fileRef.equals(filter.fileRef) : filter.fileRef != null)
    {
      return false;
    }
    if (lastUpdatedAfter != null ? !lastUpdatedAfter.equals(filter.lastUpdatedAfter) : filter.lastUpdatedAfter != null)
    {
      return false;
    }
    if (lastUpdatedBefore != null ? !lastUpdatedBefore.equals(filter.lastUpdatedBefore) :
      filter.lastUpdatedBefore != null)
    {
      return false;
    }
    if (name != null ? !name.equals(filter.name) : filter.name != null)
    {
      return false;
    }
    if (prioritieNames != null ? !prioritieNames.equals(filter.prioritieNames) : filter.prioritieNames != null)
    {
      return false;
    }
    if (recipientLogins != null ? !recipientLogins.equals(filter.recipientLogins) : filter.recipientLogins != null)
    {
      return false;
    }
    if (resolverLogins != null ? !resolverLogins.equals(filter.resolverLogins) : filter.resolverLogins != null)
    {
      return false;
    }
    if (statuses != null ? !statuses.equals(filter.statuses) : filter.statuses != null)
    {
      return false;
    }
    if (summary != null ? !summary.equals(filter.summary) : filter.summary != null)
    {
      return false;
    }
    if (tagNames != null ? !tagNames.equals(filter.tagNames) : filter.tagNames != null)
    {
      return false;
    }

    return true;
  }
}
