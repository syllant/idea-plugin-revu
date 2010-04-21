package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FileScope extends AbstractRevuEntity<FileScope>
{
  private String pathPattern;
  private String vcsBeforeRev;
  private String vcsAfterRev;

  public String getPathPattern()
  {
    return pathPattern;
  }

  public void setPathPattern(String pathPattern)
  {
    this.pathPattern = pathPattern;
  }

  public String getVcsBeforeRev()
  {
    return vcsBeforeRev;
  }

  public void setVcsBeforeRev(String vcsBeforeRev)
  {
    this.vcsBeforeRev = vcsBeforeRev;
  }

  public String getVcsAfterRev()
  {
    return vcsAfterRev;
  }

  public void setVcsAfterRev(String vcsAfterRev)
  {
    this.vcsAfterRev = vcsAfterRev;
  }

  public int compareTo(FileScope o)
  {
    return 0;
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof FileScope))
    {
      return false;
    }
    if (this == o)
    {
      return true;
    }

    FileScope fs = (FileScope) o;
    return new EqualsBuilder()
      .append(pathPattern, fs.pathPattern)
      .append(vcsAfterRev, fs.vcsAfterRev)
      .append(vcsBeforeRev, fs.vcsBeforeRev)
      .isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder()
      .append(pathPattern)
      .append(vcsAfterRev)
      .append(vcsBeforeRev)
      .toHashCode();
  }
}
