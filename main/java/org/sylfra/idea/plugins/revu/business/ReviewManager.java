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
import org.sylfra.idea.plugins.revu.config.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManager implements ProjectComponent
{
  private static final Logger LOGGER = Logger.getInstance(ReviewManager.class.getName());
  private static final String DEFAULT_REVIEW_TEMPLATE_PATH = "[default]";
  private static final String DEFAULT_REVIEW_TEMPLATE_RESOURCE_PATH
    = "/org/sylfra/idea/plugins/revu/resources/defaultReviewTemplate.xml";

  private Project project;
  private List<Review> reviews;
  private Map<String, Review> reviewsByPaths;
  private Map<String, Review> reviewsByTitles;
  private final List<IReviewListener> reviewListeners;
  private final List<IReviewExternalizationListener> reviewExternalizationListeners;

  public ReviewManager(Project project)
  {
    this.project = project;
    reviews = new ArrayList<Review>();
    reviewsByPaths = new HashMap<String, Review>();
    reviewsByTitles = new HashMap<String, Review>();
    reviewListeners = new ArrayList<IReviewListener>();
    reviewExternalizationListeners = new ArrayList<IReviewExternalizationListener>();

    installSettingsListeners();
  }

  private void installSettingsListeners()
  {
    project.getComponent(RevuProjectSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuProjectSettings>()
      {
        public void settingsChanged(RevuProjectSettings settings)
        {
          loadAndAdd(settings.getReviewFiles(), true);
        }
      });
    project.getComponent(RevuWorkspaceSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuWorkspaceSettings>()
      {
        public void settingsChanged(RevuWorkspaceSettings settings)
        {
          loadAndAdd(settings.getReviewFiles(), false);
        }
      });
  }

  @Nullable
  public Review getReview(@NotNull String path)
  {
    return reviewsByPaths.get(path);
  }

  @NotNull
  public List<Review> getReviews()
  {
    return Collections.unmodifiableList(reviews);
  }

  @NotNull
  public List<Review> getReviews(@Nullable Boolean active, Boolean template)
  {
    return getReviews(active, template, null);
  }

  @NotNull
  public List<Review> getReviews(@Nullable Boolean active, Boolean template, @Nullable String userLogin)
  {
    List<Review> result = new ArrayList<Review>();
    for (Review review : reviews)
    {
      if (((active == null) || (review.isActive() == active))
        && ((template == null) || (review.isTemplate() == template))
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
        loadAndAdd(Arrays.asList(DEFAULT_REVIEW_TEMPLATE_PATH), true);
        loadAndAdd(project.getComponent(RevuProjectSettingsComponent.class).getState().getReviewFiles(), true);
        loadAndAdd(project.getComponent(RevuWorkspaceSettingsComponent.class).getState().getReviewFiles(), false);
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
    reviewsByPaths.put(review.getPath(), review);
    reviewsByTitles.put(review.getTitle(), review);

    fireReviewAdded(review);
  }

  @Nullable
  public Review load(@NotNull String path, boolean consolidate)
  {
    Review review = new Review();
    review.setPath(path);
    return load(review, consolidate) ? review : null;
  }

  public boolean load(@NotNull final Review review, boolean consolidate)
  {
    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    InputStream inputStream = null;
    Exception exception = null;
    String path = review.getPath();
    try
    {
      inputStream = getInputStreamFromPath(path);
      reviewExternalizer.load(review, inputStream);

      if (consolidate)
      {
        consolidateReview(review);
      }

      fireReviewLoadSucceeded(review);
    }
    catch (IOException e)
    {
      exception = e;
      LOGGER.warn("IO error while loading review file: " + path, e);
    }
    catch (RevuException e)
    {
      exception = e;
      LOGGER.warn("Failed to load review file: " + path, e);
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
        LOGGER.warn("Failed to close release review file: " + path, e);
      }
    }

    if (exception != null)
    {
      fireReviewLoadFailed(exception, path);

      return false;
    }

    return true;
  }

  @Nullable
  private InputStream getInputStreamFromPath(@NotNull String path) throws IOException
  {
    if (DEFAULT_REVIEW_TEMPLATE_PATH.equals(path))
    {
      return getClass().getClassLoader().getResourceAsStream(DEFAULT_REVIEW_TEMPLATE_RESOURCE_PATH);
    }

    return new FileInputStream(path);
  }

  private void loadAndAdd(@NotNull List<String> filePaths, boolean sharedFilter)
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

      String filePath = RevuUtils.buildRelativePath(project, review.getPath());
      if (filePaths.contains(filePath))
      {
        currentReviewFilePaths.add(filePath);
        fireReviewChanged(review);
      }
      else
      {
        if (!review.getPath().equals(DEFAULT_REVIEW_TEMPLATE_PATH))
        {
          it.remove();
          reviewsByPaths.remove(review.getPath());
          reviewsByTitles.remove(review.getTitle());
          fireReviewDeleted(review);
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
        if (DEFAULT_REVIEW_TEMPLATE_PATH.equals(filePath))
        {
          review.setPath(filePath);
          review.setEmbedded(true);
        }
        else
        {
          review.setPath(RevuUtils.buildAbsolutePath(project, filePath));
        }
        load(review, false);
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
        LOGGER.warn("Failed to consolidate review file: " + review.getPath(), e);
        fireReviewLoadFailed(e, review.getPath());
      }
    }
  }

  private void consolidateReview(Review review) throws RevuException
  {
    Review extendedReview = review.getExtendedReview();
    if (extendedReview != null)
    {
      String title = extendedReview.getTitle();
      extendedReview = reviewsByTitles.get(title);
      if (extendedReview == null)
      {
        throw new RevuException(
          RevuBundle.message("friendlyError.externalizing.load.extendedNotFound.error.text", title));
      }

      checkCyclicLink(review, extendedReview);
      review.setExtendedReview(extendedReview);
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
        out = new FileOutputStream(review.getPath());
        reviewExternalizer.save(review, out);

        fireReviewSaveSucceeded(review);
      }
      catch (IOException e)
      {
        LOGGER.warn("IO error while writing review file: " + review.getPath(), e);
        exception = e;
      }
      catch (RevuException e)
      {
        LOGGER.warn("Failed to save review file : " + review.getPath(), e);
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
          LOGGER.warn("Failed to close release review file: " + review.getPath(), e);
        }
      }

      if (exception != null)
      {
        fireReviewSavedFailed(review, exception);
      }
    }
  }

  public void checkCyclicLink(@NotNull Review main, @NotNull Review extendedRoot)
    throws RevuException
  {
    checkCyclicLink(main, extendedRoot, extendedRoot);
  }

  private void checkCyclicLink(@NotNull Review main, @NotNull Review extendedRoot, @NotNull Review extendedChild)
    throws RevuException
  {
    if (main.getTitle().equals(extendedChild.getTitle()))
    {
      throw new RevuException(
        RevuBundle.message("friendlyError.externalizing.cyclicReview.details.text",
          main.getPath(), main.getTitle(),
          extendedRoot.getPath(), extendedRoot.getTitle()));
    }

    Review extendedChild2;
    while ((extendedChild2 = extendedChild.getExtendedReview()) != null)
    {
      checkCyclicLink(main, extendedRoot, extendedChild2);
    }
  }

  private void fireReviewAdded(Review review)
  {
    List<IReviewListener> copy = new ArrayList<IReviewListener>(reviewListeners);
    for (IReviewListener listener : copy)
    {
      listener.reviewAdded(review);
    }
  }

  private void fireReviewChanged(Review review)
  {
    List<IReviewListener> copy = new ArrayList<IReviewListener>(reviewListeners);
    for (IReviewListener listener : copy)
    {
      listener.reviewChanged(review);
    }
  }

  private void fireReviewDeleted(Review review)
  {
    List<IReviewListener> copy = new ArrayList<IReviewListener>(reviewListeners);
    for (IReviewListener listener : copy)
    {
      listener.reviewDeleted(review);
    }
  }

  private void fireReviewLoadFailed(Exception exception, String path)
  {
    List<IReviewExternalizationListener> copy = new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.loadFailed(path, exception);
    }
  }

  private void fireReviewLoadSucceeded(Review review)
  {
    List<IReviewExternalizationListener> copy = new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.loadSucceeded(review);
    }
  }

  private void fireReviewSaveSucceeded(Review review)
  {
    List<IReviewExternalizationListener> copy = new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.saveSucceeded(review);
    }
  }

  private void fireReviewSavedFailed(Review review, Exception exception)
  {
    List<IReviewExternalizationListener> copy = new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.saveFailed(review, exception);
    }
  }
}
