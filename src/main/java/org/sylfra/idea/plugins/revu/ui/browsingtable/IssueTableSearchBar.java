package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TableUtil;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class IssueTableSearchBar extends SpeedSearchBase<IssueTable>
{
  public IssueTableSearchBar(IssueTable table)
  {
    super(table);
  }

  public int getSelectedIndex()
  {
    return myComponent.getSelectedRow();
  }

  public Object[] getAllElements()
  {
    return myComponent.getListTableModel().getItems().toArray();
  }

  public String getElementText(Object obj)
  {
    return obj.toString();
  }

  public void selectElement(Object obj, String s1)
  {
    myComponent.setRowSelectionInterval(0, 0);
    TableUtil.scrollSelectionToVisible(myComponent);
  }
}
