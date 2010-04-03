package org.sylfra.idea.plugins.revu.ui.multichooser;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IMultiChooserItem<T> extends Comparable<IMultiChooserItem>
{
  T getNestedData();
  String getName();
}
