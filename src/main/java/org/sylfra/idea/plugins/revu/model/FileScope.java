package org.sylfra.idea.plugins.revu.model;

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
}
