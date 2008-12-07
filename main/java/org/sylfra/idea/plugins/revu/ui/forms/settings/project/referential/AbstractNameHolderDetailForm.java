package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuNamedHolderEntity;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractNameHolderDetailForm<T extends IRevuNamedHolderEntity<T>>
  extends AbstractReferentialDetailForm<T>
{
  private JPanel contentPane;
  private JTextField tfName;

  protected AbstractNameHolderDetailForm(TableView<T> table)
  {
    super(table);
  }

  public boolean isModified(@NotNull T data)
  {
    return (!checkEquals(tfName.getText(), data.getName()));
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
    updateError(tfName, checkAlreadyExist(tfName.getText(), 0), RevuBundle.message("general.valueAlreadExist.text"));
  }

  protected void internalUpdateUI(T data)
  {
    tfName.setText((data == null) ? "" : data.getName());
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