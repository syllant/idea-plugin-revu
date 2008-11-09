package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.actionSystem.DataKey;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public final class RevuDataKeys
{
  public static final DataKey<Review> REVIEW = DataKey.create("revu.Review");
  public static final DataKey<ReviewItem> REVIEW_ITEM = DataKey.create("revu.ReviewItem");
}
