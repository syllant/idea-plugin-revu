package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuUtils
{
  public static
  @NotNull
  String buildRelativePath(@NotNull Project project,
                           @NotNull ReviewItem reviewItem)
  {
    return VfsUtil.getRelativePath(reviewItem.getFile(), project.getBaseDir(), '/');
  }

  public static
  @Nullable
  VirtualFile findFileFromRelativeFile(@NotNull Project project,
                                       @NotNull String filePath)
  {
    VirtualFile baseDir = project.getBaseDir();
    return (baseDir == null) ? null : LocalFileSystem.getInstance().findFileByPath(
      baseDir.getPath() + "/" + filePath);
  }

  public static
  @Nullable
  PsiFile getPsiFile(@NotNull Project project,
                     @NotNull ReviewItem reviewItem)
  {
    return PsiManager.getInstance(project).findFile(reviewItem.getFile());
  }

  public static
  @Nullable
  Document getDocument(@NotNull Project project,
                       @NotNull ReviewItem reviewItem)
  {
    PsiFile psiFile = getPsiFile(project, reviewItem);
    return (psiFile == null) ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
  }
}
