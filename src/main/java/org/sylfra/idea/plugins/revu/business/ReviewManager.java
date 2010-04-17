package org.sylfra.idea.plugins.revu.business;

import com.intellij.AppTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.*;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.externalizing.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.settings.AbstractReviewFilesRevuSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.AbstractReviewFilesRevuSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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

  private final Project project;
  private final RevuFileListener fileListener;
  private Map<Review, MetaReview> metaReviews;
  private Map<String, Review> reviewsByPaths;
  private Map<String, Review> reviewsByNames;
  private final List<IReviewListener> reviewListeners;
  private final List<IReviewExternalizationListener> reviewExternalizationListeners;
  private IRevuSettingsListener<RevuProjectSettings> projectSettingsListener;
  private IRevuSettingsListener<RevuWorkspaceSettings> workspaceSettingsListener;

  public ReviewManager(Project project)
  {
    this.project = project;
    this.fileListener = new RevuFileListener(project, this);
    metaReviews = new IdentityHashMap<Review, MetaReview>();
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
  public SortedSet<Review> getReviews()
  {
    return new TreeSet<Review>(reviewsByNames.values());
  }

  @NotNull
  public Collection<Review> getReviews(@Nullable String userLogin, @Nullable ReviewStatus... statuses)
  {
    return getReviews(null, userLogin, statuses);
  }

  @NotNull
  public Collection<Review> getReviews(@Nullable String userLogin, boolean active)
  {
    return getReviews(userLogin, (active ? new ReviewStatus[] {ReviewStatus.REVIEWING, ReviewStatus.FIXING} : null));
  }

  @NotNull
  public Collection<Review> getReviews(@Nullable Collection<Review> customReviews, @Nullable String userLogin,
    @Nullable ReviewStatus... statuses)
  {
    if (customReviews == null)
    {
      customReviews = reviewsByNames.values();
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
    project.getComponent(RevuProjectSettingsComponent.class).addListener(projectSettingsListener);

    // Sould reload review when some workspace settins change ?!
//    workspaceSettingsListener = new IRevuSettingsListener<RevuWorkspaceSettings>()
//    {
//      public void settingsChanged(RevuWorkspaceSettings settings)
//      {
//        loadAndAdd(settings.getReviewFiles(), false);
//      }
//    };
//    project.getComponent(RevuWorkspaceSettingsComponent.class).addListener(workspaceSettingsListener);
  }

  public void projectOpened()
  {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable()
    {
      public void run()
      {
        initEmbeddedReviews();

        List<String> projectReviewPaths = RevuUtils.getProjectSettings(project).getReviewFiles();
        List<String> workspaceReviewPaths = RevuUtils.getWorkspaceSettings(project).getReviewFiles();

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
//    project.getComponent(RevuWorkspaceSettingsComponent.class).removeListener(workspaceSettingsListener);
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
    fileListener.dispose();
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
    reviewsByPaths.put(review.getPath(), review);
    reviewsByNames.put(review.getName(), review);
    metaReviews.put(review, new MetaReview(review, 0));
  }

  public void removeReview(@NotNull Review review)
  {
    Review oldReview = reviewsByPaths.remove(review.getPath());
    reviewsByNames.remove(review.getName());
    metaReviews.remove(oldReview);

    fireReviewDeleted(review);
  }

  public void reviewFileChanged(@NotNull Review review, @NotNull VirtualFile newFile)
  {
    String oldPath = review.getPath();
    String newPath = newFile.getPath();

    reviewsByPaths.remove(oldPath);

    review.setPath(newPath);
    reviewsByPaths.put(oldPath, review);

    fireReviewChanged(review);

    Class<? extends AbstractReviewFilesRevuSettingsComponent> settingsComponentClass =
      review.isShared() ? RevuProjectSettingsComponent.class : RevuWorkspaceSettingsComponent.class;
    AbstractReviewFilesRevuSettingsComponent settingsComponent = project.getComponent(settingsComponentClass);

    AbstractReviewFilesRevuSettings state = (AbstractReviewFilesRevuSettings) settingsComponent.getState();
    List<String> reviewFiles = state.getReviewFiles();
    int index = reviewFiles.indexOf(RevuVfsUtils.buildRelativePath(project, oldPath));
    if (index != -1)
    {
      reviewFiles.set(index, RevuVfsUtils.buildRelativePath(project, newPath));
      settingsComponent.loadState(state);
    }
  }

  public boolean isContentModified(@NotNull Review review)
  {
    MetaReview meta = metaReviews.get(review);
    return ((meta == null) || (review.hashCode() != meta.contentHash));
  }

  public long getLastSavedTStamp(@NotNull Review review)
  {
    MetaReview meta = metaReviews.get(review);
    return (meta == null) ? -1 : meta.lastSaved;
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
    reviewsByNames.clear();
    reviewsByPaths.clear();
    metaReviews.clear();

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
      Collection<Review> reviews = new HashSet<Review>(getReviews());
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
        Review oldReview = reviewsByPaths.remove(review.getPath());
        reviewsByNames.remove(review.getName());
        metaReviews.remove(oldReview);
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

    for (Review review : getReviews())
    {
      if ((!review.isEmbedded()) && (((sharedFilter == null) || review.isShared() == sharedFilter)))
      {
        metaReviews.put(review, new MetaReview(review, System.currentTimeMillis()));

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

  public void save(@NotNull Review review) throws RevuException, IOException
  {
    assert (!review.isEmbedded()) : "Embedded review cannot be saved : " + review;

    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    reviewExternalizer.save(review, new File(review.getPath()));
    fireReviewSaveSucceeded(review);
    metaReviews.put(review, new MetaReview(review, System.currentTimeMillis()));
  }

  public void saveSilently(@NotNull Review review)
  {
    Exception exception = null;
    try
    {
      save(review);
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
    for (Review review : getReviews())
    {
      if (!review.isEmbedded())
      {
        MetaReview metaReview = metaReviews.get(review);
        if (review.hashCode() != metaReview.contentHash)
        {
          saveSilently(review);
        }
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

    private ReviewExtendedDepth(@NotNull Review review, int depth)
    {
      this.review = review;
      this.depth = depth;
    }
  }

  private static final class MetaReview
  {
    Review review;
    int contentHash;
    long lastSaved;

    public MetaReview(Review review, long lastSaved)
    {
      this.review = review;
      this.lastSaved = lastSaved;
      contentHash = review.hashCode();
    }
  }

  /**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
   * @version $Id$
   */
  public static class RevuFileListener implements Disposable
  {
    private final Project project;
    private final ReviewManager reviewManager;
    private final VirtualFileListener virtualFileListener;
    private final FileDocumentManagerAdapter fileDocumentManagerListener;
    private MessageBusConnection messageBusConnection;

    public RevuFileListener(final Project project, final ReviewManager reviewManager)
    {
      this.reviewManager = reviewManager;
      this.project = project;

      virtualFileListener = new VirtualFileAdapter()
      {
        @Override
        public void contentsChanged(VirtualFileEvent event)
        {
          VirtualFile vFile = event.getFile();

          Review review = reviewManager.getReviewByPath(vFile.getPath());
          if ((review != null) && (vFile.getTimeStamp() > reviewManager.getLastSavedTStamp(review)))
          {
            if (Messages.showOkCancelDialog(project,
              RevuBundle.message("general.reviewFileChanged.text", review.getName()),
              RevuBundle.message("general.reviewFileChanged.title"),
              Messages.getWarningIcon()) == DialogWrapper.OK_EXIT_CODE)
            {
              reviewManager.load(review, false);
            }
          }
        }

        @Override
        public void fileDeleted(VirtualFileEvent event)
        {
          VirtualFile vFile = event.getFile();
          Review review = reviewManager.getReviewByPath(vFile.getPath());
          if (review != null)
          {
            reviewManager.removeReview(review);
          }
        }

        @Override
        public void propertyChanged(VirtualFilePropertyEvent event)
        {
          if (VirtualFile.PROP_NAME.equals(event.getPropertyName()))
          {
            String oldPath = event.getFile().getParent().getPath() + "/" + event.getOldValue();
            pathChanged(event.getFile(), oldPath);
          }
        }

        @Override
        public void fileMoved(VirtualFileMoveEvent event)
        {
          String oldPath = event.getOldParent().getPath() + "/" + event.getFileName();
          pathChanged(event.getFile(), oldPath);
        }
      };

      fileDocumentManagerListener = new FileDocumentManagerAdapter() {
        @Override
        public void beforeAllDocumentsSaving()
        {
          // File with issues
          reviewManager.saveChangedReviews();
        }
      };
      messageBusConnection = this.project.getMessageBus().connect();

      LocalFileSystem.getInstance().addVirtualFileListener(virtualFileListener);
      messageBusConnection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, fileDocumentManagerListener);
    }

    private void pathChanged(VirtualFile vFile, String oldPath)
    {
      Review review = reviewManager.getReviewByPath(oldPath);
      if (review != null)
      {
        reviewManager.reviewFileChanged(review, vFile);
      }
    }

    public void dispose()
    {
      LocalFileSystem.getInstance().removeVirtualFileListener(virtualFileListener);
      messageBusConnection.disconnect();
    }
  }
}
