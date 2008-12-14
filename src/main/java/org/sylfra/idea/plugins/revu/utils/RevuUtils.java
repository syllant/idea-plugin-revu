package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id: RevuUtils.java 7 2008-11-15 09:20:32Z syllant $
 */
public class RevuUtils
{
  @Nullable
  public static PsiFile getPsiFile(@NotNull Project project, @NotNull Issue issue)
  {
    return PsiManager.getInstance(project).findFile(issue.getFile());
  }

  @Nullable
  public static Document getDocument(@NotNull Project project, @NotNull Issue issue)
  {
    PsiFile psiFile = getPsiFile(project, issue);
    return (psiFile == null) ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
  }

  @NotNull
  public static List<Editor> getEditors(@NotNull Issue issue)
  {
    List<Editor> result = new ArrayList<Editor>();

    Editor[] editors = EditorFactory.getInstance().getAllEditors();
    for (Editor editor : editors)
    {
      VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (issue.getFile().equals(vFile))
      {
        result.add(editor);
      }
    }

    return result;
  }

  @NotNull
  public static String z(@Nullable String s1, @Nullable String s2)
  {
    if ((s1 == null) || ("".equals(s1)))
    {
      return "";
    }

    return DigestUtils.md5Hex(s1 + RevuPlugin.PLUGIN_NAME + ((s2 == null) ? "" : s2));
  }

  @Nullable
  public static String getCurrentUserLogin()
  {
    return ServiceManager.getService(RevuAppSettingsComponent.class).getState().getLogin();
  }

  @Nullable
  public static User getCurrentUser()
  {
    RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();
    if (appSettings.getLogin() == null)
    {
      return null;
    }

    User user = new User();
    user.setLogin(appSettings.getLogin());
    user.setPassword(appSettings.getPassword());
    user.setDisplayName(appSettings.getLogin());

    return user;
  }

  @Nullable
  public static User getCurrentUser(@Nullable Review review)
  {
    User user;
    if (review == null)
    {
      user = null;
    }
    else
    {
      String login = RevuUtils.getCurrentUserLogin();
      user = (login == null) ? null : review.getDataReferential().getUser(login, true);
    }

    return user;
  }

  @NotNull
  public static User getNonNullUser(@Nullable User user)
  {
    return (user == null) ? User.UNKNOWN : user;
  }

  @NotNull
  public static User getNonNullUser(@NotNull DataReferential dataReferential, @Nullable String login)
  {
    if (login == null)
    {
      return User.UNKNOWN;
    }

    if (User.DEFAULT.getLogin().equals(login))
    {
      return User.DEFAULT;
    }

    return getNonNullUser(dataReferential.getUser(login, true));
  }

  public static void configureTextAreaAsStandardField(@NotNull final JTextArea... textAreas)
  {
    for (JTextArea textArea : textAreas)
    {
      AbstractAction nextTabAction = new AbstractAction("NextTab")
      {
        public void actionPerformed(ActionEvent evt)
        {
          ((JTextArea) evt.getSource()).transferFocus();
        }
      };
      AbstractAction previousTabAction = new AbstractAction("PreviousTab")
      {
        public void actionPerformed(ActionEvent evt)
        {
          ((JTextArea) evt.getSource()).transferFocusBackward();
        }
      };

      textArea.getActionMap().put(nextTabAction.getValue(Action.NAME), nextTabAction);
      textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false),
        nextTabAction.getValue(Action.NAME));

      textArea.getActionMap().put(previousTabAction.getValue(Action.NAME), previousTabAction);
      textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK, false), 
        previousTabAction.getValue(Action.NAME));
    }
  }

  public static void setWriteAccess(boolean canWrite, @NotNull JComponent... components)
  {
    for (JComponent component : components)
    {
      if (component instanceof JTextComponent)
      {
        ((JTextComponent) component).setEditable(canWrite);
      }
      else
      {
        component.setEnabled(canWrite);
      }
    }
  }

  @NotNull
  public static String buildFileNameFromReviewName(@NotNull String name)
  {
    return FileUtil.sanitizeFileName(name) + ".xml";
  }

  @NotNull
  public static String buildIssueStatusLabel(@NotNull IssueStatus status)
  {
    return RevuBundle.message("general.issueStatus." + status.toString().toLowerCase() + ".text");
  }

  @NotNull
  public static String buildReviewStatusLabel(@NotNull ReviewStatus status)
  {
    return RevuBundle.message("general.reviewStatus." + status.toString().toLowerCase() + ".text");
  }

  @NotNull
  public static History buildHistory(@NotNull Review review)
  {
    String login = getCurrentUserLogin();
    User user = (login == null) ? User.UNKNOWN : review.getDataReferential().getUser(login, true);

    Date now = new Date();

    History history = new History();
    history.setCreatedBy(user);
    history.setCreatedOn(now);
    history.setLastUpdatedBy(user);
    history.setLastUpdatedOn(now);

    return history;
  }

  public static boolean isActive(@NotNull Review review)
  {
    return ((review.getStatus() == ReviewStatus.FIXING) || (review.getStatus() == ReviewStatus.REVIEWING));
  }
}
