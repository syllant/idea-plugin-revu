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
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarComponent;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarMessage;
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
  private static final Map<File, String> EMBEDDED_REVIEWS = new HashMap<File, String>(1);

  static
  {
    EMBEDDED_REVIEWS.put(new File("[default]"), "/org/sylfra/idea/plugins/revu/resources/defaultReviewTemplate.xml");
  }

  private final Project project;
  private final RevuFileListener fileListener;
  private Map<Review, MetaReview> metaReviews;
  private Map<File, Review> reviewsByFiles;
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
    reviewsByFiles = new HashMap<File, Review>();
    reviewsByNames = new HashMap<String, Review>();
    reviewListeners = new ArrayList<IReviewListener>();
    reviewExternalizationListeners = new ArrayList<IReviewExternalizationListener>();
  }

  @Nullable
  public Review getReviewByFile(@NotNull File file)
  {
    return reviewsByFiles.get(file);
  }

  @Nullable
  public Review getReviewByName(@NotNull String name)
  {
    return reviewsByNames.get(name);
  }

  public boolean hasReview(@NotNull String name)
  {
    return reviewsByNames.containsKey(name);
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
    return getReviews(userLogin, (active ? new ReviewStatus[]{ReviewStatus.REVIEWING, ReviewStatus.FIXING} : null));
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
      public void settingsChanged(RevuProjectSettings oldSettings, RevuProjectSettings newSettings)
      {
        if (!newSettings.getReviewFiles().equals(oldSettings.getReviewFiles()))
        {
          loadAndAdd(newSettings.getReviewFiles(), true);
        }
      }
    };
    project.getComponent(RevuProjectSettingsComponent.class).addListener(projectSettingsListener);

    workspaceSettingsListener = new IRevuSettingsListener<RevuWorkspaceSettings>()
    {
      public void settingsChanged(RevuWorkspaceSettings oldSettings, RevuWorkspaceSettings newSettings)
      {
        if (!newSettings.getReviewFiles().equals(oldSettings.getReviewFiles()))
        {
          loadAndAdd(newSettings.getReviewFiles(), false);
        }
      }
    };
    project.getComponent(RevuWorkspaceSettingsComponent.class).addListener(workspaceSettingsListener);
  }

  public void projectOpened()
  {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable()
    {
      public void run()
      {
        initEmbeddedReviews();

        RevuWorkspaceSettings workspaceSettings = RevuUtils.getWorkspaceSettings(project);
        List<String> workspaceReviewPaths = workspaceSettings.getReviewFiles();
        List<String> projectReviewPaths = RevuUtils.getProjectSettings(project).getReviewFiles();

        List<String> allPaths = new ArrayList<String>(projectReviewPaths);
        allPaths.addAll(workspaceReviewPaths);

        loadAndAdd(allPaths, null);

        // Check that reviewingReviewName matches an existing review
        String reviewingReviewName = workspaceSettings.getReviewingReviewName();
        if (reviewingReviewName != null)
        {
          Review review = getReviewByName(reviewingReviewName);
          if ((review == null) || (!ReviewStatus.REVIEWING.equals(review.getStatus())))
          {
            workspaceSettings.setReviewingReviewName(null);
            project.getComponent(RevuWorkspaceSettingsComponent.class).loadState(workspaceSettings);
          }
        }

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
    reviewsByFiles.put(review.getFile(), review);
    reviewsByNames.put(review.getName(), review);
    metaReviews.put(review, new MetaReview(review, -1, false));
  }

  public void removeReview(@NotNull Review review)
  {
    Review oldReview = reviewsByFiles.remove(review.getFile());
    reviewsByNames.remove(review.getName());
    metaReviews.remove(oldReview);

    fireReviewDeleted(review);
  }

  public void reviewFileChanged(@NotNull Review review, @NotNull VirtualFile newVFile)
  {
    File oldFile = review.getFile();
    File newFile = new File(newVFile.getPath());

    reviewsByFiles.remove(oldFile);

    review.setFile(newFile);
    reviewsByFiles.put(newFile, review);

    fireReviewChanged(review);

    Class<? extends AbstractReviewFilesRevuSettingsComponent> settingsComponentClass =
      review.isShared() ? RevuProjectSettingsComponent.class : RevuWorkspaceSettingsComponent.class;
    AbstractReviewFilesRevuSettingsComponent settingsComponent = project.getComponent(settingsComponentClass);

    AbstractReviewFilesRevuSettings state = (AbstractReviewFilesRevuSettings) settingsComponent.getState();
    List<String> reviewFiles = state.getReviewFiles();
    int index = reviewFiles.indexOf(RevuVfsUtils.buildRelativePath(project, oldFile));
    if (index != -1)
    {
      reviewFiles.set(index, RevuVfsUtils.buildRelativePath(project, newFile));
      settingsComponent.loadState(state);
    }
  }

  public boolean isContentModified(@NotNull Review review)
  {
    MetaReview meta = metaReviews.get(review);
    return ((meta == null) || (review.hashCode() != meta.reviewHashCode));
  }

  public long getLastSavedTStamp(@NotNull Review review)
  {
    MetaReview meta = metaReviews.get(review);
    return (meta == null) ? -1 : meta.lastSaved;
  }

  private boolean isSaving(Review review)
  {
    MetaReview meta = metaReviews.get(review);
    return (meta != null) && meta.isSaving;
  }

  @Nullable
  private InputStream getInputStream(@NotNull Review review) throws IOException
  {
    // Embedded review
    if (review.isEmbedded())
    {
      return getClass().getClassLoader().getResourceAsStream(EMBEDDED_REVIEWS.get(review.getFile()));
    }

    return new FileInputStream(review.getFile());
  }

  private void initEmbeddedReviews()
  {
    reviewsByNames.clear();
    reviewsByFiles.clear();
    metaReviews.clear();

    for (Map.Entry<File, String> entry : EMBEDDED_REVIEWS.entrySet())
    {
      Review review = new Review();
      review.setFile(entry.getKey());
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

        String filePath = RevuVfsUtils.buildRelativePath(project, review.getFile());
        if (filePaths.contains(filePath))
        {
          changedReviews.put(filePath, review);
        }
        else
        {
          fireReviewDeleted(review);
        }
        Review oldReview = reviewsByFiles.remove(review.getFile());
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
        metaReviews.put(review, new MetaReview(review, System.currentTimeMillis(), false));

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
      File file = RevuVfsUtils.findFileFromRelativePath(project, relativePath);
      Review review = reviewsByFiles.get(file);
      if (review == null)
      {
        review = new Review();
        review.setFile(file);
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
          LOGGER.warn("Cyclic link: " + review.getFile(), e);
          fireReviewLoadFailed(e, review.getFile());
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
    File file = review.getFile();
    try
    {
      inputStream = getInputStream(review);
      if (inputStream == null)
      {
        LOGGER.warn("Can't retrieve stream for review: " + review.getFile());
        //noinspection ThrowableInstanceNeverThrown
        exception = new FileNotFoundException(file.getPath());
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
      LOGGER.warn("IO error while loading review file: " + file, e);
    }
    catch (RevuException e)
    {
      exception = e;
      LOGGER.warn("Failed to load review file: " + file, e);
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
        LOGGER.warn("Failed to close release review file: " + file, e);
      }
    }

    if (exception != null)
    {
      fireReviewLoadFailed(exception, file);

      return false;
    }

    return true;
  }

  public void save(@NotNull Review review) throws RevuException, IOException
  {
    assert (!review.isEmbedded()) : "Embedded review cannot be saved : " + review;

    IReviewExternalizer reviewExternalizer = project.getComponent(IReviewExternalizer.class);

    boolean newReview;
    MetaReview metaReview = metaReviews.get(review);
    if (metaReview == null)
    {
      reviewsByFiles.put(review.getFile(), review);
      reviewsByNames.put(review.getName(), review);

      metaReview = new MetaReview(review, -1, true);
      metaReviews.put(review, metaReview);

      newReview = true;
    }
    else
    {
      metaReview.isSaving = true;
      metaReview.updateReviewHashcode();

      newReview = false;
    }

    try
    {
      reviewExternalizer.save(review, review.getFile());
    }
    finally
    {
      metaReview.isSaving = false;
    }

    metaReview.lastSaved = System.currentTimeMillis();
    fireReviewSaveSucceeded(review);

    if (newReview)
    {
      fireReviewAdded(review);
    }
    else
    {
      fireReviewChanged(review);
    }
  }

  public void saveChanges(@NotNull Review review)
  {
    try
    {
      save(review);
    }
    catch (Exception exception)
    {
      LOGGER.warn(exception);

      final String details = ((exception.getLocalizedMessage() == null)
        ? exception.toString() : exception.getLocalizedMessage());
      project.getComponent(StatusBarComponent.class).addMessage(new StatusBarMessage(StatusBarMessage.Type.ERROR,
        RevuBundle.message("friendlyError.externalizing.save.error.title.text"),
        RevuBundle.message("friendlyError.externalizing.load.error.details.text", review.getFile(), details)), true);

      return;
    }

    fireReviewChanged(review);
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
      LOGGER.warn("IO error while writing review file: " + review.getFile(), e);
      exception = e;
    }
    catch (RevuException e)
    {
      LOGGER.warn("Failed to save review file : " + review.getFile(), e);
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
        if (review.hashCode() != metaReview.reviewHashCode)
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
          main.getFile(), main.getName(),
          extendedRoot.getFile(), extendedRoot.getName()));
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

  private void fireReviewLoadFailed(Exception exception, File file)
  {
    List<IReviewExternalizationListener> copy =
      new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.loadFailed(file, exception);
    }
  }

  private void fireReviewLoadSucceeded(Review review)
  {
    List<IReviewExternalizationListener> copy =
      new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.loadSucceeded(review);
    }
  }

  private void fireReviewSaveSucceeded(Review review)
  {
    List<IReviewExternalizationListener> copy =
      new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
    for (IReviewExternalizationListener listener : copy)
    {
      listener.saveSucceeded(review);
    }
  }

  private void fireReviewSavedFailed(Review review, Exception exception)
  {
    List<IReviewExternalizationListener> copy =
      new ArrayList<IReviewExternalizationListener>(reviewExternalizationListeners);
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
    int reviewHashCode;
    long lastSaved;
    boolean isSaving;

    public MetaReview(Review review, long lastSaved, boolean isSaving)
    {
      this.review = review;
      this.lastSaved = lastSaved;
      this.isSaving = isSaving;
      updateReviewHashcode();
    }

    void updateReviewHashcode()
    {
      reviewHashCode = review.hashCode();
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

          Review review = reviewManager.getReviewByFile(new File(vFile.getPath()));
          if ((review != null)
            && (vFile.getTimeStamp() > reviewManager.getLastSavedTStamp(review))
            && (!reviewManager.isSaving(review)))
          {
            if (Messages.showOkCancelDialog(project,
              RevuBundle.message("general.reviewFileChanged.text", review.getName()),
              RevuBundle.message("general.reviewFileChanged.title"),
              Messages.getWarningIcon()) == DialogWrapper.OK_EXIT_CODE)
            {
              reviewManager.load(review, false);
              reviewManager.fireReviewChanged(review);
            }
          }
        }

        @Override
        public void fileDeleted(VirtualFileEvent event)
        {
          VirtualFile vFile = event.getFile();
          Review review = reviewManager.getReviewByFile(new File(vFile.getPath()));
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

      fileDocumentManagerListener = new FileDocumentManagerAdapter()
      {
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
      Review review = reviewManager.getReviewByFile(new File(oldPath));
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
