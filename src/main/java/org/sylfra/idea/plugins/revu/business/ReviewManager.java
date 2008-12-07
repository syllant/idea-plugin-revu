package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
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
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManager implements ProjectComponent
{
  private static final Logger LOGGER = Logger.getInstance(ReviewManager.class.getName());
  private static final Map<String, String> EMBEDDED_REVIEWS = new HashMap<String,  String>(1);
  static
  {
    EMBEDDED_REVIEWS.put("[default]", "/org/sylfra/idea/plugins/revu/resources/defaultReviewTemplate.xml");
  }

  private Project project;
  private List<Review> reviews;
  private Map<String, Review> reviewsByPaths;
  private Map<String, Review> reviewsByTitles;
  private final List<IReviewListener> reviewListeners;
  private final List<IReviewExternalizationListener> reviewExternalizationListeners;
  private IRevuSettingsListener<RevuProjectSettings> projectSettingsListener;
  private IRevuSettingsListener<RevuWorkspaceSettings> workspaceSettingsListener;

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
    projectSettingsListener = new IRevuSettingsListener<RevuProjectSettings>()
    {
      public void settingsChanged(RevuProjectSettings settings)
      {
        loadAndAdd(settings.getReviewFiles(), true);
      }
    };
    workspaceSettingsListener = new IRevuSettingsListener<RevuWorkspaceSettings>()
    {
      public void settingsChanged(RevuWorkspaceSettings settings)
      {
        loadAndAdd(settings.getReviewFiles(), false);
      }
    };
    project.getComponent(RevuProjectSettingsComponent.class).addListener(projectSettingsListener);
    project.getComponent(RevuWorkspaceSettingsComponent.class).addListener(workspaceSettingsListener);
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
    return getReviews(null, active, template, null);
  }

  @NotNull
  public List<Review> getReviews(@Nullable List<Review> customReviews, @Nullable Boolean active, Boolean template,
    @Nullable String userLogin)
  {
    if (customReviews == null)
    {
      customReviews = reviews;
    }

    List<Review> result = new ArrayList<Review>();
    for (Review review : customReviews)
    {
      if (((active == null) || (review.isActive() == active))
        && ((template == null) || (review.isTemplate() == template))
        && ((userLogin == null) || (review.isEmbedded()) || (review.getDataReferential().getUser(userLogin, true) != null)))
      {
        result.add(review);
      }
    }

    return Collections.unmodifiableList(result);
  }

  @NotNull
  public List<Review> getActiveReviews(@NotNull VirtualFile vFile)
  {
    List<Review> result = new ArrayList<Review>();
    for (Review review : reviews)
    {
      if ((review.isActive()) && (!review.isTemplate()) && (review.hasItems(vFile)))
      {
        result.add(review);
      }
    }

    return Collections.unmodifiableList(result);
  }

  public void projectOpened()
  {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable()
    {
      public void run()
      {
        initEmbededReviews();
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
    project.getComponent(RevuProjectSettingsComponent.class).removeListener(projectSettingsListener);
    project.getComponent(RevuWorkspaceSettingsComponent.class).removeListener(workspaceSettingsListener);
  }

  public void addReviewListener(@NotNull IReviewListener listener)
  {
    reviewListeners.add(listener);
  }

  public void removeReviewListener(@NotNull IReviewListener listener)
  {
    reviewListeners.remove(listener);
  }

  public void addReviewExternalizationListener(@NotNull IReviewExternalizationListener listener)
  {
    reviewExternalizationListeners.add(listener);
  }

  public void removeReviewExternalizationListener(@NotNull IReviewExternalizationListener listener)
  {
    reviewExternalizationListeners.remove(listener);
  }

  public void addReview(@NotNull Review review)
  {
    reviews.add(review);
    reviewsByPaths.put(review.getPath(), review);
    reviewsByTitles.put(review.getTitle(), review);
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
    // Embedded review
    String resourcePath = EMBEDDED_REVIEWS.get(path);
    if (resourcePath != null)
    {
      return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    return new FileInputStream(path);
  }

  private void initEmbededReviews()
  {
    reviews.clear();

    for (Map.Entry<String, String> entry : EMBEDDED_REVIEWS.entrySet())
    {
      Review review = new Review();
      review.setPath(entry.getKey());
      review.setEmbedded(true);
      load(review, false);
      addReview(review);
    }

    for (Review review : reviews)
    {
      try
      {
        consolidateReview(review);
      }
      catch (RevuException e)
      {
        LOGGER.warn("Failed to load review file: " + review.getPath(), e);
        fireReviewLoadFailed(e, review.getPath());
      }
    }
  }

  private void loadAndAdd(@NotNull List<String> filePaths, boolean sharedFilter)
  {
    // Delete obsolete reviews
    Map<String, Review> changedReviews = new HashMap<String, Review>();
    for (Iterator<Review> it = reviews.iterator(); it.hasNext();)
    {
      Review review = it.next();
      if ((review.isEmbedded()) || (review.isShared() != sharedFilter))
      {
        continue;
      }

      String filePath = RevuVfsUtils.buildRelativePath(project, review.getPath());
      if (filePaths.contains(filePath))
      {
        changedReviews.put(filePath, review);
      }
      else
      {
        fireReviewDeleted(review);
      }
      reviewsByPaths.remove(review.getPath());
      reviewsByTitles.remove(review.getTitle());
      it.remove();
    }

    // Load remaining reviews and added review
    for (String filePath : filePaths)
    {
      Review review = changedReviews.get(filePath);
      if (review == null)
      {
        review = new Review();
        review.setPath(RevuVfsUtils.buildAbsolutePath(project, filePath));
      }

      if (!load(review, false))
      {
        continue;
      }

      if (review.isShared() == sharedFilter)
      {
        addReview(review);
      }
    }

    for (Iterator<Review> it = reviews.iterator(); it.hasNext();)
    {
      Review review = it.next();
      if ((!review.isEmbedded()) && (review.isShared() == sharedFilter))
      {
        try
        {
          consolidateReview(review);
          if (changedReviews.containsValue(review))
          {
            fireReviewChanged(review);
          }
          else
          {
            fireReviewAdded(review);
          }
        }
        catch (RevuException e)
        {
          it.remove();
          LOGGER.warn("Failed to consolidate review file: " + review.getPath(), e);
          fireReviewLoadFailed(e, review.getPath());
        }
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

  public void save(@NotNull Review review)
  {
    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

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

  private List<Review> retrieveAfferentLinks(@NotNull Review review, @Nullable List<Review> reviewsToCheck)
    throws RevuException
  {
    if (reviewsToCheck == null)
    {
      reviewsToCheck = reviews;
    }

    List<Review> result = new ArrayList<Review>();
    for (Review tmpReview : reviewsToCheck)
    {
      if (review.equals(tmpReview.getExtendedReview()))
      {
        result.add(tmpReview);
      }
    }

    return result;
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
