package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.testFramework.IdeaTestCase;
import org.apache.tools.ant.util.FileUtils;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.model.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizerXmlImplTest extends IdeaTestCase
{
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    checkTestFilesExists("Test-1.java", "Test-2.java");
  }

  private void checkTestFilesExists(String... fileNames)
    throws IOException
  {
    for (String fileName : fileNames)
    {
      File f = new File(myProject.getBaseDir().getPath(), fileName);
      f.deleteOnExit();
      if (LocalFileSystem.getInstance().findFileByIoFile(f) == null)
      {
        LocalFileSystem.getInstance().createChildFile(null, myProject.getBaseDir(), fileName);
      }
    }
  }

  public void testLoad()
  {
    ReviewExternalizerXmlImpl impl = new ReviewExternalizerXmlImpl(getProject());
    try
    {
      Review review =
        impl.load(getClass().getClassLoader().getResourceAsStream("/review-test-unit.xml"));

      Review sampleReview = buildSampleReview();

      // Referential
      assertEquals(review.getReviewReferential().getPrioritiesByName(),
        sampleReview.getReviewReferential().getPrioritiesByName());
      assertEquals(review.getReviewReferential().getUsersByRole(),
        sampleReview.getReviewReferential().getUsersByRole());
      assertEquals(review.getReviewReferential(),
        sampleReview.getReviewReferential());

      // Items
      assertEquals(review.getItemsByFiles(), sampleReview.getItemsByFiles());

      // Whole review
      assertEquals(review, sampleReview);
    }
    catch (RevuException e)
    {
      fail("Review loading failed");
    }
  }

  public void testSave() throws IOException
  {
    ReviewExternalizerXmlImpl impl = new ReviewExternalizerXmlImpl(getProject());
    try
    {
      Review sampleReview = buildSampleReview();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      impl.save(sampleReview, baos);
      String actual = new String(baos.toByteArray());

      String expected = FileUtils.readFully(new InputStreamReader(getClass().getClassLoader()
        .getResourceAsStream("/review-test-unit.xml")));
      // XStream produces Unix line separators
      expected = expected.replaceAll("\\r\n", "\n");

      // Items
      assertEquals(actual, expected);
    }
    catch (RevuException e)
    {
      fail("Review saving failed");
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
    referential.setPriorities(new HashSet<ReviewPriority>(
      Arrays.asList(priority1, priority2, priority3)));

    // Categories
    ReviewCategory category1 = new ReviewCategory("category1");
    ReviewCategory category2 = new ReviewCategory("category2");
    referential.setCategories(new HashSet<ReviewCategory>(
      Arrays.asList(category1, category2)));

    // Review
    Review review = new Review();
    review.setReviewReferential(referential);
    review.setTitle("Test review");
    review.setDesc("A test review. A test review. A test review. A test review. A test review.");
    review.setActive(true);
    review.setHistory(createHistory(referential, 0, 1));

    // Items
    review.setItems(Arrays.asList(createReviewItem(review, 1),
      createReviewItem(review, 2)));

    return review;
  }

  private ReviewItem createReviewItem(Review review, int i)
  {
    ReviewReferential referential = review.getReviewReferential();
    ReviewItem item = new ReviewItem();
    item.setReview(review);

    item.setFile(getVirtualFile(new File(myProject.getBaseDir().getPath(), "Test-" + i + ".java")));
    item.setLineStart(i);
    item.setLineEnd(i * i + 1);
    item.setPriority(referential.getPriority("priority" + i % referential.getPrioritiesByName().size()));
    item.setCategory(referential.getCategory("category" + i % referential.getCategoriesByName().size()));
    item.setStatus(ReviewItem.Status.TO_RESOLVE);
    item.setDesc("Test item review " + i + ". Test item review " + i + ".");
    item.setTitle("Test item review " + i + ".");
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
