package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemResolutionType  extends AbstractRevuEntity<ItemResolutionType> implements Comparable<ItemResolutionType>,
  IRevuNamedHolderEntity<ItemResolutionType>
{
  private String name;

  public ItemResolutionType()
  {
  }

  public ItemResolutionType(String name)
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

  public int compareTo(ItemResolutionType o)
  {
    return name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ItemResolutionType type = (ItemResolutionType) o;

    if (!name.equals(type.name)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }
}