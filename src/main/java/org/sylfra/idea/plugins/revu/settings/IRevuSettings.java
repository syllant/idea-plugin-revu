package org.sylfra.idea.plugins.revu.settings;

import java.io.Serializable;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuSettings<T extends IRevuSettings> extends Serializable, Cloneable
{
  T clone();
}
