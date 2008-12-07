package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.AbstractRevuEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReferentialListHolder<T> extends AbstractRevuEntity<ReferentialListHolder<T>>
{
  private List<T> allItems;
  private List<T> items;
  private List<T> linkedItems;

  public ReferentialListHolder(@NotNull List<T> items, @Nullable List<T> linkedItems)
  {
    this.items = items;
    this.linkedItems = linkedItems;

    allItems = new ArrayList<T>(items.size() + ((linkedItems == null) ? 0 : linkedItems.size()));
    allItems.addAll(items);
    if (linkedItems != null)
    {
      allItems.addAll(linkedItems);
    }
  }

  public List<T> getItems()
  {
    return items;
  }

  public void setItems(List<T> items)
  {
    this.items = items;
  }

  public List<T> getLinkedItems()
  {
    return linkedItems;
  }

  public void setLinkedItems(List<T> linkedItems)
  {
    this.linkedItems = linkedItems;
  }

  public List<T> getAllItems()
  {
    return allItems;
  }

  public int compareTo(ReferentialListHolder<T> o)
  {
    return 0;
  }
}
