package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuUniqueNameHolderEntity<T> extends IRevuEntity<T>
{
  String getName();
  void setName(String name);
}
