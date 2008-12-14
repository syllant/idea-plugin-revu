package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TableUtil;
import org.sylfra.idea.plugins.revu.model.Issue;

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

  public String getElementText(Object item)
  {
    return ((Issue) item).getSummary();
  }

  public void selectElement(Object item, String text)
  {
    int index = myComponent.getListTableModel().indexOf((Issue) item);
    if (index > -1)
    {
      myComponent.setRowSelectionInterval(index, index);
      TableUtil.scrollSelectionToVisible(myComponent);
    }
  }
}
