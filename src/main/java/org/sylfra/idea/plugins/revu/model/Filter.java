package org.sylfra.idea.plugins.revu.model;

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
  private List<String> prioritieNames;
  private List<IssueStatus> statuses;
  private List<String> recipientLogins;
  private List<String> resolverLogins;
  private List<String> tagNames;

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

    if (fileRef != null ? !fileRef.equals(filter.fileRef) : filter.fileRef != null)
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

  @Override
  public int hashCode()
  {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (summary != null ? summary.hashCode() : 0);
    result = 31 * result + (fileRef != null ? fileRef.hashCode() : 0);
    result = 31 * result + (prioritieNames != null ? prioritieNames.hashCode() : 0);
    result = 31 * result + (statuses != null ? statuses.hashCode() : 0);
    result = 31 * result + (recipientLogins != null ? recipientLogins.hashCode() : 0);
    result = 31 * result + (resolverLogins != null ? resolverLogins.hashCode() : 0);
    result = 31 * result + (tagNames != null ? tagNames.hashCode() : 0);
    return result;
  }
}
