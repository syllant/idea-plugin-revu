package org.sylfra.idea.plugins.revu.ui.multichooser;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StringMultiChooserItem extends AbstractMultiChooserItem<String>
{
  public StringMultiChooserItem(String nestedData)
  {
    super(nestedData);
  }

  public String getName()
  {
    return getNestedData();
  }
}