package org.sylfra.idea.plugins.revu.ui.forms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.IRevuEntity;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractUpdatableForm<T extends IRevuEntity<T>> implements IUpdatableForm<T>
{
  protected final java.util.List<JComponent> errors;
  protected final java.util.List<UpdatableFormListener<T>> listeners;
  private Review enclosingReview;

  protected AbstractUpdatableForm()
  {
    errors = new ArrayList<JComponent>();
    listeners = new ArrayList<UpdatableFormListener<T>>();
  }

  public Review getEnclosingReview(@Nullable T data)
  {
    return enclosingReview;
  }

  public void addUpdatableFormListener(UpdatableFormListener<T> listener)
  {
    listeners.add(listener);
  }

  public boolean validateInput(@Nullable T data)
  {
    clearErrors();
    internalValidateInput(data);
    return errors.isEmpty();
  }

  protected Object[] buildComboItemsArray(Collection<?> items, boolean required)
  {
    java.util.List<Object> itemsClone = new ArrayList<Object>(items);
    if (required)
    {
      itemsClone.add(0, null);
    }

    return itemsClone.toArray(new Object[itemsClone.size()]);
  }

  protected void updateRequiredError(JComponent component, boolean hasError)
  {
    updateError(component, hasError, RevuBundle.message("general.fieldRequired.text"));
  }

  protected void updateError(@NotNull JComponent component, boolean hasError, @Nullable String message)
  {
    // One error max by component
    if (errors.contains(component))
    {
      return;
    }

    JLabel labelFor = (JLabel) component.getClientProperty("labeledBy");
    boolean hasErrorBorder = component.getBorder() instanceof ErrorBorder;
    if (hasError)
    {
      // Can't report an error on a disabled component because user won't be able to fix it !
      if (!component.isEnabled())
      {
        return;
      }

      if (message != null)
      {
        if (!hasErrorBorder)
        {
          component.setBorder(new ErrorBorder(component));
        }
        component.setToolTipText(message);
      }
      errors.add(component);
      if (labelFor != null)
      {
        labelFor.setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.FIELD_ERROR));
        labelFor.setHorizontalTextPosition(SwingConstants.LEFT);

        if (message != null)
        {
          labelFor.setToolTipText(message);
        }
      }
    }
    else
    {
      if (hasErrorBorder)
      {
        component.setBorder(((ErrorBorder)component.getBorder()).nestedBorder);
      }
      component.setToolTipText(null);
      errors.remove(component);

      if (labelFor != null)
      {
        labelFor.setIcon(null);
        labelFor.setToolTipText("");
      }
    }
  }

  protected void clearErrors()
  {
    for (Iterator<JComponent> it = errors.listIterator(); it.hasNext();)
    {
      updateError(it.next(), false, null);
      it.remove();
    }
  }

  protected void updateTabIcons(JTabbedPane tabbedPane)
  {
    for (int i = 0; i < tabbedPane.getComponents().length; i++)
    {
      boolean hasError = false;
      Component component = tabbedPane.getComponentAt(i);
      for (JComponent errorComponent : errors)
      {
        if (SwingUtilities.isDescendingFrom(errorComponent, component))
        {
          hasError = true;
          break;
        }
      }
      tabbedPane.setIconAt(i, hasError ? RevuIconProvider.getIcon(RevuIconProvider.IconRef.FIELD_ERROR) : null);
    }
  }

  public void dispose()
  {
  }

  protected boolean checkEquals(Object o1, Object o2)
  {
    return ((o1 == o2)
      || ((o1 != null) && (o1.equals(o2)))
      || ((o2 == null) && ("".equals(o1)))
      || ((o1 == null) && ("".equals(o2))));
  }

  public final void updateUI(Review enclosingReview, @Nullable T data, boolean requestFocus)
  {
    this.enclosingReview = enclosingReview;

    clearErrors();
    internalValidateInput(null);

    internalUpdateUI(data, requestFocus);

    internalUpdateWriteAccess(data, RevuUtils.getCurrentUser(getEnclosingReview(data)));

    JComponent component = getPreferredFocusedComponent();
    if ((requestFocus) && (component != null))
    {
      component.requestFocusInWindow();
    }

    for (UpdatableFormListener<T> listener : listeners)
    {
      listener.uiUpdated(enclosingReview, data);
    }
  }

  public final boolean updateData(@NotNull T data)
  {
    if (!isModified(data))
    {
      return true;
    }

    if (!validateInput(data))
    {
      errors.get(0).requestFocusInWindow();
      return false;
    }

    internalUpdateData(data);

    for (UpdatableFormListener<T> listener : listeners)
    {
      listener.dataUpdated(data);
    }

    return true;
  }

  protected boolean isHabilitedToEditReview(@Nullable T data, User user)
  {
    Review enclosingReview = getEnclosingReview(data);

    return (enclosingReview != null) && (!enclosingReview.isEmbedded())
      && (user != null) && (user.hasRole(User.Role.ADMIN));
  }

  public abstract boolean isModified(@NotNull T data);

  protected abstract void internalUpdateWriteAccess(@Nullable T data , @Nullable User user);

  protected abstract void internalValidateInput(@Nullable T data);

  protected abstract void internalUpdateUI(@Nullable T data, boolean requestFocus);

  protected abstract void internalUpdateData(@NotNull T data);

  private final static class ErrorBorder extends CompoundBorder
  {
    private final Border nestedBorder;

    private ErrorBorder(JComponent component)
    {
      super(BorderFactory.createLineBorder(Color.RED, 1), component.getBorder());
      nestedBorder = component.getBorder();
    }
  }

  public static interface UpdatableFormListener<T>
  {
    void uiUpdated(Review enclosingReview, @Nullable T data);
    void dataUpdated(@NotNull T data);
  }

  public static interface UpdatableEntityListListener<T>
  {
    void entityAdded(List<T> items, @NotNull T item);
    void entityDeleted(List<T> items, @NotNull T item);
  }
}
