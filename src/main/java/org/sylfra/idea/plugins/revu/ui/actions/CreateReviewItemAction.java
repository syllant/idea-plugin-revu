package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemDialog;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateReviewItemAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Editor editor = e.getData(DataKeys.EDITOR);
    VirtualFile virtualFile = e.getData(DataKeys.VIRTUAL_FILE);
    RevuPlugin revuPlugin = ServiceManager.getService(project, RevuPlugin.class);

    History history = new History();
    long now = System.currentTimeMillis();
    history.setCreatedBy(revuPlugin.getUser());
    history.setCreatedOn(now);
    history.setLastUpdatedBy(revuPlugin.getUser());
    history.setLastUpdatedOn(now);

    ReviewItem item = new ReviewItem();
    item.setHistory(history);
    item.setFile(virtualFile);
    item.setLineStart(editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1);
    item.setLineEnd(editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1);
    item.setStatus(ReviewItem.Status.TO_RESOLVE);

    ReviewItemDialog dialog = new ReviewItemDialog(project);
    dialog.show(item, true);
    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE)
    {
      dialog.updateData(item);
      item.getReview().addItem(item);
    }
  }
}
