package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewFileChooser
{
  private final Project project;
  private ReviewFileChooserDescriptor descriptor;
  @Nullable
  private VirtualFile defaultFile;

  public ReviewFileChooser(@NotNull Project project)
  {
    this.project = project;
    descriptor = new ReviewFileChooserDescriptor();
    defaultFile = project.getBaseDir();
  }

  public ReviewFileChooserDescriptor getDescriptor()
  {
    return descriptor;
  }

  public VirtualFile selectFileToSave(@Nullable VirtualFile defaultFile)
  {
    return selectFile(defaultFile, false);
  }

  public VirtualFile selectFileToOpen(@Nullable VirtualFile defaultFile)
  {
    return selectFile(defaultFile, true);
  }

  private VirtualFile selectFile(@Nullable VirtualFile defaultFile, boolean openMode)
  {
    if (defaultFile != null)
    {
      this.defaultFile = defaultFile;
    }

    descriptor.setOpenMode(openMode);
    VirtualFile[] virtualFiles = FileChooser.chooseFiles(project, descriptor, this.defaultFile);

    if (virtualFiles.length > 0)
    {
      this.defaultFile = virtualFiles[0];
      return this.defaultFile;
    }

    return null;
  }

  /**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
   * @version $Id$
   */
  private static class ReviewFileChooserDescriptor extends FileChooserDescriptor
  {
    private boolean openMode;

    public ReviewFileChooserDescriptor()
    {
      super(true, true, false, false, false, false);
      setDescription(RevuBundle.message("general.fileChooser.description.text"));
    }

    public void setOpenMode(boolean openMode)
    {
      this.openMode = openMode;
      setTitle(RevuBundle.message(openMode ? "general.fileChooser.open.title" : "general.fileChooser.save.title"));
    }

    @Override
    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles)
    {
      return ((!FileElement.isFileHidden(file))
        && ((file.isDirectory()) || ("xml".equals(file.getExtension()))));
    }

    @Override
    public boolean isFileSelectable(VirtualFile file)
    {
      return (((!openMode) && (file.isDirectory())) || ("xml".equals(file.getExtension())));
    }
  }
}
