package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.config.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizerXmlImpl implements IReviewExternalizer, ApplicationComponent
{
  private static final Logger LOGGER = Logger.getInstance(ReviewExternalizerXmlImpl.class.getName())
    ;

  // Base on XStream library
  private XStream xstream;

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

  public Review load(InputStream stream) throws RevuException
  {
    checkXStream();

    return (Review) xstream.fromXML(stream);
  }

  public void save(Review review, OutputStream stream) throws RevuException
  {
    checkXStream();

    IndentingXMLStreamWriter writer;
    try
    {
      writer = new IndentingXMLStreamWriter(
        XMLOutputFactory.newInstance().createXMLStreamWriter(stream));
      writer.writeStartDocument("UTF-8", "1.0");
      xstream.marshal(review, new XppDriver().createWriter(stream));
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
      xstream.registerConverter(new ReviewItemConverter());
      xstream.registerConverter(new ReviewPriorityConverter());
      xstream.registerConverter(new ReviewReferentialConverter());
      xstream.registerConverter(new UserConverter());
    }
  }
}
