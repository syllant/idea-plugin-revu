package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.vfs.VcsVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuFriendlyException;
import org.sylfra.idea.plugins.revu.RevuPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class VcsFetcher implements ApplicationComponent
{
  private final static Logger LOGGER = Logger.getInstance(VcsFetcher.class.getName());

  private final Map<CacheKey, VirtualFile> cache;

  public VcsFetcher()
  {
    cache = Collections.synchronizedMap(new HashMap<CacheKey, VirtualFile>());
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
    cache.clear();
  }

  @Nullable
  public void fetch(@NotNull Project project, @NotNull VirtualFile vFile, @NotNull String rev,
    @NotNull final IVcsFetchListener fetchListener)
  {
    VirtualFile result;
    final CacheKey key = new CacheKey(vFile, rev);

    synchronized (cache)
    {
      result = cache.get(key);
      if (result == null)
      {
        IVcsFetchListener listenerWrapper = new IVcsFetchListener()
        {
          public void fetchSucceeded(@NotNull VirtualFile vFile)
          {
            fetchListener.fetchSucceeded(vFile);
            cache.put(key, vFile);
          }

          public void fetchFailed(@NotNull RevuFriendlyException exception)
          {
            fetchListener.fetchFailed(exception);
          }
        };
        new FetchingFileTask(project, vFile, rev, listenerWrapper).queue();
      }
      else
      {
        fetchListener.fetchSucceeded(vFile);
      }
    }
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".VcsFetcher";
  }

  public static interface IVcsFetchListener
  {
    void fetchSucceeded(@NotNull VirtualFile vFile);
    void fetchFailed(@NotNull RevuFriendlyException exception);
  }

  private static final class CacheKey
  {
    private VirtualFile vFile;
    private String rev;

    private CacheKey(VirtualFile vFile, String rev)
    {
      this.vFile = vFile;
      this.rev = rev;
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o)
      {
        return true;
      }
      if (o == null || getClass() != o.getClass())
      {
        return false;
      }

      CacheKey cacheKey = (CacheKey) o;

      return !((!rev.equals(cacheKey.rev)) || (!vFile.equals(cacheKey.vFile)));
    }

    @Override
    public int hashCode()
    {
      return (31 * vFile.hashCode()) + rev.hashCode();
    }
  }

  private static class FetchingFileTask extends Task.Backgroundable
  {
    private final Project project;
    private final VirtualFile virtualFile;
    private final String revision;
    private final IVcsFetchListener fetchListener;

    public FetchingFileTask(Project project, VirtualFile vFile, String revision, IVcsFetchListener fetchListener)
    {
      super(project, RevuBundle.message("task.fetchingVcsFile.text", 
        RevuVcsUtils.formatVcsFileRevision(vFile, revision)), false);

      this.project = project;
      this.virtualFile = vFile;
      this.revision = revision;
      this.fetchListener = fetchListener;
    }

    @Override
    public boolean shouldStartInBackground()
    {
      return false;
    }

    @Override
    public void run(ProgressIndicator indicator)
    {
      indicator.setIndeterminate(true);
      VirtualFile result;
      try
      {
        result = fetchRevision(project, virtualFile, revision);
      }
      catch (RevuFriendlyException e)
      {
        fetchListener.fetchFailed(e);
        return;
      }

      fetchListener.fetchSucceeded(result);
    }

    @NotNull
    public VirtualFile fetchRevision(@NotNull Project project, @NotNull VirtualFile vFile, @NotNull String rev)
      throws RevuFriendlyException
    {
      DiffProvider diffProvider = RevuVcsUtils.getDiffProvider(project, vFile);
      if (diffProvider == null)
      {
        throw new RevuFriendlyException("Failed to get DiffProvider: " + vFile,
          RevuBundle.message("friendlyError.failedToFetchVcsFile.noVcs.details.text",
          vFile.getPath()));
      }

      AbstractVcs vcs = VcsUtil.getVcsFor(project, vFile);
      assert vcs != null;

      VcsRevisionNumber vcsRevisionNumber = vcs.parseRevisionNumber(rev);
      if (vcsRevisionNumber == null)
      {
        throw new RevuFriendlyException("Failed to parse VCS revision number: " + rev,
          RevuBundle.message("friendlyError.failedToFetchVcsFile.invalidRevision.details.text",
          vFile.getPath(), rev));
      }

      ContentRevision contentRevision = diffProvider.createFileContent(vcsRevisionNumber, vFile);
      String content = null;
      if (contentRevision != null)
      {
        try
        {
          if (LOGGER.isDebugEnabled())
          {
            LOGGER.debug("Fetching from VCS:" + contentRevision);
          }
          content = contentRevision.getContent();
        }
        catch (VcsException e)
        {
          throw new RevuFriendlyException("Error while retrieving VCS content: " + contentRevision,
            null, RevuBundle.message("friendlyError.failedToFetchVcsFile.vcsError.details.text",
            RevuBundle.message("general.vcsFileAndRev.text", vFile.getPath(), rev), e.getMessage()), e);
        }
      }

      if (content == null)
      {
        throw new RevuFriendlyException("VCS fetched content is null: "+ contentRevision,
          RevuBundle.message("friendlyError.failedToFetchVcsFile.nullContent.details.text",
          vFile.getPath()));
      }

      return new VcsVirtualFile(contentRevision.getFile().getPath(), content.getBytes(),
        vcsRevisionNumber.asString(), vFile.getFileSystem());
    }
  }
}
