package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuEntity;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;

public abstract class AbstractDetailDialog<T extends IRevuEntity<T>> extends DialogWrapper
{
  private AbstractReferentialDetailForm<T> nestedForm;
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
  protected abstract AbstractReferentialDetailForm<T> buildNestedForm();

  @Nls
  protected abstract String getTitleKey(boolean addMode);

  @NotNull
  protected abstract T createDefaultData();

  public void show(@Nullable Review review, @Nullable T data)
  {
    nestedForm.setCreateMode(data == null);
    setTitle(RevuBundle.message(getTitleKey((data == null))));
    if (data == null)
    {
      data = createDefaultData();
    }

    this.data = data;
    nestedForm.updateUI(review, data);
    super.show();
  }

  @Override
  protected void doOKAction()
  {
    if (nestedForm.updateData(data))
    {
      super.doOKAction();
    }
    else
    {
      setErrorText(RevuBundle.message("general.form.hasErrors.text"));
    }
  }

  @NotNull
  public T getData()
  {
    return data;
  }
}
