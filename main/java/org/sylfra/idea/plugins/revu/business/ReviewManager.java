package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.config.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManager implements ProjectComponent, IRevuSettingsListener<RevuProjectSettings>
{
  private static final Logger LOGGER = Logger.getInstance(ReviewManager.class.getName());

  private Project project;
  private Map<VirtualFile, Review> reviews;
  private final List<IReviewListener> reviewListeners;

  public ReviewManager(Project project)
  {
    this.project = project;
    reviews = new HashMap<VirtualFile, Review>();
    reviewListeners = new LinkedList<IReviewListener>();

    RevuProjectSettingsComponent projectSettingsComponent = ServiceManager.getService(project,
      RevuProjectSettingsComponent.class);
    projectSettingsComponent.addListener(this);
    settingsChanged(projectSettingsComponent.getState());
  }

  public List<Review> getReviews()
  {
    return Collections.unmodifiableList(new ArrayList<Review>(reviews.values()));
  }

  public List<Review> getReviews(boolean active)
  {
    return getReviews(active, null);  
  }

  public List<Review> getReviews(boolean active, String userLogin)
  {
    List<Review> result = new ArrayList<Review>();
    for (Review review : reviews.values())
    {
      if ((review.isActive() == active)
        && ((userLogin == null) || (review.getReviewReferential().getUser(userLogin) != null)))
      {
        result.add(review);
      }
    }

    return Collections.unmodifiableList(result);
  }

  public void projectOpened()
  {
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

  public void settingsChanged(RevuProjectSettings projectSettings)
  {
    load(projectSettings);
  }

  public void addReviewListener(IReviewListener listener)
  {
    reviewListeners.add(listener);
  }

  public void addReview(VirtualFile f, Review review)
  {
    reviews.put(f, review);
    RevuProjectSettingsComponent projectSettingsComponent =
      ServiceManager.getService(project, RevuProjectSettingsComponent.class);
    projectSettingsComponent.getState().getReviewFiles().add(f.getPath());

    for (IReviewListener listener : reviewListeners)
    {
      listener.reviewAdded(review);
    }
  }

  public void load(RevuProjectSettings projectSettings)
  {
    IReviewExternalizer reviewExternalizer =
      ServiceManager.getService(project, IReviewExternalizer.class);

    // Delete obsolete reviews
    for (Iterator<Map.Entry<VirtualFile, Review>> it = reviews.entrySet().iterator();
         it.hasNext();)
    {
      Map.Entry<VirtualFile, Review> entry = it.next();
      VirtualFile file = entry.getKey();
      if (!projectSettings.getReviewFiles().contains(file))
      {
        Review review = entry.getValue();

        it.remove();

        for (IReviewListener listener : reviewListeners)
        {
          listener.reviewDeleted(review);
        }
      }
    }

    // Add new reviews
    for (String filePath : projectSettings.getReviewFiles())
    {
      VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
      if (!reviews.containsKey(file))
      {
        Review review = null;
        InputStream inputStream = null;
        try
        {
          // @TODO report error to user instead of only logging
          inputStream = file.getInputStream();
          review = reviewExternalizer.load(inputStream);

          for (IReviewListener listener : reviewListeners)
          {
            listener.reviewAdded(review);
          }
        }
        catch (IOException e)
        {
          LOGGER.warn("IO error while loading review file: " + file);
        }
        catch (RevuException e)
        {
          LOGGER.warn("Failed to load review file: " + file);
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
        reviews.put(file, review);
      }
    }
  }

  public void save()
  {
    IReviewExternalizer reviewExternalizer =
      ServiceManager.getService(project, IReviewExternalizer.class);

    for (Map.Entry<VirtualFile, Review> entry : reviews.entrySet())
    {
      VirtualFile file = entry.getKey();
      OutputStream out = null;
      try
      {
        // @TODO report error to user instead of only logging
        out = file.getOutputStream(this);
        reviewExternalizer.save(entry.getValue(), out);
      }
      catch (IOException e)
      {
        LOGGER.warn("IO error while writing review file: " + file);
      }
      catch (RevuException e)
      {
        LOGGER.warn("Failed to save review file : " + file, e);
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
          LOGGER.warn("Failed to close release review file: " + file, e);
        }
      }
    }
  }
}
