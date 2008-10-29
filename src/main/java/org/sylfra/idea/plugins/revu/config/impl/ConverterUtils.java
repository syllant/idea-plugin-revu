package org.sylfra.idea.plugins.revu.config.impl;

import java.util.Collection;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ConverterUtils
{
  public static String toString(Collection<?> collection, boolean toLowerCase)
  {
    StringBuilder b = new StringBuilder();
    for (Object o : collection)
    {
      b.append(o.toString()).append(",");
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
