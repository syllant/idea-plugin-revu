package org.sylfra.idea.plugins.revu.ui.forms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.IRevuHistoryHolderEntity;
import org.sylfra.idea.plugins.revu.model.User;

import javax.swing.*;
import java.text.DateFormat;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class HistoryForm<T extends IRevuHistoryHolderEntity<T>> extends AbstractUpdatableForm<T>
{
  public static final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(
    DateFormat.LONG, DateFormat.LONG);

  private JPanel contentPane;
  private JLabel lbCreatedBy;
  private JLabel lbCreatedOn;
  private JLabel lbLastUpdatedBy;
  private JLabel lbLastUpdatedOn;

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI(T data)
  {
    lbCreatedBy.setText((data == null) ? "" : getUserName(data.getHistory().getCreatedBy()));
    lbLastUpdatedBy.setText((data == null) ? "" : getUserName(data.getHistory().getLastUpdatedBy()));

    synchronized (DATE_FORMATTER)
    {
      lbCreatedOn.setText(((data == null) || (data.getHistory().getCreatedOn() == null))
        ? "" : DATE_FORMATTER.format(data.getHistory().getCreatedOn()));
      lbLastUpdatedOn.setText(((data == null) || (data.getHistory().getLastUpdatedOn() == null))
        ? "" : DATE_FORMATTER.format(data.getHistory().getLastUpdatedOn()));
    }
  }

  private String getUserName(@Nullable User user)
  {
    return (user == null) ? "" : (user.getDisplayName() == null) ? user.getLogin() : user.getDisplayName();
  }

  public JComponent getPreferredFocusedComponent()
  {
    return null;
  }

  public boolean isModified(@NotNull T data)
  {
    return false;
  }

  public void internalValidateInput()
  {
  }

  protected void internalUpdateData(@Nullable T data)
  {
  }

}
