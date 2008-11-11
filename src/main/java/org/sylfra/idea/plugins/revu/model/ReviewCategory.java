package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewCategory implements Comparable<ReviewCategory>
{
  private String name;

  public ReviewCategory(String name)
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

  public int compareTo(ReviewCategory o)
  {
    return name.compareTo(o.getName());
  }
}