package org.sylfra.idea.plugins.revu.model;

import java.util.Date;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FileScope extends AbstractRevuEntity<FileScope>
{
  private String pathPattern;
  private String rev;
  private Date date;

  public String getPathPattern()
  {
    return pathPattern;
  }

  public void setPathPattern(String pathPattern)
  {
    this.pathPattern = pathPattern;
  }

  public String getRev()
  {
    return rev;
  }

  public void setRev(String rev)
  {
    this.rev = rev;
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public int compareTo(FileScope o)
  {
    return 0;
  }
}
