package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVfsUtils
{
  private static final Logger LOGGER = Logger.getInstance(RevuVfsUtils.class.getName());

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull VirtualFile vFile)
  {
    return VfsUtil.getRelativePath(vFile, project.getBaseDir(), '/');
  }

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull File file)
  {
    VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    return (vFile == null) ? buildAbsolutePath(file) : buildRelativePath(project, vFile);
  }

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull String path)
  {
    VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
    return (vFile == null) ? path : buildRelativePath(project, vFile);
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull Project project, @NotNull String path)
  {
    return buildAbsolutePath(findFileFromRelativePath(project, path));
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull String path)
  {
    return buildAbsolutePath(new File(path));
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull File file)
  {
    try
    {
      return file.getCanonicalPath();
    }
    catch (IOException e)
    {
      LOGGER.warn("Can't get canonical path: " + file, e);
      return file.getPath();
    }
  }

  @Nullable
  public static VirtualFile findVFileFromRelativeFile(@NotNull Project project, @NotNull String filePath)
  {
    VirtualFile baseDir = project.getBaseDir();
    return (baseDir == null) ? null : LocalFileSystem.getInstance().findFileByPath(
      baseDir.getPath() + "/" + filePath);
  }

  @NotNull
  public static File findFileFromRelativePath(@NotNull Project project, @NotNull String filePath)
  {
    VirtualFile baseDir = project.getBaseDir();
    return new File(baseDir.getPath(), filePath);
  }

  @Nullable
  public static VirtualFile findFile(@NotNull String filePath)
  {
    return LocalFileSystem.getInstance().findFileByPath(filePath);
  }
}
