package org.sylfra.idea.plugins.revu.ui.multichooser;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IMultiChooserItem<T> extends Comparable<IMultiChooserItem>
{
  T getNestedData();
  String getName();
}
