package org.sylfra.idea.plugins.revu.externalizing;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.model.Review;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewExternalizer
{
  void load(@NotNull Review review, @NotNull InputStream stream, boolean prepare) throws RevuException;

  void save(@NotNull Review review, @NotNull File file) throws RevuException, IOException;
}
