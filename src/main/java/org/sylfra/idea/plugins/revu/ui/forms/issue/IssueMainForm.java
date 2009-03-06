package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.editor.RevuEditorHandler;
import org.sylfra.idea.plugins.revu.ui.multichooser.MultiChooserPanel;
import org.sylfra.idea.plugins.revu.ui.multichooser.UniqueNameMultiChooserItem;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueMainForm extends AbstractIssueForm
{
  private final boolean createMode;
  private final boolean inDialog;
  private MultiChooserPanel<IssueTag, UniqueNameMultiChooserItem<IssueTag>> tagsMultiChooserPanel;
  private JPanel contentPane;
  private JTextArea taDesc;
  private JComboBox cbPriority;
  private JTextArea taSummary;
  private JLabel lbLocation;
  private JComboBox cbReview;
  private JRadioButton rbLocationFile;
  private JRadioButton rbLocationGlobal;
  private JRadioButton rbLocationLineRange;
  private JLabel lbSync;
  private JLabel lbTags;
  private JLabel lbReview;
  private JPanel pnReview;
  private ButtonGroup bgLocation;

  public IssueMainForm(@NotNull Project project, boolean createMode, boolean inDialog)
  {
    super(project);
    this.createMode = createMode;
    this.inDialog = inDialog;
    $$$setupUI$$$();
    configureUI();
  }

  private void createUIComponents()
  {
    lbTags = new JLabel();
    tagsMultiChooserPanel = new MultiChooserPanel<IssueTag, UniqueNameMultiChooserItem<IssueTag>>(project, lbTags,
      RevuBundle.message("issueForm.tagsPopup.title"),
      "TagsChooser" + (inDialog ? "Dialog" : ""), RevuIconProvider.IconRef.TAG)
    {
      protected UniqueNameMultiChooserItem<IssueTag> createMultiChooserItem(@NotNull IssueTag issueTag)
      {
        return new UniqueNameMultiChooserItem<IssueTag>(issueTag);
      }

      @Override
      protected List<IssueTag> retrieveAllAvailableElements()
      {
        Review review = (createMode) ? (Review) cbReview.getSelectedItem() : currentIssue.getReview();

        return review.getDataReferential().getIssueTags(true);
      }
    };
  }

  private void configureUI()
  {
    RevuUtils.configureTextAreaAsStandardField(taDesc, taSummary);

    ((CardLayout) pnReview.getLayout()).show(pnReview, createMode ? "combo" : "label");

    if (createMode)
    {
      cbReview.setModel(new ReviewComboBoxModel(project));
      cbReview.setRenderer(new DefaultListCellRenderer()
      {
        public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus)
        {
          String tooltip;
          if (value == null)
          {
            tooltip = null;
            value = RevuBundle.message("general.selectComboValue.text");
          }
          else
          {
            Review review = (Review) value;
            tooltip = review.getPath();
            value = review.getName();
          }

          JComponent result = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);

          if (tooltip != null)
          {
            result.setToolTipText(tooltip);
          }

          return result;
        }
      });

      cbReview.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          Object selectedReview = cbReview.getSelectedItem();
          if (selectedReview != null)
          {
            DataReferential referential = ((Review) selectedReview).getDataReferential();

            cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(
              new TreeSet<IssuePriority>(referential.getIssuePrioritiesByName(true).values()), true)));

            tagsMultiChooserPanel.setEnabled(true);
          }
          else
          {
            // "[Select a value]" String is selected
            cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));

            tagsMultiChooserPanel.setEnabled(false);
          }
        }
      });
    }

    cbPriority.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message(((createMode) && (cbReview.getSelectedItem() == null))
            ? "general.selectReviewBeforeFillingCombo.text" : "general.selectComboValue.text");
        }
        else
        {
          value = ((IssuePriority) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    ActionListener locationTypeListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Issue.LocationType locationType = (rbLocationGlobal.isSelected())
          ? Issue.LocationType.GLOBAL
          : ((rbLocationFile.isSelected()) ? Issue.LocationType.FILE : Issue.LocationType.LINE_RANGE);
        updateLocation(locationType);
      }
    };
    rbLocationGlobal.addActionListener(locationTypeListener);
    rbLocationFile.addActionListener(locationTypeListener);
    rbLocationLineRange.addActionListener(locationTypeListener);

    lbSync.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        assert currentIssue != null;

        RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
        currentIssue.setHash(editorHandler.buildNewHash(currentIssue));
        currentIssue.getReview().fireIssueUpdated(currentIssue);
      }
    });
  }

  public JComponent getPreferredFocusedComponent()
  {
    return ((currentIssue == null) || (currentIssue.getReview() == null)) ? cbReview : taSummary;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    super.internalUpdateUI(data, requestFocus);

    if (createMode)
    {
      ReviewComboBoxModel reviewComboBoxModel = (ReviewComboBoxModel) cbReview.getModel();
      reviewComboBoxModel.updateReviews();

      // Select default review if only one exists (but combo as 2 items with [Select ...])
      Review defaultReview = ((data == null) || (data.getReview() == null))
        ? (Review) ((reviewComboBoxModel.getSize() == 2) ? reviewComboBoxModel.getElementAt(1) : null)
        : data.getReview();

      cbReview.setSelectedItem(defaultReview);
    }
    else
    {
      if ((data != null) && (data.getReview() != null))
      {
        cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(
          new TreeSet<IssuePriority>(
            currentIssue.getReview().getDataReferential().getIssuePrioritiesByName(true).values()), true)));
        lbReview.setText(RevuBundle.message("issueForm.main.review.text", data.getReview().getName(),
          RevuUtils.buildReviewStatusLabel(data.getReview().getStatus())));
      }
    }

    taDesc.setText((data == null) ? "" : data.getDesc());
    taSummary.setText((data == null) ? "" : data.getSummary());
    cbPriority.setSelectedItem((data == null) ? null : data.getPriority());
    lbSync.setVisible((data != null) && (!isIssueSynchronized(data)));
    tagsMultiChooserPanel.setSelectedItemDatas((data == null) ? null : data.getTags());
    tagsMultiChooserPanel.setEnabled((!createMode) || (cbReview.getSelectedItem() != null));

    Issue.LocationType locationType = (data == null) ? null : data.getLocationType();
    updateLocation(locationType);
  }

  protected void internalUpdateData(@NotNull Issue data)
  {
    if (createMode)
    {
      data.setReview((Review) cbReview.getSelectedItem());
    }

    data.setDesc(taDesc.getText());
    data.setSummary(taSummary.getText());
    data.setPriority((IssuePriority) cbPriority.getSelectedItem());
    data.setTags(tagsMultiChooserPanel.getSelectedItemDatas());

    // Location
    if (rbLocationGlobal.isSelected())
    {
      data.setFile(null);
      data.setLineStart(-1);
      data.setLineEnd(-1);
    }
    else if (rbLocationFile.isSelected())
    {
      data.setLineStart(-1);
      data.setLineEnd(-1);
    }
  }

  public boolean isModified(@NotNull Issue data)
  {
    if (!checkEquals(taDesc.getText(), data.getDesc()))
    {
      return true;
    }

    if (!checkEquals(taSummary.getText(), data.getSummary()))
    {
      return true;
    }

    if ((createMode) && (!checkEquals(cbReview.getSelectedItem(), data.getReview())))
    {
      return true;
    }

    if (!checkEquals(cbPriority.getSelectedItem(), data.getPriority()))
    {
      return true;
    }

    if (!checkEquals(tagsMultiChooserPanel.getSelectedItemDatas(), data.getTags()))
    {
      return true;
    }

    if ((bgLocation.getSelection() != null)
      && (!checkEquals(bgLocation.getSelection().getActionCommand(), data.getLocationType().toString())))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    boolean mayReview = (((createMode) || (user != null))
      && ((currentIssue == null) || (currentIssue.getReview() == null)
      || (IssueStatus.CLOSED != currentIssue.getStatus())));
    RevuUtils.setWriteAccess(mayReview, cbPriority, taSummary, taDesc, rbLocationGlobal);

    Issue.LocationType locationType = (currentIssue == null) ? null : currentIssue.getLocationType();
    rbLocationFile.setEnabled(mayReview && !Issue.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(mayReview && !Issue.LocationType.GLOBAL.equals(locationType)
      && !Issue.LocationType.FILE.equals(locationType));

    tagsMultiChooserPanel.setEnabled(mayReview && ((!createMode) || (cbReview.getSelectedIndex() > 0)));
  }

  public void internalValidateInput()
  {
    updateRequiredError(taSummary, "".equals(taSummary.getText().trim()));
    if (createMode)
    {
      updateRequiredError(cbReview, (!(cbReview.getSelectedItem() instanceof Review)));

      // Check is user is declared in selected review
      if (cbReview.getSelectedItem() instanceof Review)
      {
        Review review = (Review) cbReview.getSelectedItem();

        RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();
        User user = review.getDataReferential().getUser(appSettings.getLogin(), true);

        updateRequiredError(cbReview, user == null);
      }
    }
  }

  private boolean isIssueSynchronized(Issue issue)
  {
    RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
    return editorHandler.isSynchronized(issue, true);
  }

  private void updateLocation(@Nullable Issue.LocationType locationType)
  {
    if (locationType == null)
    {
      rbLocationGlobal.setSelected(true);
      lbLocation.setText("");
    }

    String filePath;
    String locationPath;
    switch (locationType)
    {
      case GLOBAL:
        rbLocationGlobal.setSelected(true);
        locationPath = RevuBundle.message("issueForm.main.location.global.text");
        break;

      case FILE:
        rbLocationFile.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentIssue.getFile());
        locationPath = (currentIssue.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("issueForm.main.location.pathWithVcsRev.text", filePath,
          currentIssue.getVcsRev());
        break;

      default:
        rbLocationLineRange.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentIssue.getFile());
        String filePathWithVcsRev = (currentIssue.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("issueForm.main.location.pathWithVcsRev.text", filePath,
          currentIssue.getVcsRev());
        locationPath = RevuBundle.message("issueForm.main.location.range.path.text",
          filePathWithVcsRev, (currentIssue.getLineStart() + 1), (currentIssue.getLineEnd() + 1));
    }

    lbLocation.setText(locationPath);
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$()
  {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
    final JLabel label1 = new JLabel();
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.main.desc.label"));
    label1.setVerticalAlignment(0);
    label1.setVerticalTextPosition(0);
    contentPane.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setFont(new Font(label2.getFont().getName(), Font.BOLD, label2.getFont().getSize()));
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.title.label"));
    contentPane.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    this.$$$loadLabelText$$$(lbTags,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("issueForm.main.tag.label"));
    contentPane.add(lbTags, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setFont(new Font(label3.getFont().getName(), Font.BOLD, label3.getFont().getSize()));
    this.$$$loadLabelText$$$(label3,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.review.label"));
    contentPane.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setFont(new Font(label4.getFont().getName(), Font.BOLD, label4.getFont().getSize()));
    this.$$$loadLabelText$$$(label4,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.location.label"));
    label4.setVerticalTextPosition(0);
    contentPane.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), 0, 0));
    contentPane.add(panel1,
      new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    rbLocationGlobal = new JRadioButton();
    rbLocationGlobal.setActionCommand("GLOBAL");
    this.$$$loadButtonText$$$(rbLocationGlobal,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.location.global.text"));
    rbLocationGlobal.setVerticalAlignment(1);
    panel2.add(rbLocationGlobal);
    rbLocationFile = new JRadioButton();
    rbLocationFile.setActionCommand("FILE");
    this.$$$loadButtonText$$$(rbLocationFile,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.location.file.text"));
    panel2.add(rbLocationFile);
    rbLocationLineRange = new JRadioButton();
    rbLocationLineRange.setActionCommand("LINE_RANGE");
    this.$$$loadButtonText$$$(rbLocationLineRange,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.location.range.text"));
    panel2.add(rbLocationLineRange);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    lbLocation = new JLabel();
    lbLocation.setForeground(new Color(-10066330));
    lbLocation.setText("...");
    panel3.add(lbLocation, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(40, -1), null, null, 1,
      false));
    lbSync = new JLabel();
    lbSync.setIcon(
      new ImageIcon(getClass().getResource("/org/sylfra/idea/plugins/revu/resources/icons/desynchronized.png")));
    this.$$$loadLabelText$$$(lbSync,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.sync.label.text"));
    lbSync.setToolTipText(ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
      "issueForm.main.sync.tip.text"));
    panel3.add(lbSync, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    contentPane.add(scrollPane1,
      new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(659, 180),
        null, 0, false));
    taDesc = new JTextArea();
    taDesc.setLineWrap(true);
    taDesc.setRows(6);
    taDesc.setText("");
    taDesc.setWrapStyleWord(true);
    scrollPane1.setViewportView(taDesc);
    final JScrollPane scrollPane2 = new JScrollPane();
    contentPane.add(scrollPane2,
      new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
        null, null, null, 0, false));
    taSummary = new JTextArea();
    taSummary.setLineWrap(true);
    taSummary.setRows(2);
    scrollPane2.setViewportView(taSummary);
    contentPane.add(tagsMultiChooserPanel,
      new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    pnReview = new JPanel();
    pnReview.setLayout(new CardLayout(0, 0));
    contentPane.add(pnReview, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    cbReview = new JComboBox();
    pnReview.add(cbReview, "combo");
    lbReview = new JLabel();
    lbReview.setText("Label");
    pnReview.add(lbReview, "label");
    final JLabel label5 = new JLabel();
    this.$$$loadLabelText$$$(label5,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.main.priority.label"));
    contentPane.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    cbPriority = new JComboBox();
    cbPriority.setEditable(false);
    contentPane.add(cbPriority, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    label1.setLabelFor(taDesc);
    label2.setLabelFor(taSummary);
    label3.setLabelFor(cbReview);
    label5.setLabelFor(cbPriority);
    bgLocation = new ButtonGroup();
    bgLocation.add(rbLocationFile);
    bgLocation.add(rbLocationLineRange);
    bgLocation.add(rbLocationGlobal);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }

  private static class ReviewComboBoxModel extends AbstractListModel implements IReviewListener, ComboBoxModel
  {
    private final static Comparator<Review> REVIEW_COMPARATOR = new Comparator<Review>()
    {
      /**
       * {@inheritDoc}
       */
      public int compare(Review r1, Review r2)
      {
        return (r1 == null) ? -1 : ((r2 == null) ? 1 : r1.getName().compareTo(r2.getName()));
      }
    };

    private final Project project;
    private final List<Review> reviews;
    private Review selectedReview;

    private ReviewComboBoxModel(@NotNull Project project)
    {
      this.project = project;
      reviews = new ArrayList<Review>(1);
      reviews.add(null);

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(this);
    }

    public int getSize()
    {
      return reviews.size();
    }

    public Object getElementAt(int index)
    {
      return reviews.get(index);
    }

    public void setSelectedItem(Object item)
    {
      if (((selectedReview != null) && !selectedReview.equals(item)) || ((selectedReview == null) && (item != null)))
      {
        selectedReview = (Review) item;
        fireContentsChanged(this, -1, -1);
      }
    }

    public Object getSelectedItem()
    {
      return selectedReview;
    }

    public void updateReviews()
    {
      reviews.clear();
      reviews.add(null);
      reviews.addAll(project.getComponent(ReviewManager.class).getReviews(RevuUtils.getCurrentUserLogin(), true));
      Collections.sort(reviews, REVIEW_COMPARATOR);
    }

    public void reviewAdded(Review review)
    {
      if (!RevuUtils.isActive(review))
      {
        return;
      }

      reviews.add(review);
      Collections.sort(reviews, REVIEW_COMPARATOR);

      int index = reviews.indexOf(review);
      fireIntervalAdded(this, index, index);
    }

    public void reviewChanged(Review review)
    {
      int index = reviews.indexOf(review);
      if (index == -1)
      {
        if (RevuUtils.isActive(review))
        {
          reviewAdded(review);
        }
      }
      else
      {
        if (!RevuUtils.isActive(review))
        {
          reviewDeleted(review);
        }
        else
        {
          fireContentsChanged(this, index, index);
        }
      }
    }

    public void reviewDeleted(Review review)
    {
      int index = reviews.indexOf(review);
      if (index > -1)
      {
        reviews.remove(review);
        fireIntervalRemoved(this, index, index);
      }
    }
  }
}
