package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.externalizing.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
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
  private Map<Review, Integer> reviewsSavedHashs;
  private Map<String, Review> reviewsByPaths;
  private Map<String, Review> reviewsByNames;
  private final List<IReviewListener> reviewListeners;
  private final List<IReviewExternalizationListener> reviewExternalizationListeners;
  private IRevuSettingsListener<RevuProjectSettings> projectSettingsListener;
  private IRevuSettingsListener<RevuWorkspaceSettings> workspaceSettingsListener;

  public ReviewManager(Project project)
  {
    this.project = project;
    reviews = new ArrayList<Review>();
    reviewsSavedHashs = new IdentityHashMap<Review, Integer>();
    reviewsByPaths = new HashMap<String, Review>();
    reviewsByNames = new HashMap<String, Review>();
    reviewListeners = new ArrayList<IReviewListener>();
    reviewExternalizationListeners = new ArrayList<IReviewExternalizationListener>();
  }

  @Nullable
  public Review getReviewByPath(@NotNull String path)
  {
    return reviewsByPaths.get(path);
  }

  @Nullable
  public Review getReviewByName(@NotNull String name)
  {
    return reviewsByNames.get(name);
  }

  @NotNull
  public List<Review> getReviews()
  {
    return Collections.unmodifiableList(reviews);
  }

  @NotNull
  public List<Review> getReviews(@Nullable String userLogin, @Nullable ReviewStatus... statuses)
  {
    return getReviews(null, userLogin, statuses);
  }

  @NotNull
  public List<Review> getReviews(@Nullable String userLogin, boolean active)
  {
    return getReviews(userLogin, (active ? new ReviewStatus[] {ReviewStatus.REVIEWING, ReviewStatus.FIXING} : null));
  }

  @NotNull
  public List<Review> getReviews(@Nullable List<Review> customReviews, @Nullable String userLogin,
    @Nullable ReviewStatus... statuses)
  {
    if (customReviews == null)
    {
      customReviews = reviews;
    }

    List<ReviewStatus> statusList = Arrays.asList((statuses == null) ? ReviewStatus.values() : statuses);

    List<Review> result = new ArrayList<Review>();
    for (Review review : customReviews)
    {
      if ((statusList.contains(review.getStatus())
        && ((userLogin == null) || (review.getDataReferential().getUser(userLogin, true) != null))))
      {
        result.add(review);
      }
    }

    return Collections.unmodifiableList(result);
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

  public void projectOpened()
  {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable()
    {
      public void run()
      {
        initEmbeddedReviews();

        List<String> projectReviewPaths =
          project.getComponent(RevuProjectSettingsComponent.class).getState().getReviewFiles();
        List<String> workspaceReviewPaths =
          project.getComponent(RevuWorkspaceSettingsComponent.class).getState().getReviewFiles();

        List<String> allPaths = new ArrayList<String>(projectReviewPaths);
        allPaths.addAll(workspaceReviewPaths);

        loadAndAdd(allPaths, null);
      }
    });

    installSettingsListeners();
  }

  public void projectClosed()
  {
    project.getComponent(RevuProjectSettingsComponent.class).removeListener(projectSettingsListener);
    project.getComponent(RevuWorkspaceSettingsComponent.class).removeListener(workspaceSettingsListener);
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

  private void addReview(@NotNull Review review)
  {
    reviews.add(review);
    reviewsByPaths.put(review.getPath(), review);
    reviewsByNames.put(review.getName(), review);
    reviewsSavedHashs.put(review, review.hashCode());
  }

  public boolean isModified(@NotNull Review review)
  {
    return (review.hashCode() != reviewsSavedHashs.get(review));
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

  private void initEmbeddedReviews()
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
  }

  private void loadAndAdd(@NotNull List<String> filePaths, @Nullable Boolean sharedFilter)
  {
    List<Review> reviewsToLoad = prepareReviewsToLoad(filePaths);

    Map<String, Review> changedReviews = new HashMap<String, Review>();

    if (sharedFilter != null)
    {
      // Delete obsolete reviews
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
        review.clearIssuesListeners();
        reviewsByPaths.remove(review.getPath());
        reviewsByNames.remove(review.getName());
        reviewsSavedHashs.remove(review);
        it.remove();
      }
    }

    // Load remaining reviews and added review
    for (Review review : reviewsToLoad)
    {
      if (!load(review, false))
      {
        continue;
      }

      if ((sharedFilter == null) || (review.isShared() == sharedFilter))
      {
        addReview(review);
      }
    }

    for (Review review : reviews)
    {
      if ((!review.isEmbedded()) && (((sharedFilter == null) || review.isShared() == sharedFilter)))
      {
        reviewsSavedHashs.put(review, review.hashCode());

        if (changedReviews.containsValue(review))
        {
          fireReviewChanged(review);
        }
        else
        {
          fireReviewAdded(review);
        }
      }
    }
  }

  private List<Review> prepareReviewsToLoad(List<String> relativePaths)
  {
    Map<String, ReviewExtendedDepth> reviewByNames = new HashMap<String, ReviewExtendedDepth>(relativePaths.size());
    for (String relativePath : relativePaths)
    {
      String absolutePath = RevuVfsUtils.buildAbsolutePath(project, relativePath);
      Review review = reviewsByPaths.get(absolutePath);
      if (review == null)
      {
        review = new Review();
        review.setPath(absolutePath);
      }

      if (load(review, true))
      {
        ReviewExtendedDepth reviewExtendedDepth = new ReviewExtendedDepth(review, 0);
        reviewByNames.put(review.getName(), reviewExtendedDepth);
      }
    }

    List<ReviewExtendedDepth> reviewExtendedDepths = new ArrayList<ReviewExtendedDepth>(reviewByNames.values());
    for (ReviewExtendedDepth reviewExtendedDepth : reviewExtendedDepths)
    {
      Review review = reviewExtendedDepth.review;
      if (review.getExtendedReview() != null)
      {
        try
        {
          checkCyclicLink(review, review.getExtendedReview());
        }
        catch (RevuException e)
        {
          LOGGER.warn("Cyclic link: " + review.getPath(), e);
          fireReviewLoadFailed(e, review.getPath());
        }

        while ((review != null) && (review.getExtendedReview() != null))
        {
          reviewExtendedDepth.depth++;
          review = reviewsByNames.get(review.getExtendedReview().getName());
        }
      }
    }

    Collections.sort(reviewExtendedDepths, new Comparator<ReviewExtendedDepth>()
    {
      public int compare(ReviewExtendedDepth o1, ReviewExtendedDepth o2)
      {
        return o1.depth - o2.depth;
      }
    });

    List<Review> result = new ArrayList<Review>(reviewExtendedDepths.size());
    for (ReviewExtendedDepth reviewExtendedDepth : reviewExtendedDepths)
    {
      result.add(reviewExtendedDepth.review);
    }

    return result;
  }

  public boolean load(@NotNull final Review review, boolean prepare)
  {
    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    InputStream inputStream = null;
    Exception exception = null;
    String path = review.getPath();
    try
    {
      inputStream = getInputStreamFromPath(path);
      if (inputStream == null)
      {
        LOGGER.warn("Can't retrieve stream for path: " + path);
        exception = new FileNotFoundException(path);
      }
      else
      {
        reviewExternalizer.load(review, inputStream, prepare);

        if (!prepare)
        {
          fireReviewLoadSucceeded(review);
        }
      }
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

  public void save(@NotNull Review review)
  {
    assert (!review.isEmbedded()) : "Embedded review cannot be saved : " + review;
    
    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    Exception exception = null;
    try
    {
      reviewExternalizer.save(review, new File(review.getPath()));

      fireReviewSaveSucceeded(review);
      reviewsSavedHashs.put(review, review.hashCode());
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

    if (exception != null)
    {
      fireReviewSavedFailed(review, exception);
    }
  }

  public void saveChangedReviews()
  {
    for (Review review : reviews)
    {
      if ((!review.isEmbedded())
        && ((!reviewsSavedHashs.containsKey(review)) || (review.hashCode() != reviewsSavedHashs.get(review))))
      {
        save(review);
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
    if (main.getName().equals(extendedChild.getName()))
    {
      throw new RevuException(
        RevuBundle.message("friendlyError.externalizing.cyclicReview.details.text",
          main.getPath(), main.getName(),
          extendedRoot.getPath(), extendedRoot.getName()));
    }

    Review extendedChild2;
    while ((extendedChild2 = extendedChild.getExtendedReview()) != null)
    {
      checkCyclicLink(main, extendedRoot, extendedChild2);
    }
  }

  private void fireReviewAdded(Review review)
  {
    // Defensive copy against concurrent modifications
    List<IReviewListener> copy = new ArrayList<IReviewListener>(reviewListeners);
    for (IReviewListener listener : copy)
    {
      listener.reviewAdded(review);
    }
  }

  private void fireReviewChanged(Review review)
  {
    // Defensive copy against concurrent modifications
    List<IReviewListener> copy = new ArrayList<IReviewListener>(reviewListeners);
    for (IReviewListener listener : copy)
    {
      listener.reviewChanged(review);
    }
  }

  private void fireReviewDeleted(Review review)
  {
    // Defensive copy against concurrent modifications
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

  private static final class ReviewExtendedDepth
  {
    Review review;
    int depth;

    private ReviewExtendedDepth(Review review, int depth)
    {
      this.review = review;
      this.depth = depth;
    }
  }
}
