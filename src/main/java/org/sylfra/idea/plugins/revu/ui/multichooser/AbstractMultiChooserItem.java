package org.sylfra.idea.plugins.revu.ui.multichooser;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractMultiChooserItem<NestedData> implements IMultiChooserItem<NestedData>
{
  private final NestedData nestedData;

  protected AbstractMultiChooserItem(NestedData nestedData)
  {
    this.nestedData = nestedData;
  }

  public NestedData getNestedData()
  {
    return nestedData;
  }

  public int compareTo(IMultiChooserItem o)
  {
    return getName().compareTo(o.getName());
  }

  @Override
  public int hashCode()
  {
    return nestedData.hashCode();
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

    AbstractMultiChooserItem item = (AbstractMultiChooserItem) o;

    return nestedData.equals(item.getNestedData());
  }
}
