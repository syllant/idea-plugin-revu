package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ItemCategory  extends AbstractRevuEntity<ItemCategory> implements Comparable<ItemCategory>,
  IRevuNamedHolderEntity<ItemCategory>
{
  private String name;

  public ItemCategory()
  {
  }

  public ItemCategory(String name)
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

  public int compareTo(ItemCategory o)
  {
    return name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ItemCategory category = (ItemCategory) o;

    if (!name.equals(category.name)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }
}