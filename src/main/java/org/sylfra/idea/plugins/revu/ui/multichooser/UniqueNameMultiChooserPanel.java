package org.sylfra.idea.plugins.revu.ui.multichooser;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.DataReferential;
import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class UniqueNameMultiChooserPanel<T extends IRevuUniqueNameHolderEntity<T>>
  extends MultiChooserPanel<T, AbstractMultiChooserItem<T>>
{
  public UniqueNameMultiChooserPanel(@NotNull Project project, @NotNull JLabel label, @NotNull String popupTitle,
    @Nullable String dimensionKeySuffix, @Nullable RevuIconProvider.IconRef iconRef)
  {
    super(project, label, popupTitle, dimensionKeySuffix, iconRef);
  }

  protected AbstractMultiChooserItem<T> createMultiChooserItem(@NotNull final T nestedData)
  {
    return new AbstractMultiChooserItem<T>(nestedData)
    {
      public String getName()
      {
        return nestedData.getName();
      }
    };
  }

  @Override
  protected List<T> retrieveAllAvailableElements()
  {
    List<Review> reviews = project.getComponent(ReviewManager.class).getReviews();
    Set<String> names = new HashSet<String>();
    List<T> result = new ArrayList<T>();
    for (Review review : reviews)
    {
      List<T> items = getReferentialItems(review.getDataReferential());
      for (T item : items)
      {
        if (!names.contains(item.getName()))
        {
          result.add(item);
        }
      }
    }

    return result;
  }

  @NotNull
  protected abstract List<T> getReferentialItems(@NotNull DataReferential dataReferential);
}
