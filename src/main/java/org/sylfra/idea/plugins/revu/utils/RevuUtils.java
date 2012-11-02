package org.sylfra.idea.plugins.revu.utils;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.forms.settings.RevuAppSettingsForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.RevuProjectSettingsForm;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: RevuUtils.java 7 2008-11-15 09:20:32Z syllant $
 */
public class RevuUtils
{
  @Nullable
  public static Project getProject()
  {
    return DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());
  }

  @Nullable
  public static PsiFile getPsiFile(@NotNull Project project, @NotNull Issue issue)
  {
    return (issue.getFile() == null) ? null : PsiManager.getInstance(project).findFile(issue.getFile());
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

    if (issue.getFile() != null)
    {
      Editor[] editors = EditorFactory.getInstance().getAllEditors();
      for (Editor editor : editors)
      {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (issue.getFile().equals(vFile))
        {
          result.add(editor);
        }
      }
    }

    return result;
  }

  /**
   * Retrieve virtual file from data context, by checking also current editor (in diff dialog)
   */
  @Nullable
  public static VirtualFile getVirtualFile(AnActionEvent e)
  {
    VirtualFile result = e.getData(DataKeys.VIRTUAL_FILE);
    if (result == null)
    {
      Editor editor = e.getData(DataKeys.EDITOR);
      if (editor != null)
      {
        result = FileDocumentManager.getInstance().getFile(editor.getDocument());
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

  public static boolean hasRole(@NotNull Review review, @NotNull User.Role role)
  {
    String login = RevuUtils.getCurrentUserLogin();
    if (login == null)
    {
      return false;
    }

    User user = review.getDataReferential().getUser(login, true);
    return (user != null) && user.hasRole(role);
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
        UIUtil.setEnabled(component, canWrite, true);
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
  public static String buildReviewStatusLabel(@NotNull ReviewStatus status, boolean lowerCase)
  {
    String label = RevuBundle.message("general.reviewStatus." + status.toString().toLowerCase() + ".text");
    return lowerCase ? label.toLowerCase() : label;
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

  @NotNull
  public static RangeMarker createRangeMarker(@NotNull Issue issue, @NotNull Document document)
  {
    int lineStart = (issue.getLineStart() == -1) ? 0 : issue.getLineStart();
    int lineEnd = (issue.getLineEnd() == -1) ? 0 : issue.getLineEnd();
    final int lineCount = document.getLineCount();
    lineStart = lineStart < lineCount ? lineStart : lineCount - 1;
    lineEnd = lineEnd < lineCount ? lineEnd : lineCount - 1;
    return document.createRangeMarker(document.getLineStartOffset(lineStart), document.getLineStartOffset(lineEnd));
  }

  public static boolean isActive(@NotNull Review review)
  {
    return ((review.getStatus() == ReviewStatus.FIXING) || (review.getStatus() == ReviewStatus.REVIEWING));
  }

  public static boolean isActiveForCurrentUser(@NotNull Review review)
  {
    return (((review.getStatus() == ReviewStatus.FIXING) || (review.getStatus() == ReviewStatus.REVIEWING))
      && (review.getDataReferential().getUser(getCurrentUserLogin(), true) != null));
  }

  @NotNull
  public static String getHex(@NotNull Color color)
  {
    return "#" + Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
  }

  public static Color getIssueStatusColor(@NotNull IssueStatus status)
  {
    // @TODO manage Colors in cache (already done in IssueTable)
    RevuAppSettings appSettings = ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .getState();
    return Color.decode(appSettings.getIssueStatusColors().get(status));
  }

  @Nullable
  public static Review getReviewingReview(@NotNull Project project)
  {
    String reviewName = getWorkspaceSettings(project).getReviewingReviewName();
    if (reviewName == null)
    {
      return null;
    }

    Review review = project.getComponent(ReviewManager.class).getReviewByName(reviewName);
    User user = RevuUtils.getCurrentUser(review);

    return ((user != null) && (user.hasRole(User.Role.REVIEWER))) ? review : null;
  }

  public static boolean hasRoleForReviewingReview(@NotNull Project project, @NotNull User.Role role)
  {
    Review review = RevuUtils.getReviewingReview(project);

    if (review != null)
    {
      User user = RevuUtils.getCurrentUser(review);
      if ((user != null) && (user.hasRole(role)))
      {
        return true;
      }
    }

    return false;
  }

  @NotNull
  public static Collection<Review> getActiveReviewsForCurrentUser(@NotNull Project project)
  {
    return project.getComponent(ReviewManager.class).getReviews(RevuUtils.getCurrentUserLogin(), true);
  }

  @NotNull
  public static RevuAppSettings getAppSettings()
  {
    return ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class).getState();
  }

  @NotNull
  public static RevuWorkspaceSettings getWorkspaceSettings(@NotNull Project project)
  {
    return project.getComponent(RevuWorkspaceSettingsComponent.class).getState();
  }

  @NotNull
  public static RevuProjectSettings getProjectSettings(@NotNull Project project)
  {
    return project.getComponent(RevuProjectSettingsComponent.class).getState();
  }

  public static void editProjectSettings(@NotNull Project project, @Nullable final Review review)
  {
    final RevuProjectSettingsForm form = project.getComponent(RevuProjectSettingsForm.class);
    ShowSettingsUtil.getInstance().editConfigurable(project, form, new Runnable()
    {
      public void run()
      {
        if (review != null)
        {
          form.selectItem(review);
        }
      }
    });
  }

  public static void editAppSettings(@Nullable Project project)
  {
    ShowSettingsUtil.getInstance().editConfigurable(project,
      ApplicationManager.getApplication().getComponent(RevuAppSettingsForm.class));
  }

  @NotNull
  public static Icon findIcon(@NotNull Collection<Issue> issues, boolean fullySynchronized)
  {
    boolean allResolved = true;
    for (Issue issue : issues)
    {
      if ((!issue.getStatus().equals(IssueStatus.RESOLVED)) && (!issue.getStatus().equals(IssueStatus.CLOSED)))
      {
        allResolved = false;
      }
    }

    return RevuIconProvider.getIcon(fullySynchronized
      ? (allResolved ? RevuIconProvider.IconRef.GUTTER_ISSUES_RESOLVED
        : RevuIconProvider.IconRef.GUTTER_ISSUES)
      : (allResolved ? RevuIconProvider.IconRef.GUTTER_ISSUES_DESYNCHRONIZED
        : RevuIconProvider.IconRef.GUTTER_ISSUES_DESYNCHRONIZED_RESOLVED));
  }

  @NotNull
  public static Icon findIcon(@NotNull Issue issue, boolean fullySynchronized)
  {
    IssueStatus status = issue.getStatus();

    boolean resolved = (status.equals(IssueStatus.RESOLVED) || status.equals(IssueStatus.CLOSED));
    return RevuIconProvider.getIcon(fullySynchronized
      ? (resolved ? RevuIconProvider.IconRef.GUTTER_ISSUE_RESOLVED
        : RevuIconProvider.IconRef.GUTTER_ISSUE)
      : (resolved ? RevuIconProvider.IconRef.GUTTER_ISSUE_DESYNCHRONIZED
        : RevuIconProvider.IconRef.GUTTER_ISSUE_DESYNCHRONIZED_RESOLVED));
  }
}
