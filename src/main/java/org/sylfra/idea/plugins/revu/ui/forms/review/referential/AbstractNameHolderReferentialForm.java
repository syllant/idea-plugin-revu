package org.sylfra.idea.plugins.revu.ui.forms.review.referential;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractNameHolderReferentialForm<T extends IRevuUniqueNameHolderEntity<T>>
  extends AbstractReferentialForm<T>
{
  protected AbstractNameHolderReferentialForm(Project project)
  {
    super(project);
  }

  protected IDetailDialogFactory<T> buildDetailDialogFactory()
  {
    return new IDetailDialogFactory<T>()
    {
      @NotNull
      public AbstractDetailDialog<T> createDialog()
      {
        return new AbstractDetailDialog<T>()
        {
          protected AbstractReferentialDetailForm<T> buildNestedForm()
          {
            return buildNestedFormForDialog();
          }

          @Nls
          protected String getTitleKey(boolean addMode)
          {
            return getTitleKeyForDialog(addMode);
          }

          protected T createDefaultData()
          {
            return createDefaultDataForDialog();
          }
        };
      }
    };
  }

  protected ReferentialColumnInfo<T, ?>[] buildColumnInfos()
  {
    //noinspection unchecked
    return new ReferentialColumnInfo[]
      {
        new ReferentialColumnInfo<T, String>(RevuBundle.message(
          "projectSettings.review.referential.namedHolder.table.name.title"))
        {
          public String valueOf(T data)
          {
            return data.getName();
          }
        }
      };
  }

  protected abstract AbstractReferentialDetailForm<T> buildNestedFormForDialog();

  protected abstract String getTitleKeyForDialog(boolean addMode);

  @NotNull
  protected abstract T createDefaultDataForDialog();
}