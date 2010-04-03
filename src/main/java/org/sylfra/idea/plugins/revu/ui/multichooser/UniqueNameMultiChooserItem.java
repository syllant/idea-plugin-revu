package org.sylfra.idea.plugins.revu.ui.multichooser;

import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class UniqueNameMultiChooserItem<T extends IRevuUniqueNameHolderEntity<T>>
  extends AbstractMultiChooserItem<T>
{
  public UniqueNameMultiChooserItem(T nestedData)
  {
    super(nestedData);
  }

  public String getName()
  {
    return getNestedData().getName();
  }
}
