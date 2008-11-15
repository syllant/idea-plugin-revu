package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuUtils
{
  @NotNull
  public static String buildRelativePath(@NotNull Project project,
    @NotNull ReviewItem reviewItem)
  {
    return VfsUtil.getRelativePath(reviewItem.getFile(), project.getBaseDir(), '/');
  }

  @Nullable
  public static VirtualFile findFileFromRelativeFile(@NotNull Project project,
    @NotNull String filePath)
  {
    VirtualFile baseDir = project.getBaseDir();
    return (baseDir == null) ? null : LocalFileSystem.getInstance().findFileByPath(
      baseDir.getPath() + "/" + filePath);
  }

  @Nullable
  public static PsiFile getPsiFile(@NotNull Project project,
    @NotNull ReviewItem reviewItem)
  {
    return PsiManager.getInstance(project).findFile(reviewItem.getFile());
  }

  @Nullable
  public static Document getDocument(@NotNull Project project,
    @NotNull ReviewItem reviewItem)
  {
    PsiFile psiFile = getPsiFile(project, reviewItem);
    return (psiFile == null) ? null : PsiDocumentManager.getInstance(project).getDocument(psiFile);
  }

  @Nullable
  public static Editor getEditor(@NotNull ReviewItem reviewItem)
  {
    Editor[] editors = EditorFactory.getInstance().getAllEditors();
    for (Editor editor : editors)
    {
      VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (reviewItem.getFile().equals(vFile))
      {
        return editor;
      }
    }

    return null;
  }

  @NotNull
  public static String z(@Nullable String s)
  {
    if ((s == null) || ("".equals(s)))
    {
      return "";
    }
    
    return DigestUtils.md5Hex(s + RevuPlugin.PLUGIN_NAME);
  }
}
