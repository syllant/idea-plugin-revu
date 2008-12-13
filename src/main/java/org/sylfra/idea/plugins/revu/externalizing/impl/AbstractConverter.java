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

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
abstract class AbstractConverter implements Converter
{
  protected final Logger logger;

  protected AbstractConverter()
  {
    logger = Logger.getLogger(getClass());
  }

  protected Review getReview(DataHolder dataHolder)
  {
    return (Review) dataHolder.get(ReviewExternalizerXmlImpl.CONTEXT_KEY_REVIEW);
  }

  protected Project getProject(DataHolder dataHolder)
  {
    return (Project) dataHolder.get(ReviewExternalizerXmlImpl.CONTEXT_KEY_PROJECT);
  }

  protected User retrieveUser(@NotNull UnmarshallingContext context, @Nullable String login)
  {
    Review review = getReview(context);

    return RevuUtils.getNonNullUser(review.getDataReferential(), login);
  }
}
