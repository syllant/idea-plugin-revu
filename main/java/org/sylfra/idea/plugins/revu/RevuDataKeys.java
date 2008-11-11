package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.actionSystem.DataKey;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public final class RevuDataKeys
{
  public static final DataKey<Review> REVIEW = DataKey.create("revu.Review");
  public static final DataKey<ReviewItem> REVIEW_ITEM = DataKey.create("revu.ReviewItem");
  public static final DataKey<List<ReviewItem>> REVIEW_ITEM_ARRAY = DataKey.create("revu.ReviewItemArray");
}
