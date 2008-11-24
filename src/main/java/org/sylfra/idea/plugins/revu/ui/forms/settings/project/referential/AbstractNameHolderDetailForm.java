package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.INamedHolder;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractNameHolderDetailForm<T extends INamedHolder> extends AbstractUpdatableForm<T>
{
  private JPanel contentPane;
  private JTextField tfName;

  public boolean isModified(@NotNull T data)
  {
    return (!checkEquals(tfName.getText(), data.getName()));
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
  }

  protected void internalUpdateUI(T data)
  {
    tfName.setText(data.getName());
  }

  protected void internalUpdateData(@NotNull T data)
  {
    data.setName(tfName.getText());
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfName;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }
}