package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.user;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractDetailDialog;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.AbstractReferentialForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class UserReferentialForm extends AbstractReferentialForm<User>
{
  public UserReferentialForm(Project project)
  {
    super(project);
  }

  protected boolean isTableSelectionMovable()
  {
    return false;
  }

  @Override
  protected void internalValidateInput()
  {
    super.internalValidateInput();

    // Check if current user is contained in list
    boolean adminFound = false;
    boolean currentUserFound = false;
    for (User user : table.getListTableModel().getItems())
    {
      if (user.getLogin().equals(RevuUtils.getCurrentUserLogin()))
      {
        currentUserFound = true;
        if (adminFound)
        {
          break;
        }
      }

      if (user.getRoles().contains(User.Role.ADMIN))
      {
        adminFound = true;
        if (currentUserFound)
        {
          break;
        }
      }
    }

    updateError(table, !currentUserFound,
      RevuBundle.message("settings.project.review.referential.user.form.currentUserNotFound.message",
        RevuUtils.getCurrentUserLogin()));

    updateError(table, !adminFound,
      RevuBundle.message("settings.project.review.referential.user.form.adminNotFound.message"));
  }

  protected IDetailDialogFactory<User> buildDetailDialogFactory()
  {
    return new IDetailDialogFactory<User>()
    {
      @NotNull
      public AbstractDetailDialog<User> createDialog()
      {
        return new AbstractDetailDialog<User>()
        {
          protected AbstractUpdatableForm<User> buildNestedForm()
          {
            return new UserDetailForm();
          }

          @Nls
          protected String getTitleKey(boolean addMode)
          {
            return addMode
              ? "settings.project.review.referential.user.addDialog.title"
              : "settings.project.review.referential.user.editDialog.title";
          }

          @NotNull
          protected User createDefaultData()
          {
            return new User();
          }
        };
      }
    };
  }

  protected ReferentialColumnInfo<User, ?>[] buildColumnInfos()
  {
    return new ReferentialColumnInfo[]
      {
        new ReferentialColumnInfo<User, String>(RevuBundle.message("settings.project.review.referential.user.table.login.title"))
        {
          public String valueOf(User user)
          {
            return user.getLogin();
          }
        },
        new ReferentialColumnInfo<User, String>(RevuBundle.message("settings.project.review.referential.user.table.displayName.title"))
        {
          public String valueOf(User user)
          {
            return user.getDisplayName();
          }
        },
        new ReferentialColumnInfo<User, Boolean>(RevuBundle.message("userRoles.admin.text"))
        {
          public Boolean valueOf(User user)
          {
            return user.getRoles().contains(User.Role.ADMIN);
          }

          @Override
          public Class getColumnClass()
          {
            return Boolean.class;
          }

          @Override
          public int getWidth(JTable table)
          {
            return 50;
          }
        },
        new ReferentialColumnInfo<User, Boolean>(RevuBundle.message("userRoles.reviewer.text"))
        {
          public Boolean valueOf(User user)
          {
            return user.getRoles().contains(User.Role.REVIEWER);
          }

          @Override
          public Class getColumnClass()
          {
            return Boolean.class;
          }

          @Override
          public int getWidth(JTable table)
          {
            return 50;
          }
        },
        new ReferentialColumnInfo<User, Boolean>(RevuBundle.message("userRoles.author.text"))
        {
          public Boolean valueOf(User user)
          {
            return user.getRoles().contains(User.Role.AUTHOR);
          }

          @Override
          public Class getColumnClass()
          {
            return Boolean.class;
          }

          @Override
          public int getWidth(JTable table)
          {
            return 50;
          }
        }
      };
  }
}