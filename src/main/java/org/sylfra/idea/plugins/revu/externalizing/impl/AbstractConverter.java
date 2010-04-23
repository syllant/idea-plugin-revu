package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.intellij.openapi.project.Project;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
abstract class AbstractConverter implements Converter
{
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss Z");

  protected final Logger logger;

  protected AbstractConverter()
  {
    logger = Logger.getLogger(getClass());
  }

  @NotNull
  protected Review getReview(DataHolder dataHolder)
  {
    return (Review) dataHolder.get(ReviewExternalizerXmlImpl.CONTEXT_KEY_REVIEW);
  }

  @NotNull
  protected Project getProject(DataHolder dataHolder)
  {
    return (Project) dataHolder.get(ReviewExternalizerXmlImpl.CONTEXT_KEY_PROJECT);
  }

  @NotNull
  protected User retrieveUser(@NotNull UnmarshallingContext context, @Nullable String login)
  {
    Review review = getReview(context);

    return RevuUtils.getNonNullUser(review.getDataReferential(), login);
  }

  @NotNull
  protected String formatDate(@Nullable Date value)
  {
    synchronized (DATE_FORMATTER)
    {
      return DATE_FORMATTER.format(value == null ? new Date() : value);
    }
  }

  @NotNull
  protected Date parseDate(@NotNull String value)
  {
    try
    {
      synchronized (DATE_FORMATTER)
      {
        return DATE_FORMATTER.parse(value);
      }
    }
    catch (ParseException e)
    {
      logger.warn("Found bad date: " + value + ". Will use current date.");
      return new Date();
    }
  }
}
