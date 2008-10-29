package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import org.apache.log4j.Logger;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
abstract class AbstractConverter implements Converter
{
  static final String CONTEXT_KEY_REVIEW = "review";

  protected final Logger logger;

  protected AbstractConverter()
  {
    logger = Logger.getLogger(getClass());
  }

  protected Review getReview(UnmarshallingContext context)
  {
    return (Review) context.get(CONTEXT_KEY_REVIEW);
  }

  protected User retrieveUser(UnmarshallingContext context, String login)
  {
    Review review = getReview(context);
    User user = review.getReviewReferential().getUser(login);

    if (user == null)
    {
      logger.warn("Can't find user from review users: " + user);
      user = User.UNKNOWN;
    }

    return user;
  }
}
