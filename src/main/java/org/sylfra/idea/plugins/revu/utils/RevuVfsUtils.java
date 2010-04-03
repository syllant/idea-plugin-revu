package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVfsUtils
{
  private static final Logger LOGGER = Logger.getInstance(RevuVfsUtils.class.getName());

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull File file)
  {
    if (project.getBaseDir() == null)
    {
      return file.getPath();
    }

    // Hum, VfsUtil#getPath() doesn't work as I expect (or is bugged ?)
//    String path = VfsUtil.getPath(project.getBaseDir(), vFile, '/');
    String result = FileUtil.getRelativePath(new File(project.getBaseDir().getPath()), file);
    if (result == null)
    {
      return file.getPath();
    }

    result = result.replace('\\', '/');
    return result;
  }

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull VirtualFile vFile)
  {
    return buildRelativePath(project, new File(vFile.getPath()));
  }

  @NotNull
  public static String buildRelativePath(@NotNull Project project, @NotNull String path)
  {
    return buildRelativePath(project, new File(path));
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull Project project, @NotNull String path)
  {
    return buildAbsolutePath(findFileFromRelativePath(project, path));
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull File file)
  {
    return buildAbsolutePath(file.getAbsolutePath());
  }

  @NotNull
  public static String buildAbsolutePath(@NotNull String path)
  {
    return path.replace('\\', '/');
  }

  @NotNull
  public static String buildPresentablePath(@NotNull String path)
  {
    return buildPresentablePath(new File(path));
  }

  @NotNull
  public static String buildPresentablePath(@NotNull File file)
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
    File f = new File(filePath);
    if (f.isAbsolute())
    {
      return LocalFileSystem.getInstance().findFileByIoFile(f);
    }

    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null)
    {
      LOGGER.warn("Can't get project base dir to compute relative path: " + f + ", project:" + project.getName());
      return null;
    }

    return LocalFileSystem.getInstance().findFileByPath(baseDir.getPath() + "/" + filePath);
  }

  @NotNull
  public static File findFileFromRelativePath(@NotNull Project project, @NotNull String filePath)
  {
    File f = new File(filePath);
    if (f.isAbsolute())
    {
      return f;
    }

    VirtualFile baseDir = project.getBaseDir();
    if (baseDir == null)
    {
      LOGGER.warn("Can't get project base dir to compute relative path: " + f + ", project:" + project.getName());
      return f;
    }

    return new File(baseDir.getPath(), filePath);
  }

  @Nullable
  public static VirtualFile findFile(@NotNull String filePath)
  {
    return LocalFileSystem.getInstance().findFileByPath(filePath);
  }
}
