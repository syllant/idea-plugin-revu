package org.sylfra.idea.plugins.revu.externalizing.impl;

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
      assertEquals(review.getDataReferential().getItemPrioritiesByName(true),
        sampleReview.getDataReferential().getItemPrioritiesByName(true));
      assertEquals(review.getDataReferential().getUsersByRole(true),
        sampleReview.getDataReferential().getUsersByRole(true));
      assertEquals(review.getDataReferential().getItemPrioritiesByName(true),
        sampleReview.getDataReferential().getItemPrioritiesByName(true));
      assertEquals(review.getDataReferential(),
        sampleReview.getDataReferential());

      // Items
      Map<VirtualFile,List<Issue>> actualItems = review.getItemsByFiles();
      Map<VirtualFile, List<Issue>> expectedItems = sampleReview.getItemsByFiles();
      assertEquals(actualItems.size(), expectedItems.size());
      for (Map.Entry<VirtualFile, List<Issue>> entry : actualItems.entrySet())
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
    // Review
    Review review = new Review();
    DataReferential referential = new DataReferential(review);
    review.setDataReferential(referential);

    review.setName("Test review");
    review.setGoal("A test review. A test review. A test review. A test review. A test review.");
    review.setActive(true);
    review.setShared(true);
    review.setTemplate(false);
    review.setHistory(createHistory(referential, 0, 1));

    // Items
    review.setItems(Arrays.asList(createIssue(review, 1),
      createIssue(review, 2)));

    // Users
    User user1 = new User("u1", "p1", "user1", User.Role.ADMIN);
    User user2 = new User("u2", "p2", "user2", User.Role.ADMIN, User.Role.REVIEWER);
    User user3 = new User("u3", "p3", "user3", User.Role.REVIEWER, User.Role.AUTHOR);

    referential.setUsers(new HashSet<User>(Arrays.asList(user1, user2, user3)));

    // Priorities
    IssuePriority priority1 = new IssuePriority((byte) 1, "priority1");
    IssuePriority priority2 = new IssuePriority((byte) 2, "priority2");
    IssuePriority priority3 = new IssuePriority((byte) 3, "priority3");
    referential.setItemPriorities(new HashSet<IssuePriority>(
      Arrays.asList(priority1, priority2, priority3)));

    // Tags
    IssueTag tag1 = new IssueTag("tag1");
    IssueTag tag2 = new IssueTag("tag2");
    referential.setItemTags(new HashSet<IssueTag>(
      Arrays.asList(tag1, tag2)));

    return review;
  }

  private Issue createIssue(Review review, int i)
  {
    DataReferential referential = review.getDataReferential();
    Issue item = new Issue();
    item.setReview(review);

    item.setFile(getVirtualFile(new File(myProject.getBaseDir().getPath(), "Test-" + i + ".java")));
    item.setLineStart(i);
    item.setLineEnd(i * i + 1);
    item.setPriority(referential.getItemPriority("priority" + i % referential.getItemPrioritiesByName(true).size()));
    item.setStatus(IssueStatus.TO_RESOLVE);
    item.setDesc("Test item review " + i + ". Test item review " + i + ".");
    item.setSummary("Test item review " + i + ".");
    item.setHistory(createHistory(referential, i, i + 1));

    item.setTags(Arrays.asList(referential.getItemTag("tag" + i % referential.getItemTagsByName(true).size())));

    return item;
  }

  private History createHistory(DataReferential referential, int createdByNb, int lastUpdatedByNb)
  {
    createdByNb = createdByNb % referential.getUsersByLogin(true).size() + 1;
    lastUpdatedByNb = lastUpdatedByNb % referential.getUsersByLogin(true).size() + 1;

    History history = new History();

    history.setCreatedBy(referential.getUser("u" + createdByNb, true));
    history.setLastUpdatedBy(referential.getUser("u" + lastUpdatedByNb, true));

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
