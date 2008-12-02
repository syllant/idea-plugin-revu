package org.sylfra.idea.plugins.revu.model;

import java.io.Serializable;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuEntity<T> extends Serializable, Cloneable
{
  T clone();
}
