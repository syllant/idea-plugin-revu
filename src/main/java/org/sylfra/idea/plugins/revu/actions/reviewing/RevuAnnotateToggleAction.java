package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.ActiveAnnotationGutter;
import com.intellij.openapi.vcs.actions.AnnotationGutterLineConvertorProxy;
import com.intellij.openapi.vcs.actions.VcsContext;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.changes.BackgroundFromStartOption;
import com.intellij.openapi.vcs.impl.BackgroundableActionEnabledHandler;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vcs.impl.VcsBackgroundableActions;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuAnnotateToggleAction extends ToggleAction
{
  private static final Logger LOGGER = Logger.getInstance(RevuAnnotateToggleAction.class.getName());

  static final Key<Collection<ActiveAnnotationGutter>> KEY_IN_EDITOR = Key.create("RevuAnnotations");
  private FileScopeManager fileScopeManager;

  public RevuAnnotateToggleAction()
  {
    fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);
  }

  public void update(AnActionEvent e)
  {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    Review review = RevuUtils.getReviewingReview(project);
    if (review == null)
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.annotate.template.text"));
      e.getPresentation().setEnabled(false);
    }
    else
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.annotate.review.text", review.getName()));

      VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
      e.getPresentation().setEnabled((vFile != null)
        && RevuVcsUtils.isUnderVcs(project, vFile)
        && fileScopeManager.belongsToScope(project, review, vFile));
    }
  }

  public boolean isSelected(AnActionEvent e)
  {
    VcsContext context = VcsContextFactory.SERVICE.getInstance().createContextOn(e);
    Editor editor = context.getEditor();
    if (editor == null)
    {
      return false;
    }

    Collection annotations = editor.getUserData(KEY_IN_EDITOR);
    return annotations != null && !annotations.isEmpty();
  }

  public void setSelected(AnActionEvent e, boolean state)
  {
    VcsContext context = VcsContextFactory.SERVICE.getInstance().createContextOn(e);
    Editor editor = context.getEditor();
    if (!state)
    {
      if (editor != null)
      {
        editor.getGutter().closeAllAnnotations();
      }
    }
    else
    {
      if (editor == null)
      {
        VirtualFile selectedFile = context.getSelectedFile();
        if (selectedFile == null)
        {
          return;
        }

        FileEditor[] fileEditors = FileEditorManager.getInstance(context.getProject()).openFile(selectedFile, false);
        for (FileEditor fileEditor : fileEditors)
        {
          if (fileEditor instanceof TextEditor)
          {
            editor = ((TextEditor) fileEditor).getEditor();
          }
        }
      }

      LOGGER.assertTrue(editor != null);

      doAnnotate(editor, context.getProject());
    }
  }

  private static void doAnnotate(final Editor editor, final Project project)
  {
    if (project == null)
    {
      return;
    }

    final VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (file == null)
    {
      return;
    }

    final ProjectLevelVcsManager plVcsManager = ProjectLevelVcsManager.getInstance(project);
    AbstractVcs vcs = plVcsManager.getVcsFor(file);
    if (vcs == null)
    {
      return;
    }

    final AnnotationProvider annotationProvider = vcs.getAnnotationProvider();
    if (annotationProvider == null)
    {
      return;
    }

    final Ref<FileAnnotation> fileAnnotationRef = new Ref<FileAnnotation>();
    final Ref<VcsException> exceptionRef = new Ref<VcsException>();

    final BackgroundableActionEnabledHandler handler =
      ((ProjectLevelVcsManagerImpl) plVcsManager).getBackgroundableActionHandler(
        VcsBackgroundableActions.ANNOTATE);
    handler.register(file.getPath());

    ProgressManager.getInstance()
      .run(new Task.Backgroundable(project, VcsBundle.message("retrieving.annotations"), true,
        BackgroundFromStartOption.getInstance())
      {
        public void run(@NotNull ProgressIndicator indicator)
        {
          try
          {
            fileAnnotationRef.set(annotationProvider.annotate(file));
          }
          catch (VcsException e)
          {
            exceptionRef.set(e);
          }
        }

        @Override
        public void onCancel()
        {
          onSuccess();
        }

        @Override
        public void onSuccess()
        {
          handler.completed(file.getPath());

          if (!exceptionRef.isNull())
          {
            AbstractVcsHelper.getInstance(project)
              .showErrors(Arrays.asList(exceptionRef.get()), VcsBundle.message("message.title.annotate"));
          }
          if (fileAnnotationRef.isNull())
          {
            return;
          }

          doAnnotate(editor, project, fileAnnotationRef.get());
        }
      });
  }

  public static void doAnnotate(final Editor editor, final Project project, final FileAnnotation fileAnnotation)
  {
    final UpToDateLineNumberProvider getUpToDateLineNumber = new UpToDateLineNumberProviderImpl(editor.getDocument(),
      project);

    editor.getGutter().closeAllAnnotations();

    // be careful, not proxies but original items are put there (since only their presence not behaviour is important)
    Collection<ActiveAnnotationGutter> annotations = editor.getUserData(KEY_IN_EDITOR);
    if (annotations == null)
    {
      annotations = new HashSet<ActiveAnnotationGutter>();
      editor.putUserData(KEY_IN_EDITOR, annotations);
    }

    final RevuAnnotationFieldGutter gutter = new RevuAnnotationFieldGutter(fileAnnotation, editor);
    final AnnotationGutterLineConvertorProxy proxy =
      new AnnotationGutterLineConvertorProxy(getUpToDateLineNumber, gutter);
    editor.getGutter().registerTextAnnotation(proxy, proxy);
    annotations.add(gutter);
  }
}
