package org.sylfra.idea.plugins.revu.config;

import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.model.Review;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewExternalizer
{
  Review load(InputStream stream) throws RevuException;

  void save(Review review, OutputStream stream) throws RevuException;
}
