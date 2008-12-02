package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImportDataReferentialDialog extends DialogWrapper
{
  private JPanel contentPane;
  private JTextField tfFile;
  private JButton bnFileChooser;
  private JRadioButton rbTypeCopy;
  private JRadioButton rbTypeLink;
  private ReviewFileChooser fileChooser;
  private FileTextField fileTextField;
  private final Project project;

  public ImportDataReferentialDialog(final Project project, final ReviewFileChooser fileChooser)
  {
    super(project, false);
    this.project = project;
    this.fileChooser = fileChooser;

    bnFileChooser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VirtualFile defaultFile = (tfFile.getText().length() == 0)
          ? null : RevuUtils.findFile(tfFile.getText());

        VirtualFile vFile = fileChooser.selectFileToOpen(defaultFile);
        if (vFile != null)
        {
          tfFile.setText(vFile.getPath());
        }
      }
    });

    init();
    setTitle(RevuBundle.message("settings.project.review.referential.importDialog.title"));
    pack();
  }

  protected JComponent createCenterPanel()
  {
    return contentPane;
  }

  @Override
  public JComponent getPreferredFocusedComponent()
  {
    return tfFile;
  }

  private void createUIComponents()
  {
    fileTextField = FileChooserFactory.getInstance().createFileTextField(fileChooser.getDescriptor(), false,
      myDisposable);
    tfFile = fileTextField.getField();

    // @TODO listener for isOKActionEnabled
  }

  @Override
  protected Action[] createActions()
  {
    return new Action[]{getOKAction()};
  }

  @Override
  public boolean isOKActionEnabled()
  {
    return ((fileTextField.getSelectedFile() != null) && (fileTextField.getSelectedFile().getParent().exists()));
  }

  public VirtualFile getSelectedFile()
  {
    return fileTextField.getSelectedFile();
  }

  public boolean isLink()
  {
    return rbTypeLink.isSelected();
  }
}
