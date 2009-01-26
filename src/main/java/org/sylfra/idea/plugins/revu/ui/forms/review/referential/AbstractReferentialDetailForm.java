package org.sylfra.idea.plugins.revu.ui.forms.review.referential;

import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.IRevuEntity;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReferentialDetailForm<T extends IRevuEntity<T>> extends AbstractUpdatableForm<T>
{
  private TableView<T> table;
  private boolean createMode;

  protected AbstractReferentialDetailForm(TableView<T> table)
  {
    this.table = table;
  }

  public void setCreateMode(boolean createMode)
  {
    this.createMode = createMode;
  }

  protected boolean checkAlreadyExist(@Nullable String value, int colIndex)
  {
    if ((!createMode) || (value == null))
    {
      return false;
    }

    for (int i=0; i < table.getRowCount(); i++)
    {
      if (value.equals(table.getValueAt(i, colIndex)))
      {
        return true;
      }
    }

    return false;
  }
}
