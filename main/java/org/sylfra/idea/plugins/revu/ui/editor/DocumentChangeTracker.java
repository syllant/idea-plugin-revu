package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
class DocumentChangeTracker implements DocumentListener
{
  private static final Key<Object> ORPHAN_MARKER_KEY = Key.create("revu.OrhpanMarker");

  private final VirtualFile vFile;
  private final Document document;
  private final Set<Editor> editors;
  private final Map<Issue, RangeMarker> markers;
  private RevuEditorHandler revuEditorHandler;

  public DocumentChangeTracker(RevuEditorHandler revuEditorHandler, VirtualFile vFile, Document document)
  {
    this.revuEditorHandler = revuEditorHandler;
    this.vFile = vFile;
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
    RangeMarker marker = document.createRangeMarker(document.getLineStartOffset(issue.getLineStart()),
      document.getLineEndOffset(issue.getLineEnd()));

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

    updateItems(lineStart);
  }

  private void updateIssue(@NotNull Issue item, @NotNull RangeMarker marker)
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

      item.setLineStart(lineStart);
      item.setLineEnd(lineEnd);
    }

    item.getReview().fireItemUpdated(item);
  }

  private void updateItems(int lineStart)
  {
    for (Map.Entry<Issue, RangeMarker> entry : markers.entrySet())
    {
      Issue item = entry.getKey();
      RangeMarker marker = entry.getValue();
      if (item.getLineStart() >= lineStart)
      {
        updateIssue(item, marker);
      }
    }
  }

  void release()
  {
    document.removeDocumentListener(this);
  }
}
