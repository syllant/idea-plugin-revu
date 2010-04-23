package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarComponent;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarMessage;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class CreateReviewDialog extends DialogWrapper
{
  public static enum ImportType
  {
    BLANK,
    COPY,
    LINK
  }

  private final Project project;
  private final boolean createMode;
  private ReviewFileChooser fileChooser;
  private JPanel contentPane;
  private JRadioButton rbTypeBlank;
  private JRadioButton rbTypeCopy;
  private JRadioButton rbTypeLink;
  private ComboBox cbReviewCopy;
  private ComboBox cbReviewLink;
  private JTextField tfName;
  private JTextField tfFile;
  private JButton bnFileChooser;
  private JLabel lbTitle;
  private JLabel lbFile;
  private FileTextField fileTextField;
  private Review currentReview;
  private File currentFile;

  public CreateReviewDialog(final Project project, boolean createMode)
  {
    super(project, false);
    this.project = project;
    this.createMode = createMode;

    configureUI(createMode);
  }

  private void createUIComponents()
  {
    fileChooser = new ReviewFileChooser(project);
    fileTextField = FileChooserFactory.getInstance().createFileTextField(
      fileChooser.getDescriptor(), false, myDisposable);
    tfFile = fileTextField.getField();
  }

  private void configureUI(final boolean createMode)
  {
    bnFileChooser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VirtualFile defaultFile = (tfFile.getText().length() == 0)
          ? null : RevuVfsUtils.findFile(tfFile.getText());

        VirtualFile vFile = fileChooser.selectFileToSave(defaultFile);

        if (vFile != null)
        {
          tfFile.setText(vFile.getPath());
        }
      }
    });

    DefaultListCellRenderer comboRenderer = createComboReviewRenderer();
    cbReviewCopy.setRenderer(comboRenderer);
    cbReviewLink.setRenderer(comboRenderer);

    DocumentAdapter textFieldsListener = new DocumentAdapter()
    {
      public void textChanged(DocumentEvent event)
      {
        setOKActionEnabled((!createMode)
        || ((tfName.getText().trim().length() > 0)
          && (tfFile.getText().trim().length() > 0)
          && (fileTextField.getSelectedFile() != null)
          && (fileTextField.getSelectedFile().isDirectory())));
      }
    };
    tfName.getDocument().addDocumentListener(textFieldsListener);
    tfFile.getDocument().addDocumentListener(textFieldsListener);

    rbTypeBlank.setVisible(createMode);
    rbTypeBlank.setSelected(createMode);
    rbTypeCopy.setSelected(!createMode);
    lbTitle.setVisible(createMode);
    tfName.setVisible(createMode);
    lbFile.setVisible(createMode);
    tfFile.setVisible(createMode);
    bnFileChooser.setVisible(createMode);

    cbReviewCopy.setEnabled(!createMode);
    cbReviewLink.setEnabled(false);

    ActionListener radioTypeListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cbReviewLink.setEnabled(rbTypeLink.isSelected());
        cbReviewCopy.setEnabled(rbTypeCopy.isSelected());
      }
    };
    rbTypeBlank.addActionListener(radioTypeListener);
    rbTypeCopy.addActionListener(radioTypeListener);
    rbTypeLink.addActionListener(radioTypeListener);

    setOKActionEnabled(!createMode);
    setTitle(RevuBundle.message(createMode
      ? "projectSettings.review.importDialog.create.title"
      : "projectSettings.review.importDialog.update.title"));

    init();
    pack();
  }

  private DefaultListCellRenderer createComboReviewRenderer()
  {
    return new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        value = ((Review) value).getName();
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    };
  }

  protected JComponent createCenterPanel()
  {
    return contentPane;
  }

  @Override
  public JComponent getPreferredFocusedComponent()
  {
    return createMode ? tfName : rbTypeCopy;
  }

  @NotNull
  public String getReviewName()
  {
    return tfName.getText();
  }

  @NotNull
  public File getReviewFile()
  {
    return currentFile;
  }

  @Nullable
  public Review getImportedReview()
  {
    if (rbTypeCopy.isSelected())
    {
      return (Review) cbReviewCopy.getSelectedItem();
    }

    if (rbTypeLink.isSelected())
    {
      return (Review) cbReviewLink.getSelectedItem();
    }

    return null;
  }

  public ImportType getImportType()
  {
    if (rbTypeCopy.isSelected())
    {
      return ImportType.COPY;
    }

    if (rbTypeLink.isSelected())
    {
      return ImportType.LINK;
    }

    return ImportType.BLANK;
  }

  public void show(@NotNull Collection<Review> currentReviews, @Nullable Review review)
  {
    currentReview = review;

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    java.util.List<Review> reviews = new ArrayList<Review>(
      reviewManager.getReviews(currentReviews, null, ReviewStatus.DRAFT, ReviewStatus.FIXING, ReviewStatus.REVIEWING,
        ReviewStatus._TEMPLATE));

    reviews.remove(review);
    CollectionComboBoxModel cbModel = new CollectionComboBoxModel(reviews, reviews.get(0));

    cbReviewCopy.setModel(cbModel);
    cbReviewLink.setModel(cbModel);

    String dir = RevuUtils.getWorkspaceSettings(project).getLastSelectedReviewDir();
    if (dir == null)
    {
      dir = project.getBaseDir().getPath();
    }
    tfFile.setText(dir);

    super.show();
  }

  @Override
  protected void doOKAction()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    // Update
    if (currentReview != null)
    {
      // Cyclic link
      Review importedReview = getImportedReview();
      if (importedReview != null)
      {
        try
        {
          reviewManager.checkCyclicLink(currentReview, importedReview);
        }
        catch (RevuException exception)
        {
          String errorTitle = RevuBundle.message("friendlyError.externalizing.cyclicReview.title.text");
          setErrorText(errorTitle);

          StatusBarComponent.showMessageInPopup(project, (new StatusBarMessage(StatusBarMessage.Type.ERROR, errorTitle,
            exception.getMessage())), false);
          return;
        }
      }
    }
    // Creation
    else
    {
      // Name already exists
      if (reviewManager.getReviewByName(tfName.getText()) != null)
      {
        setErrorText(RevuBundle.message("projectSettings.review.importDialog.nameAlreadyExists.text"));
        return;
      }
    }

    String fileName = RevuUtils.buildFileNameFromReviewName(getReviewName());
    VirtualFile vFile = fileTextField.getSelectedFile();

    // @TODO check vFile is null ?
    File file = new File(vFile.getPath(), fileName);
    if (file.exists())
    {
      // Don't provide path as msg arg since error label height is fixed and is not appropriate for 2 lines
      setErrorText(RevuBundle.message("projectSettings.review.fileAlreadyExists.text"));
      return;
    }

    currentFile = file;

    RevuWorkspaceSettingsComponent workspaceSettingsComponent
      = project.getComponent(RevuWorkspaceSettingsComponent.class);
    RevuWorkspaceSettings state = workspaceSettingsComponent.getState();
    state.setLastSelectedReviewDir(vFile.getPath());
    workspaceSettingsComponent.loadState(state);

    super.doOKAction();
  }

}
