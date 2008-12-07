package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractRevuEntity<T> implements IRevuEntity<T>
{
  public T clone()
  {
    T clone;
    try
    {
      //noinspection unchecked
      clone = (T) super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      Logger.getInstance(getClass().getName()).warn(e);
      return null;
    }

    return clone;
  }

  protected <T extends IRevuEntity> List<T> cloneList(@NotNull List<T> list)
  {
    List<T> result = new ArrayList<T>(list.size());
    for (T object : list)
    {
      //noinspection unchecked
      result.add((T) object.clone());
    }
    
    return result;
  }
}
