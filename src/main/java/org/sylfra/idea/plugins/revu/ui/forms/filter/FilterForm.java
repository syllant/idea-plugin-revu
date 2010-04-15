package org.sylfra.idea.plugins.revu.ui.forms.filter;

import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.michaelbaranov.microba.calendar.DatePicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.multichooser.AbstractMultiChooserItem;
import org.sylfra.idea.plugins.revu.ui.multichooser.MultiChooserPanel;
import org.sylfra.idea.plugins.revu.ui.multichooser.UniqueNameMultiChooserPanel;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FilterForm extends AbstractUpdatableForm<Filter>
{
  private final Project project;
  private JPanel contentPane;
  private JTextField tfName;
  private JLabel lbStatus;
  private JLabel lbRecipients;
  private JLabel lbPriority;
  private JLabel lbResolver;
  private JLabel lbTags;
  private JTextField tfSummary;
  private JTextField tfFileRef;
  private MultiChooserPanel<IssueStatus, IssueStatusMCItem> mcpStatus;
  private UniqueNameMultiChooserPanel<User> mcpRecipients;
  private UniqueNameMultiChooserPanel<User> mcpResolver;
  private UniqueNameMultiChooserPanel<IssuePriority> mcpPriority;
  private UniqueNameMultiChooserPanel<IssueTag> mcpTags;
  private JCheckBox ckCreatedAfter;
  private JCheckBox ckCreatedBefore;
  private DatePicker dpCreatedAfter;
  private DatePicker dpCreatedBefore;
  private JCheckBox ckLastUpdatedAfter;
  private JCheckBox ckLastUpdatedBefore;
  private DatePicker dpLastUpdatedAfter;
  private DatePicker dpLastUpdatedBefore;

  public FilterForm(@NotNull final Project project)
  {
    this.project = project;

    $$$setupUI$$$();

    configureUI();
  }

  private void configureUI()
  {
    DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    dpCreatedAfter.setDateFormat(dateFormat);
    dpCreatedBefore.setDateFormat(dateFormat);
    dpLastUpdatedAfter.setDateFormat(dateFormat);
    dpLastUpdatedBefore.setDateFormat(dateFormat);

    ckCreatedAfter.addActionListener(new DatePickerActionListener(ckCreatedAfter, dpCreatedAfter));
    ckCreatedBefore.addActionListener(new DatePickerActionListener(ckCreatedBefore, dpCreatedBefore));
    ckLastUpdatedAfter.addActionListener(new DatePickerActionListener(ckLastUpdatedAfter, dpLastUpdatedAfter));
    ckLastUpdatedBefore.addActionListener(new DatePickerActionListener(ckLastUpdatedBefore, dpLastUpdatedBefore));
  }

  private void createUIComponents()
  {
    // Multi Chooser Priority
    lbPriority = new JLabel();
    mcpPriority = new UniqueNameMultiChooserPanel<IssuePriority>(project, lbPriority,
      RevuBundle.message("filterForm.priorityPopup.title"), null, null)
    {
      @NotNull
      protected List<IssuePriority> getReferentialItems(@NotNull DataReferential dataReferential)
      {
        return dataReferential.getIssuePriorities(true);
      }
    };

    // Multi Chooser Recipients
    lbRecipients = new JLabel();
    mcpRecipients = createUserMCP("filterForm.recipientsPopup.title");

    // Multi Chooser Resolver
    lbResolver = new JLabel();
    mcpResolver = createUserMCP("filterForm.resolverPopup.title");

    // Multi Chooser Status
    lbStatus = new JLabel();
    mcpStatus = new MultiChooserPanel<IssueStatus, IssueStatusMCItem>(project, lbStatus,
      RevuBundle.message("filterForm.statusPopup.title"), null, null)
    {
      @Override
      protected IssueStatusMCItem createMultiChooserItem(@NotNull IssueStatus issueStatus)
      {
        return new IssueStatusMCItem(issueStatus);
      }

      @Override
      protected List<IssueStatus> retrieveAllAvailableElements()
      {
        return Arrays.asList(IssueStatus.values());
      }
    };

    // Multi Chooser Tags
    lbTags = new JLabel();
    mcpTags = new UniqueNameMultiChooserPanel<IssueTag>(project, lbTags,
      RevuBundle.message("filterForm.tagsPopup.title"), null, null)
    {
      @NotNull
      protected List<IssueTag> getReferentialItems(@NotNull DataReferential dataReferential)
      {
        return dataReferential.getIssueTags(true);
      }
    };
  }

  private UniqueNameMultiChooserPanel<User> createUserMCP(String popupTitleKey)
  {
    return new UniqueNameMultiChooserPanel<User>(project, lbRecipients,
      RevuBundle.message(popupTitleKey), null, null)
    {
      @NotNull
      protected List<User> getReferentialItems(@NotNull DataReferential dataReferential)
      {
        List<User> result = new ArrayList<User>(dataReferential.getUsers(true));
        result.add(0, new User(RevuBundle.message("filterSettings.me.text"), "",
          RevuBundle.message("filterSettings.me.text")));
        return result;
      }
    };
  }

  public boolean isModified(@NotNull Filter data)
  {
    if (!checkEquals(tfFileRef.getText(), data.getFileRef()))
    {
      return true;
    }

    if (!checkEquals(tfName.getText(), data.getName()))
    {
      return true;
    }

    if (!checkEquals(tfSummary.getText(), data.getSummary()))
    {
      return true;
    }

    if (!mcpPriority.getSelectedItemNames().equals(data.getPrioritieNames()))
    {
      return false;
    }

    if (!mcpRecipients.getSelectedItemNames().equals(data.getRecipientLogins()))
    {
      return false;
    }

    if (!mcpResolver.getSelectedItemNames().equals(data.getResolverLogins()))
    {
      return false;
    }

    if (!mcpStatus.getSelectedItemDatas().equals(data.getStatuses()))
    {
      return true;
    }

    if (!mcpTags.getSelectedItemNames().equals(data.getTagNames()))
    {
      return false;
    }

    return true;
  }

  @Override
  protected void internalUpdateWriteAccess(Filter data, @Nullable User user)
  {
  }

  protected void internalValidateInput(Filter data)
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
  }

  protected void internalUpdateUI(Filter data, boolean requestFocus)
  {
    tfFileRef.setText((data == null) ? "" : data.getFileRef());
    tfName.setText((data == null) ? "" : data.getName());
    tfSummary.setText((data == null) ? "" : data.getSummary());

    ckCreatedAfter.setSelected((data != null) && (data.getCreatedAfter() != null));
    ckCreatedBefore.setSelected((data != null) && (data.getCreatedBefore() != null));
    ckLastUpdatedAfter.setSelected((data != null) && (data.getLastUpdatedAfter() != null));
    ckLastUpdatedBefore.setSelected((data != null) && (data.getLastUpdatedBefore() != null));

    try
    {
      dpCreatedAfter.setDate((data == null) ? null : data.getCreatedAfter());
      dpCreatedBefore.setDate((data == null) ? null : data.getCreatedBefore());
      dpLastUpdatedAfter.setDate((data == null) ? null : data.getLastUpdatedAfter());
      dpLastUpdatedBefore.setDate((data == null) ? null : data.getLastUpdatedBefore());
    }
    catch (PropertyVetoException ignored)
    {
      // Ignored
    }

    mcpPriority.setSelectedItemNames((data == null) ? null : data.getPrioritieNames());
    mcpRecipients.setSelectedItemNames((data == null) ? null : data.getRecipientLogins());
    mcpResolver.setSelectedItemNames((data == null) ? null : data.getResolverLogins());
    mcpStatus.setSelectedItemDatas((data == null) ? null : data.getStatuses());
    mcpTags.setSelectedItemNames((data == null) ? null : data.getTagNames());
  }

  protected void internalUpdateData(@NotNull Filter data)
  {
    data.setFileRef(nullIfEmpty(tfFileRef.getText()));
    data.setName(nullIfEmpty(tfName.getText()));
    data.setSummary(nullIfEmpty(tfSummary.getText()));

    data.setCreatedAfter(ckCreatedAfter.isSelected() ? dpCreatedAfter.getDate() : null);
    data.setCreatedBefore(ckCreatedBefore.isSelected() ? dpCreatedBefore.getDate() : null);
    data.setLastUpdatedAfter(ckLastUpdatedAfter.isSelected() ? dpLastUpdatedAfter.getDate() : null);
    data.setLastUpdatedBefore(ckLastUpdatedBefore.isSelected() ? dpLastUpdatedBefore.getDate() : null);

    data.setPrioritieNames(nullIfEmpty(mcpPriority.getSelectedItemNames()));
    data.setRecipientLogins(nullIfEmpty(mcpRecipients.getSelectedItemNames()));
    data.setResolverLogins(nullIfEmpty(mcpResolver.getSelectedItemNames()));
    data.setStatuses(nullIfEmpty(mcpStatus.getSelectedItemDatas()));
    data.setTagNames(nullIfEmpty(mcpTags.getSelectedItemNames()));
  }

  @Nullable
  private String nullIfEmpty(@Nullable String value)
  {
    return ("".equals(value)) ? null : value;
  }

  @Nullable
  private <T> List<T> nullIfEmpty(@NotNull List<T> list)
  {
    return (list.isEmpty()) ? null : list;
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfName;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
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
    contentPane.setLayout(new BorderLayout(0, 0));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, BorderLayout.NORTH);
    tfName = new JTextField();
    panel1.add(tfName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    final JLabel label1 = new JLabel();
    label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, label1.getFont().getSize()));
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("reviewForm.name.label"));
    panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridBagLayout());
    contentPane.add(panel2, BorderLayout.CENTER);
    panel2.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.definition.title")));
    this.$$$loadLabelText$$$(lbStatus,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.status.label"));
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3, 0, 0, 0);
    panel2.add(lbStatus, gbc);
    this.$$$loadLabelText$$$(lbRecipients,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.recipients.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3, 0, 0, 0);
    panel2.add(lbRecipients, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 5;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpStatus, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 6;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpRecipients, gbc);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 9;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(panel3, gbc);
    final JLabel label2 = new JLabel();
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.summary.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    panel2.add(label2, gbc);
    tfSummary = new JTextField();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel2.add(tfSummary, gbc);
    final JLabel label3 = new JLabel();
    this.$$$loadLabelText$$$(label3,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.fileRef.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.EAST;
    panel2.add(label3, gbc);
    tfFileRef = new JTextField();
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel2.add(tfFileRef, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 7;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpResolver, gbc);
    this.$$$loadLabelText$$$(lbResolver,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.resolver.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3, 0, 0, 0);
    panel2.add(lbResolver, gbc);
    this.$$$loadLabelText$$$(lbPriority,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.priority.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3, 0, 0, 0);
    panel2.add(lbPriority, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 4;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpPriority, gbc);
    this.$$$loadLabelText$$$(lbTags,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.tags.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    gbc.insets = new Insets(3, 0, 0, 0);
    panel2.add(lbTags, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 8;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpTags, gbc);
    final JLabel label4 = new JLabel();
    this.$$$loadLabelText$$$(label4,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.createdOn.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.EAST;
    panel2.add(label4, gbc);
    final JLabel label5 = new JLabel();
    this.$$$loadLabelText$$$(label5,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.updatedOn.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.EAST;
    panel2.add(label5, gbc);
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 0, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    panel2.add(panel4, gbc);
    ckLastUpdatedAfter = new JCheckBox();
    this.$$$loadButtonText$$$(ckLastUpdatedAfter,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.dateAfter.text"));
    panel4.add(ckLastUpdatedAfter,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    dpLastUpdatedAfter = new DatePicker();
    dpLastUpdatedAfter.setStripTime(true);
    panel4.add(dpLastUpdatedAfter,
      new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_WANT_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(120, -1), null,
        0, false));
    ckLastUpdatedBefore = new JCheckBox();
    this.$$$loadButtonText$$$(ckLastUpdatedBefore,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.dateBefore.text"));
    panel4.add(ckLastUpdatedBefore,
      new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    dpLastUpdatedBefore = new DatePicker();
    panel4.add(dpLastUpdatedBefore,
      new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        null, new Dimension(120, -1), null, 0, false));
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 0, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.weightx = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    panel2.add(panel5, gbc);
    ckCreatedAfter = new JCheckBox();
    this.$$$loadButtonText$$$(ckCreatedAfter,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.dateAfter.text"));
    panel5.add(ckCreatedAfter, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    dpCreatedAfter = new DatePicker();
    panel5.add(dpCreatedAfter, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, new Dimension(120, -1), null, 0, false));
    ckCreatedBefore = new JCheckBox();
    this.$$$loadButtonText$$$(ckCreatedBefore,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.dateBefore.text"));
    panel5.add(ckCreatedBefore, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    dpCreatedBefore = new DatePicker();
    panel5.add(dpCreatedBefore, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, new Dimension(120, -1), null, 0, false));
    label1.setLabelFor(tfName);
    label2.setLabelFor(tfSummary);
    label3.setLabelFor(tfFileRef);
    label4.setLabelFor(tfFileRef);
    label5.setLabelFor(tfFileRef);
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

  private final static class IssueStatusMCItem extends AbstractMultiChooserItem<IssueStatus>
  {
    protected IssueStatusMCItem(IssueStatus issueStatus)
    {
      super(issueStatus);
    }

    public String getName()
    {
      return RevuUtils.buildIssueStatusLabel(getNestedData());
    }
  }

  private static class DatePickerActionListener implements ActionListener
  {
    private final JCheckBox checkBox;
    private final DatePicker datePicker;

    public DatePickerActionListener(JCheckBox checkBox, DatePicker datePicker)
    {
      this.checkBox = checkBox;
      this.datePicker = datePicker;
    }

    public void actionPerformed(ActionEvent e)
    {
      datePicker.setEnabled(checkBox.isSelected());
    }
  }
}