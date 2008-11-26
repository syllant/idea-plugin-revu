package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.config.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManager implements ProjectComponent
{
  private static final Logger LOGGER = Logger.getInstance(ReviewManager.class.getName());

  private Project project;
  private List<Review> reviews;
  private Map<File, Review> reviewsByFile;
  private final List<IReviewListener> reviewListeners;
  private final List<IReviewExternalizationListener> reviewExternalizationListeners;

  public ReviewManager(Project project)
  {
    this.project = project;
    reviews = new ArrayList<Review>();
    reviewsByFile = new HashMap<File, Review>();
    reviewListeners = new LinkedList<IReviewListener>();
    reviewExternalizationListeners = new LinkedList<IReviewExternalizationListener>();

    installSettingsListeners();
  }

  private void installSettingsListeners()
  {
    project.getComponent(RevuProjectSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuProjectSettings>()
      {
        public void settingsChanged(RevuProjectSettings settings)
        {
          load(settings.getReviewFiles(), true);
        }
      });
    project.getComponent(RevuWorkspaceSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuWorkspaceSettings>()
      {
        public void settingsChanged(RevuWorkspaceSettings settings)
        {
          load(settings.getReviewFiles(), false);
        }
      });
  }

  public
  @Nullable
  Review getReview(@NotNull File file)
  {
    return reviewsByFile.get(file);
  }

  public
  @NotNull
  List<Review> getReviews()
  {
    return Collections.unmodifiableList(reviews);
  }

  public
  @NotNull
  List<Review> getReviews(@Nullable Boolean active)
  {
    return getReviews(active, null);
  }

  public
  @NotNull
  List<Review> getReviews(@Nullable Boolean active, @Nullable String userLogin)
  {
    List<Review> result = new ArrayList<Review>();
    for (Review review : reviews)
    {
      if (((active == null) || (review.isActive() == active))
        && ((userLogin == null) || (review.getDataReferential().getUser(userLogin) != null)))
      {
        result.add(review);
      }
    }

    return Collections.unmodifiableList(result);
  }

  public void projectOpened()
  {
    LocalFileSystem.getInstance().addVirtualFileListener(new RevuVirtualFileListener(project));
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable()
    {
      public void run()
      {
        load(project.getComponent(RevuProjectSettingsComponent.class).getState().getReviewFiles(), true);
        load(project.getComponent(RevuWorkspaceSettingsComponent.class).getState().getReviewFiles(), false);
      }
    });
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".ReviewManager";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }

  public void addReviewListener(@NotNull IReviewListener listener)
  {
    reviewListeners.add(listener);
  }

  public void addReviewExternalizationListener(@NotNull IReviewExternalizationListener listener)
  {
    reviewExternalizationListeners.add(listener);
  }

  public void addReview(@NotNull Review review)
  {
    reviews.add(review);
    reviewsByFile.put(review.getFile(), review);

    for (IReviewListener listener : reviewListeners)
    {
      listener.reviewAdded(review);
    }
  }

  public boolean reload(@NotNull final Review review, boolean consolidate)
  {
    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    InputStream inputStream = null;
    Exception exception = null;
    try
    {
      // @TODO report error to user instead of only logging
      inputStream = new FileInputStream(review.getFile());
      reviewExternalizer.load(review, inputStream);

      if (consolidate)
      {
        consolidateReview(review);
      }

      for (IReviewExternalizationListener listener : reviewExternalizationListeners)
      {
        listener.loadSucceeded(review);
      }

      for (IReviewListener listener : reviewListeners)
      {
        listener.reviewAdded(review);
      }
    }
    catch (IOException e)
    {
      exception = e;
      LOGGER.warn("IO error while loading review file: " + review.getFile(), e);
    }
    catch (RevuException e)
    {
      exception = e;
      LOGGER.warn("Failed to load review file: " + review.getFile(), e);
    }
    finally
    {
      try
      {
        if (inputStream != null)
        {
          inputStream.close();
        }
      }
      catch (IOException e)
      {
        LOGGER.warn("Failed to close release review file: " + review.getFile(), e);
      }
    }

    if (exception != null)
    {
      for (IReviewExternalizationListener listener : reviewExternalizationListeners)
      {
        listener.loadFailed(review.getFile(), exception);
      }

      return false;
    }

    return true;
  }

  private void load(@NotNull List<String> filePaths, boolean sharedFilter)
  {
    // Delete obsolete reviews
    // currentReviewFiles stores paths still there. Used to check new path for second step
    Set<String> currentReviewFilePaths = new HashSet<String>(reviews.size());
    for (Iterator<Review> it = reviews.iterator(); it.hasNext();)
    {
      Review review = it.next();
      if (review.isShared() != sharedFilter)
      {
        continue;
      }

      String filePath = RevuUtils.buildCanonicalPath(review.getFile());
      if (filePaths.contains(filePath))
      {
        currentReviewFilePaths.add(filePath);
        for (IReviewListener listener : reviewListeners)
        {
          listener.reviewChanged(review);
        }
      }
      else
      {
        it.remove();
        reviewsByFile.remove(review.getFile());

        for (IReviewListener listener : reviewListeners)
        {
          listener.reviewDeleted(review);
        }
      }
    }

    // Add new reviews
    List<Review> addedReviews = new ArrayList<Review>();
    for (String filePath : filePaths)
    {
      if (!currentReviewFilePaths.contains(filePath))
      {
        Review review = new Review();
        review.setFile(new File(filePath));
        reload(review, false);
        addedReviews.add(review);

        if (review.isShared() != sharedFilter)
        {
          continue;
        }

        addReview(review);
      }
    }

    for (Review review : addedReviews)
    {
      try
      {
        consolidateReview(review);
      }
      catch (RevuException e)
      {
        LOGGER.warn("Failed to consolidate review file: " + review.getFile(), e);
        for (IReviewExternalizationListener listener : reviewExternalizationListeners)
        {
          listener.loadFailed(review.getFile(), e);
        }
      }
    }
  }

  private void consolidateReview(Review review) throws RevuException
  {
    File linkedFile = review.getDataReferential().getLinkedFile();
    if (linkedFile != null)
    {
      Review linkedReview = reviewsByFile.get(linkedFile);
      if (linkedReview == null)
      {
        throw new RevuException(
          RevuBundle.message("externalizing.load.linkNotFound.error.text", linkedFile.getPath()));
      }
      else
      {
        review.getDataReferential().setWrappedReferential(linkedReview.getDataReferential());
      }
    }
  }

  public void save()
  {
    IReviewExternalizer reviewExternalizer =
      project.getComponent(IReviewExternalizer.class);

    for (Review review : reviews)
    {
      Exception exception = null;
      OutputStream out = null;
      try
      {
        out = new FileOutputStream(review.getFile());
        reviewExternalizer.save(review, out);

        for (IReviewExternalizationListener listener : reviewExternalizationListeners)
        {
          listener.saveSucceeded(review);
        }
      }
      catch (IOException e)
      {
        LOGGER.warn("IO error while writing review file: " + review.getFile(), e);
        exception = e;
      }
      catch (RevuException e)
      {
        LOGGER.warn("Failed to save review file : " + review.getFile(), e);
        exception = e;
      }
      finally
      {
        try
        {
          if (out != null)
          {
            out.close();
          }
        }
        catch (IOException e)
        {
          LOGGER.warn("Failed to close release review file: " + review.getFile(), e);
        }
      }

      if (exception != null)
      {
        for (IReviewExternalizationListener listener : reviewExternalizationListeners)
        {
          listener.saveFailed(review, exception);
        }
      }
    }
  }

}
