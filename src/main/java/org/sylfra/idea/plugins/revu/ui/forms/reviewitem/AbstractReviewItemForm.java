package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReviewItemForm implements IReviewItemForm, Disposable
{
  @NotNull
  protected final Project project;

  @NotNull
  protected ReviewItem reviewItem;

  private Set<JComponent> errors;

  public AbstractReviewItemForm(@NotNull Project project)
  {
    this.project = project;
    errors = new HashSet<JComponent>();
  }

  @Nullable
  public ReviewItem getReviewItem()
  {
    return reviewItem;
  }

  public final void updateUI(@NotNull ReviewItem reviewItem)
  {
    this.reviewItem = reviewItem;
    errors.clear();
    internalUpdateUI();
    if (getPreferredFocusedComponent() != null)
    {
      getPreferredFocusedComponent().requestFocusInWindow();
    }
  }

  public boolean validateInput()
  {
    internalValidateInput();
    return errors.isEmpty();
  }

  public final boolean updateData(@NotNull ReviewItem reviewItemToUpdate)
  {
    if (!validateInput())
    {
      return false;
    }

    if (isModified(reviewItemToUpdate))
    {
      internalUpdateData(reviewItemToUpdate);
    }

    return true;
  }

  protected Object[] buildComboItemsArray(Collection<?> items)
  {
    List<Object> itemsClone = new ArrayList<Object>(items);
    itemsClone.add(0, null);

    return itemsClone.toArray(new Object[itemsClone.size()]);

  }

  protected void updateError(JComponent component, boolean hasError)
  {
    if (hasError)
    {
      component.setBorder(new ErrorBorder(component));
      errors.add(component);
    }
    else
    {
      if (component.getBorder() instanceof ErrorBorder)
      {
        component.setBorder(((ErrorBorder)component.getBorder()).nestedBorder);
      }
      errors.remove(component);
    }
  }

  public void dispose()
  {
  }

  protected boolean checkEquals(Object o1, Object o2)
  {
    return ((o1 == o2) || ((o1 != null) && (o1.equals(o2))));
  }

  protected abstract boolean isModified(@NotNull ReviewItem reviewItem);

  protected abstract void internalValidateInput();

  protected abstract void internalUpdateUI();

  protected abstract void internalUpdateData(@NotNull ReviewItem reviewItemToUpdate);

  private final static class ErrorBorder extends CompoundBorder
  {
    private final Border nestedBorder;

    private ErrorBorder(JComponent component)
    {
      super(BorderFactory.createLineBorder(Color.RED, 1), component.getBorder());
      nestedBorder = component.getBorder();
    }
  }
}
