package org.sylfra.idea.plugins.revu.config.impl;

import org.apache.tools.ant.util.FileUtils;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.model.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewLoaderXmlImplTest
{
  @Test
  public void testLoad()
  {
    ReviewExternalizerXmlImpl impl = new ReviewExternalizerXmlImpl();
    try
    {
      Review review =
        impl.load(getClass().getClassLoader().getResourceAsStream("review-sample.xml"));

      Review sampleReview = buildSampleReview();

      // Referential
      Assert.assertEquals(review.getReviewReferential().getPrioritiesByName(),
        sampleReview.getReviewReferential().getPrioritiesByName());
      Assert.assertEquals(review.getReviewReferential().getUsersByRole(),
        sampleReview.getReviewReferential().getUsersByRole());
      Assert.assertEquals(review.getReviewReferential(),
        sampleReview.getReviewReferential());

      // Items
      Assert.assertEquals(review.getItemsByFilePath(), sampleReview.getItemsByFilePath());

      // Whole review
      Assert.assertEquals(review, sampleReview);
    }
    catch (RevuException e)
    {
      Assert.fail("Review loading failed", e);
    }
  }

  @Test
  public void testSave() throws IOException
  {
    ReviewExternalizerXmlImpl impl = new ReviewExternalizerXmlImpl();
    try
    {
      Review sampleReview = buildSampleReview();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      impl.save(sampleReview, baos);
      String actual = new String(baos.toByteArray());

      String expected = FileUtils.readFully(new InputStreamReader(getClass().getClassLoader()
        .getResourceAsStream("review-sample.xml")));
      // XStream produces Unix line separators
      expected = expected.replaceAll("\\r\n", "\n");

      // Items
      Assert.assertEquals(actual, expected);
    }
    catch (RevuException e)
    {
      Assert.fail("Review saving failed", e);
    }
  }

  private Review buildSampleReview()
  {
    ReviewReferential referential = new ReviewReferential();

    // Users
    User user1 = new User("u1", "p1", "user1", User.Role.ADMIN);
    User user2 = new User("u2", "p2", "user2", User.Role.ADMIN, User.Role.RECIPIENT);
    User user3 = new User("u3", "p3", "user3", User.Role.RECIPIENT);

    referential.setUsers(new HashSet<User>(Arrays.asList(user1, user2, user3)));

    // Priorities
    ReviewPriority priority1 = new ReviewPriority((byte) 1, "priority1");
    ReviewPriority priority2 = new ReviewPriority((byte) 2, "priority2");
    ReviewPriority priority3 = new ReviewPriority((byte) 3, "priority3");
    referential.setPriorities(Arrays.asList(priority1, priority2, priority3));

    referential.consolidate();

    // Review
    Review review = new Review();
    review.setReviewReferential(referential);
    review.setTitle("Test review");
    review.setDesc("A test review. A test review. A test review. A test review. A test review.");
    review.setActive(true);
    review.setHistory(createHistory(referential, 0, 1));

    // Items
    review.setItems(Arrays.asList(createReviewItem(referential, 1),
      createReviewItem(referential, 2)));

    return review;
  }

  private ReviewItem createReviewItem(ReviewReferential referential, int i)
  {
    ReviewItem item = new ReviewItem();

    item.setFilePath("Test-" + i + ".java");
    item.setLineStart(i);
    item.setLineEnd(i * i + 1);
    item.setPriority(
      referential.getPriority("priority" + i % referential.getPrioritiesByName().size()));
    item.setStatus(ReviewItem.Status.TO_RESOLVE);
    item.setDesc("Test item review " + i + ".");
    item.setHistory(createHistory(referential, i, i + 1));

    return item;
  }

  private History createHistory(ReviewReferential referential, int createdByNb, int lastUpdatedByNb)
  {
    createdByNb = createdByNb % referential.getUsersByLogin().size() + 1;
    lastUpdatedByNb = lastUpdatedByNb % referential.getUsersByLogin().size() + 1;

    History history = new History();

    history.setCreatedBy(referential.getUser("u" + createdByNb));
    history.setLastUpdatedBy(referential.getUser("u" + lastUpdatedByNb));

    try
    {
      DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.ENGLISH);
      history.setCreatedOn(dateFormatter.parse("0" + (createdByNb + 1) + "/01/2008").getTime());
      history
        .setLastUpdatedOn(dateFormatter.parse("0" + (lastUpdatedByNb + 1) + "/01/2008").getTime());
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }

    return history;
  }
}
