package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizerXmlImplTest extends IdeaTestCase
{
  private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

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
      Review review = new Review();
      impl.load(review, getClass().getClassLoader().getResourceAsStream("review-test-unit.xml"));

      Review sampleReview = buildSampleReview();

      // Note: assertions are redundant but allow to debug errors more easily

      // Referential
      assertEquals(review.getDataReferential().getItemPrioritiesByName(),
        sampleReview.getDataReferential().getItemPrioritiesByName());
      assertEquals(review.getDataReferential().getUsersByRole(),
        sampleReview.getDataReferential().getUsersByRole());
      assertEquals(review.getDataReferential().getItemPrioritiesByName(),
        sampleReview.getDataReferential().getItemPrioritiesByName());
      assertEquals(review.getDataReferential().getItemResolutionTypesByName(),
        sampleReview.getDataReferential().getItemResolutionTypesByName());
      assertEquals(review.getDataReferential(),
        sampleReview.getDataReferential());

      // Items
      Map<VirtualFile,List<ReviewItem>> actualItems = review.getItemsByFiles();
      Map<VirtualFile, List<ReviewItem>> expectedItems = sampleReview.getItemsByFiles();
      assertEquals(actualItems.size(), expectedItems.size());
      for (Map.Entry<VirtualFile, List<ReviewItem>> entry : actualItems.entrySet())
      {
        assertEquals(entry.getValue(), expectedItems.get(entry.getKey()));
      }
      assertEquals(actualItems, expectedItems);

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
    DataReferential referential = new DataReferential();

    // Users
    User user1 = new User("u1", "p1", "user1", User.Role.ADMIN);
    User user2 = new User("u2", "p2", "user2", User.Role.ADMIN, User.Role.REVIEWER);
    User user3 = new User("u3", "p3", "user3", User.Role.REVIEWER, User.Role.AUTHOR);

    referential.setUsers(new HashSet<User>(Arrays.asList(user1, user2, user3)));

    // Priorities
    ItemPriority priority1 = new ItemPriority((byte) 1, "priority1");
    ItemPriority priority2 = new ItemPriority((byte) 2, "priority2");
    ItemPriority priority3 = new ItemPriority((byte) 3, "priority3");
    referential.setItemPriorities(new HashSet<ItemPriority>(
      Arrays.asList(priority1, priority2, priority3)));

    // Categories
    ItemCategory category1 = new ItemCategory("category1");
    ItemCategory category2 = new ItemCategory("category2");
    referential.setItemCategories(new HashSet<ItemCategory>(
      Arrays.asList(category1, category2)));

    // Resolution status
    ItemResolutionType itemResolutionType1 = new ItemResolutionType("resolutionType1");
    ItemResolutionType itemResolutionType2 = new ItemResolutionType("resolutionType2");
    referential.setItemResolutionTypes(new HashSet<ItemResolutionType>(
      Arrays.asList(itemResolutionType1, itemResolutionType2)));

    // Review
    Review review = new Review();
    review.setDataReferential(referential);
    review.setTitle("Test review");
    review.setDesc("A test review. A test review. A test review. A test review. A test review.");
    review.setActive(true);
    review.setShared(true);
    review.setHistory(createHistory(referential, 0, 1));

    // Items
    review.setItems(Arrays.asList(createReviewItem(review, 1),
      createReviewItem(review, 2)));

    return review;
  }

  private ReviewItem createReviewItem(Review review, int i)
  {
    DataReferential referential = review.getDataReferential();
    ReviewItem item = new ReviewItem();
    item.setReview(review);

    item.setFile(getVirtualFile(new File(myProject.getBaseDir().getPath(), "Test-" + i + ".java")));
    item.setLineStart(i);
    item.setLineEnd(i * i + 1);
    item.setPriority(referential.getItemPriority("priority" + i % referential.getItemPrioritiesByName().size()));
    item.setCategory(referential.getItemCategory("category" + i % referential.getItemCategoriesByName().size()));
    item.setResolutionStatus(ItemResolutionStatus.TO_RESOLVE);
    item.setResolutionType(referential.getItemResolutionType("resolutionType" + i % referential.getItemResolutionTypesByName().size()));
    item.setDesc("Test item review " + i + ". Test item review " + i + ".");
    item.setSummary("Test item review " + i + ".");
    item.setHistory(createHistory(referential, i, i + 1));

    return item;
  }

  private History createHistory(DataReferential referential, int createdByNb, int lastUpdatedByNb)
  {
    createdByNb = createdByNb % referential.getUsersByLogin().size() + 1;
    lastUpdatedByNb = lastUpdatedByNb % referential.getUsersByLogin().size() + 1;

    History history = new History();

    history.setCreatedBy(referential.getUser("u" + createdByNb));
    history.setLastUpdatedBy(referential.getUser("u" + lastUpdatedByNb));

    try
    {
      history.setCreatedOn(DATE_FORMATTER.parse("2008-0" + (createdByNb - 1) + "-01T12:00:00"));
      history.setLastUpdatedOn(DATE_FORMATTER.parse("2008-0" + (lastUpdatedByNb - 1) + "-02T12:00:00"));
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }

    return history;
  }
}
