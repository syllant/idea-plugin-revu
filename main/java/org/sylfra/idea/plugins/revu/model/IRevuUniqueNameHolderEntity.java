package org.sylfra.idea.plugins.revu.model;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuUniqueNameHolderEntity<T> extends IRevuEntity<T>
{
  String getName();
  void setName(String name);
}
