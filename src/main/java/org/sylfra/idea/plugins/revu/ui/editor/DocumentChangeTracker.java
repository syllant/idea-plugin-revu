package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;

import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
class DocumentChangeTracker implements DocumentListener
{
  private static final Key<Object> ORPHAN_MARKER_KEY = Key.create("revu.OrhpanMarker");

  private final Document document;
  private final Set<Editor> editors;
  private final Map<Issue, RangeMarker> markers;

  public DocumentChangeTracker(Document document)
  {
    this.document = document;

    editors = new HashSet<Editor>();
    markers = new IdentityHashMap<Issue, RangeMarker>();

    document.addDocumentListener(this);
  }

  @NotNull
  Set<Editor> getEditors()
  {
    return editors;
  }

  @NotNull
  RangeMarker addMarker(@NotNull Issue issue, boolean orphanMarker)
  {
    int lineStart = (issue.getLineStart() == -1) ? 0 : issue.getLineStart();
    int lineEnd = (issue.getLineEnd() == -1) ? 0 : issue.getLineEnd();

    RangeMarker marker = document.createRangeMarker(document.getLineStartOffset(lineStart),
      document.getLineEndOffset(lineEnd));

    marker.putUserData(ORPHAN_MARKER_KEY, orphanMarker);
    markers.put(issue, marker);

    return marker;
  }

  @Nullable
  RangeMarker getMarker(@NotNull Issue issue)
  {
    return markers.get(issue);
  }

  void removeMarker(@NotNull Issue issue)
  {
    markers.remove(issue);
  }

  boolean isOrphanMarker(RangeMarker marker)
  {
    return Boolean.TRUE.equals(marker.getUserData(ORPHAN_MARKER_KEY));
  }

  public void beforeDocumentChange(DocumentEvent event)
  {
  }

  public void documentChanged(DocumentEvent event)
  {
    int lineStart = document.getLineNumber(event.getOffset());

    updateIssues(lineStart);
  }

  private void updateIssue(@NotNull Issue issue, @NotNull RangeMarker marker)
  {
    if ((marker.isValid()) && (!isOrphanMarker(marker)))
    {
      CharSequence fragment =
        marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset());
      int lineStart = marker.getDocument().getLineNumber(marker.getStartOffset());
      int lineEnd = marker.getDocument().getLineNumber(marker.getEndOffset());

      if (StringUtil.endsWith(fragment, "\n"))
      {
        lineEnd--;
      }

      issue.setLineStart(lineStart);
      issue.setLineEnd(lineEnd);
    }

    issue.getReview().fireIssueUpdated(issue);
  }

  private void updateIssues(int lineStart)
  {
    Map<Issue, RangeMarker> markersCopy = new HashMap<Issue, RangeMarker>(markers);
    for (Map.Entry<Issue, RangeMarker> entry : markersCopy.entrySet())
    {
      Issue issue = entry.getKey();
      if (issue.getLineStart() >= lineStart)
      {
        updateIssue(issue, entry.getValue());
      }
    }
  }

  void release()
  {
    document.removeDocumentListener(this);
  }
}
