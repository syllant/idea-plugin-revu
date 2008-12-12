package org.sylfra.idea.plugins.revu.config.impl;

import org.sylfra.idea.plugins.revu.model.IRevuNamedHolderEntity;

import java.util.Collection;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ConverterUtils
{
  public static String toString(Collection<?> collection, boolean toLowerCase)
  {
    StringBuilder b = new StringBuilder();
    for (Object o : collection)
    {
      String value = (o instanceof IRevuNamedHolderEntity) ? ((IRevuNamedHolderEntity) o).getName() : o.toString();
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
