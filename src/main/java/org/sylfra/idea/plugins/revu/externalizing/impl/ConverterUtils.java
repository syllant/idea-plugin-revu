package org.sylfra.idea.plugins.revu.externalizing.impl;

import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;

import java.util.Collection;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ConverterUtils
{
  public static String toString(Collection<?> collection, boolean toLowerCase)
  {
    StringBuilder b = new StringBuilder();
    for (Object o : collection)
    {
      String value = (o instanceof IRevuUniqueNameHolderEntity) ? ((IRevuUniqueNameHolderEntity) o).getName() : o.toString();
      b.append(value).append(",");
    }

    if (collection.size() > 0)
    {
      b.deleteCharAt(b.length() - 1);
    }

    String result = b.toString();
    if (toLowerCase)
    {
      result = result.toLowerCase();
    }

    return result;
  }
}
