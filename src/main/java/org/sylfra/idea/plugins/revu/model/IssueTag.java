package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTag extends AbstractRevuEntity<IssueTag> implements Comparable<IssueTag>,
  IRevuUniqueNameHolderEntity<IssueTag>
{
  private String name;

  public IssueTag()
  {
  }

  public IssueTag(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int compareTo(IssueTag o)
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
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    IssueTag issueTag = (IssueTag) o;

    if (name != null ? !name.equals(issueTag.name) : issueTag.name != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return name != null ? name.hashCode() : 0;
  }
}