package org.sylfra.idea.plugins.revu.ui.forms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.IHistoryHolder;
import org.sylfra.idea.plugins.revu.model.User;

import javax.swing.*;
import java.text.DateFormat;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class HistoryForm extends AbstractUpdatableForm<IHistoryHolder>
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

  protected void internalUpdateUI(@NotNull IHistoryHolder data)
  {
    lbCreatedBy.setText(getUserName(data.getHistory().getCreatedBy()));
    lbLastUpdatedBy.setText(getUserName(data.getHistory().getLastUpdatedBy()));

    synchronized (DATE_FORMATTER)
    {
      lbCreatedOn.setText(DATE_FORMATTER.format(data.getHistory().getCreatedOn()));
      lbLastUpdatedOn.setText(DATE_FORMATTER.format(data.getHistory().getLastUpdatedOn()));
    }
  }

  private String getUserName(@NotNull User user)
  {
    return (user.getDisplayName() == null) ? user.getLogin() : user.getDisplayName();
  }

  public JComponent getPreferredFocusedComponent()
  {
    return null;
  }

  public boolean isModified(@NotNull IHistoryHolder data)
  {
    return false;
  }

  public void internalValidateInput()
  {
  }

  protected void internalUpdateData(@Nullable IHistoryHolder data)
  {
  }

}
