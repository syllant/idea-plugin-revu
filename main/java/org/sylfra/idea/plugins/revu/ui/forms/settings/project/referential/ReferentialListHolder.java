package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import org.sylfra.idea.plugins.revu.model.AbstractRevuEntity;

import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReferentialListHolder<T> extends AbstractRevuEntity<ReferentialListHolder<T>>
{
  private List<T> allItems;
  private List<T> linkedItems;

  public ReferentialListHolder(List<T> allItems, List<T> linkedItems)
  {
    this.allItems = allItems;
    this.linkedItems = linkedItems;
  }

  public List<T> getAllItems()
  {
    return allItems;
  }

  public void setAllItems(List<T> allItems)
  {
    this.allItems = allItems;
  }

  public List<T> getLinkedItems()
  {
    return linkedItems;
  }

  public void setLinkedItems(List<T> linkedItems)
  {
    this.linkedItems = linkedItems;
  }
}
