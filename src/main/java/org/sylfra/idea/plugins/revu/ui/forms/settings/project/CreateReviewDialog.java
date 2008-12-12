package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
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
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
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
  private JComboBox cbReviewCopy;
  private JComboBox cbReviewLink;
  private JTextField tfName;
  private JTextField tfFile;
  private JButton bnFileChooser;
  private JLabel lbTitle;
  private JLabel lbFile;
  private FileTextField fileTextField;
  private Review currentReview;
  private String currentPath;

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
    tfFile.setText(project.getBaseDir().getPath());
  }

  private void configureUI(boolean createMode)
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
        setOKActionEnabled((tfName.getText().trim().length() > 0)
          && (tfFile.getText().trim().length() > 0)
          && (fileTextField.getSelectedFile() != null)
          && (fileTextField.getSelectedFile().isDirectory()));
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
      ? "settings.project.review.importDialog.create.title"
      : "settings.project.review.importDialog.update.title"));

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
  public String getReviewPath()
  {
    return currentPath;
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

  public void show(@NotNull java.util.List<Review> currentReviews, @Nullable Review review)
  {
    currentReview = review;

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    RevuAppSettings appSettings =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class).getState();

    java.util.List<Review> reviews = new ArrayList<Review>(
      reviewManager.getReviews(currentReviews, true, null, appSettings.getLogin()));

    CollectionComboBoxModel cbModel = new CollectionComboBoxModel(reviews, reviews.get(0));

    cbReviewCopy.setModel(cbModel);
    cbReviewLink.setModel(cbModel);

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
      try
      {
        reviewManager.checkCyclicLink(currentReview, getImportedReview());
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
    // Creation
    else
    {
      // Name already exists
      if (reviewManager.getReviewByName(tfName.getText()) != null)
      {
        setErrorText(RevuBundle.message("settings.project.review.importDialog.nameAlreadyExists.text"));
        return;
      }
    }

    String fileName = RevuUtils.buildFileNameFromReviewName(getReviewName());
    File file = new File(fileTextField.getSelectedFile().getPath(), fileName);
    currentPath = RevuVfsUtils.buildAbsolutePath(file);
    if (file.exists())
    {
      // Don't provide path as msg arg since error label height is fixed and is not appropriate for
      // 2 lines
      setErrorText(RevuBundle.message("settings.project.review.fileAlreadyExists.text"));
      return;
    }

    super.doOKAction();
  }

}
