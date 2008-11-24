package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

import javax.swing.*;

public abstract class AbstractDetailDialog<T> extends DialogWrapper
{
  private AbstractUpdatableForm<T> nestedForm;
  private T data;

  public AbstractDetailDialog()
  {
    super(false);
    nestedForm = buildNestedForm();
    init();
  }

  protected JComponent createCenterPanel()
  {
    return nestedForm.getContentPane();
  }

  @Override
  public JComponent getPreferredFocusedComponent()
  {
    return nestedForm.getPreferredFocusedComponent();
  }

  @NotNull
  protected abstract AbstractUpdatableForm<T> buildNestedForm();

  @Nls
  protected abstract String getTitleKey(boolean addMode);

  @NotNull
  protected abstract T createDefaultData();

  public void show(@Nullable T data)
  {
    setTitle(RevuBundle.message(getTitleKey((data == null))));
    if (data == null)
    {
      data = createDefaultData();
    }

    this.data = data;
    nestedForm.updateUI(data);
    super.show();
  }

  @Override
  protected void doOKAction()
  {
    if (nestedForm.updateData(data))
    {
      super.doOKAction();
    }
  }

  @NotNull
  public T getData()
  {
    return data;
  }
}
