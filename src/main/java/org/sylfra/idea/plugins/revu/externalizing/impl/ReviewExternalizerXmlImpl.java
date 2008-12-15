package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.externalizing.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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
    return RevuPlugin.PLUGIN_NAME + ".ReviewLoader";
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
      xstream.unmarshal(new XppDriver().createReader(stream), null, xstreamDataHolder);
    }
    catch (Exception e)
    {
      LOGGER.warn(e);
      throw new RevuException(e);
    }
  }

  public void save(@NotNull Review review) throws RevuException
  {
    OutputStream out = null;
    try
    {
      out = new FileOutputStream(review.getPath());
      save(review, out);
    }
    catch (IOException e)
    {
      LOGGER.warn(e);
      throw new RevuException(e);
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
        LOGGER.warn("Failed to serialize review: " + review, e);
      }
    }
  }

  public void save(@NotNull Review review, @NotNull File file) throws RevuException, IOException
  {
    // Review will be fully serialized into a temporary memory stream before. It prevents
    // cases where serialization fails while writing to file and make an corrupted file

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    save(review, baos);

    FileOutputStream fos = null;
    try
    {
      fos = new FileOutputStream(file);
      byte[] content = baos.toByteArray();
      fos.write(content);
    }
    finally
    {
      if (fos != null)
      {
        fos.close();
      }
    }
  }

  public void save(@NotNull Review review, @NotNull OutputStream stream) throws RevuException
  {
    checkXStream();

    IndentingXMLStreamWriter writer;
    try
    {
      writer = new IndentingXMLStreamWriter(
        XMLOutputFactory.newInstance().createXMLStreamWriter(stream, "UTF-8"));
      writer.writeStartDocument("UTF-8", "1.0");
      writer.setDefaultNamespace("http://plugins.intellij.net/revu");
      xstreamDataHolder.put(ReviewExternalizerXmlImpl.CONTEXT_KEY_REVIEW, null);
      xstream.marshal(review, new XppDriver().createWriter(stream), xstreamDataHolder);
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
