package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.ReferentialTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewSettingsForm extends AbstractUpdatableForm<Review>
{
  private final Project project;
  private JTabbedPane tabbedPane;
  private HistoryForm historyForm;
  private JTextField tfTitle;
  private JTextField tfFile;
  private JButton bnFileChooser;
  private JPanel contentPane;
  private JCheckBox ckActive;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private FileChooserDescriptor fileChooserDescriptor;

  public ReviewSettingsForm(@NotNull final Project project)
  {
    this.project = project;

    fileChooserDescriptor = new FileChooserDescriptor(true, true, false, false, false, false);
    bnFileChooser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VirtualFile defaultFile = null;
        if (tfFile.getText().length() > 0)
        {
          defaultFile = RevuUtils.findFile(tfFile.getText());
        }

        if (defaultFile == null)
        {
          defaultFile = project.getBaseDir();
        }

        VirtualFile[] virtualFiles = FileChooser.chooseFiles((Component) e.getSource(),
          fileChooserDescriptor, defaultFile);

        if (virtualFiles.length > 0)
        {
          String path;
          VirtualFile vFile = virtualFiles[0];
          if (vFile.isDirectory())
          {
            path = vFile.getPath() + File.separator + tfFile.getText() + ".xml";
          }
          else
          {
            path = vFile.getPath();
          }

          tfFile.setText(path);
        }
      }
    });
  }

  @Override
  public void dispose()
  {
    historyForm.dispose();
    referentialForm.dispose();
  }

  public boolean isModified(@NotNull Review data)
  {
    if (!checkEquals(tfTitle.getText(), data.getTitle()))
    {
      return true;
    }

    if (!checkEquals(ckActive.isSelected(), data.isActive()))
    {
      return true;
    }

    if (!checkEquals(ckShare.isSelected(), data.isShared()))
    {
      return true;
    }

    File file = new File(tfFile.getText());

    if (!checkEquals(file, data.getFile()))
    {
      return true;
    }

    if (referentialForm.isModified(data.getDataReferential()))
    {
      return true;
    }

    return false;
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfTitle, "".equals(tfTitle.getText().trim()));

    // @TODO check file path
    updateRequiredError(tfFile, "".equals(tfFile.getText().trim()));
  }

  protected void internalUpdateUI(Review data)
  {
    tfTitle.setText(this.data.getTitle());
    tfFile.setText((this.data.getFile() == null) ? "" : this.data.getFile().getPath());
    ckActive.setSelected(this.data.isActive());
    ckShare.setSelected(this.data.isShared());

    referentialForm.updateUI(this.data.getDataReferential());
    historyForm.updateUI(this.data);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setTitle(tfTitle.getText());
    data.setFile(new File(tfFile.getText()));
    data.setActive(ckActive.isSelected());
    data.setShared(ckShare.isSelected());

    referentialForm.updateData(data.getDataReferential());
    historyForm.updateData(data);

    data.getHistory().setLastUpdatedBy(data.getDataReferential().getUser(RevuUtils.getCurrentUserLogin()));
    data.getHistory().setLastUpdatedOn(new Date());
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfTitle;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  private void createUIComponents()
  {
    referentialForm = new ReferentialTabbedPane(project);
    tfFile = FileChooserFactory.getInstance().createFileTextField(fileChooserDescriptor, false, this).getField();
  }

}
