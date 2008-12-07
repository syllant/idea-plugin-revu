package org.sylfra.idea.plugins.revu.model;

import java.io.Serializable;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuEntity<T> extends Comparable<T>, Serializable, Cloneable
{
  T clone();
}
