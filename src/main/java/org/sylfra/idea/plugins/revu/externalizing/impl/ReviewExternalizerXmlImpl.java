package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.externalizing.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizerXmlImpl implements IReviewExternalizer, ProjectComponent
{
  private static final Logger LOGGER = Logger
    .getInstance(ReviewExternalizerXmlImpl.class.getName());

  static final String CONTEXT_KEY_PROJECT = "project";
  static final String CONTEXT_KEY_REVIEW = "review";
  static final String CONTEXT_KEY_PREPARE_MODE = "onlyExtendInfo";

  private final Project project;
  // Base on XStream library
  private XStream xstream;
  private DataHolder xstreamDataHolder;

  public ReviewExternalizerXmlImpl(Project project)
  {
    this.project = project;
  }

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent()
  {
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent()
  {
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  public void load(@NotNull Review review, @NotNull InputStream stream, boolean prepareMode) throws RevuException
  {
    checkXStream();

    xstreamDataHolder.put(ReviewExternalizerXmlImpl.CONTEXT_KEY_REVIEW, review);
    xstreamDataHolder.put(ReviewExternalizerXmlImpl.CONTEXT_KEY_PREPARE_MODE, prepareMode);

    try
    {
      xstream.unmarshal(new DomDriver().createReader(new InputStreamReader(stream, "UTF-8")), null, xstreamDataHolder);
    }
    catch (Exception e)
    {
      LOGGER.warn(e);
      throw new RevuException(e);
    }
  }

  public void save(@NotNull Review review, @NotNull final File file) throws RevuException, IOException
  {
    // Review will be fully serialized into a temporary memory stream before. It prevents
    // cases where serialization fails while writing to file and makes corrupted file
    final StringWriter writer = new StringWriter();
    save(review, writer);

    // XML conversion is OK, now saving it to a file
    final RevuException[] exception = new RevuException[1];
    ApplicationManager.getApplication().runWriteAction(new Runnable()
    {
      public void run()
      {
        try
        {
          final VirtualFile vParentFile = RevuVfsUtils.findFile(file.getParent());
          if (vParentFile == null)
          {
            exception[0] = new RevuException("Parent directory does not exist: " + file.getParent());
            return;
          }

          chmodReviewFile(file);

          VirtualFile vFile = vParentFile.createChildData(this, file.getName());
          VfsUtil.saveText(vFile, writer.toString());
        }
        catch (RevuException e)
        {
          exception[0] = e;
        }
        catch (IOException e)
        {
          exception[0] = new RevuException(e);
        }
      }
    });

    if (exception[0] != null)
    {
      throw exception[0];
    }
  }

  private void chmodReviewFile(File file) throws RevuException
  {
    if (file.exists() && !file.canWrite())
    {
      if (file.setWritable(true))
      {
        LOGGER.debug("Review file was readonly, chmoded it: " + file);
      }
      else
      {
        throw new RevuException("Review file is readonly and cannot chmod it: " + file);
      }
    }
  }

  public void save(@NotNull Review review, @NotNull Writer writer) throws RevuException
  {
    checkXStream();

    IndentingXMLStreamWriter xmlWriter;
    try
    {
      xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
      xmlWriter.writeStartDocument("UTF-8", "1.0");
      xmlWriter.setDefaultNamespace("http://plugins.intellij.net/revu");
      xstreamDataHolder.put(ReviewExternalizerXmlImpl.CONTEXT_KEY_REVIEW, null);
      xstream.marshal(review, new XppDriver().createWriter(writer), xstreamDataHolder);
    }
    catch (XMLStreamException e)
    {
      throw new RevuException("Failed to serialize review: " + review, e);
    }
  }

  /**
   * Initializes XStream environment
   */
  private void checkXStream()
  {
    if (xstream == null)
    {
      xstream = new XStream();

      xstream.setClassLoader(getClass().getClassLoader());

      xstream.alias("review", Review.class);

      xstream.registerConverter(new HistoryConverter());
      xstream.registerConverter(new FileScopeConverter());
      xstream.registerConverter(new ReviewConverter());
      xstream.registerConverter(new IssueConverter());
      xstream.registerConverter(new IssueNoteConverter());
      xstream.registerConverter(new IssueTagConverter());
      xstream.registerConverter(new ReviewPriorityConverter());
      xstream.registerConverter(new DataReferentialConverter());
      xstream.registerConverter(new UserConverter());

      xstreamDataHolder = xstream.newDataHolder();
      xstreamDataHolder.put(CONTEXT_KEY_PROJECT, project);
    }
  }
}
