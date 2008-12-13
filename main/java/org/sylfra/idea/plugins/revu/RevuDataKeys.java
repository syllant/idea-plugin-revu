package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.actionSystem.DataKey;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;

import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public final class RevuDataKeys
{
  public static final DataKey<Review> REVIEW = DataKey.create("revu.Review");
  public static final DataKey<Issue> ISSUE = DataKey.create("revu.Issue");
  public static final DataKey<List<Issue>> ISSUE_ARRAY = DataKey.create("revu.IssueArray");
}
