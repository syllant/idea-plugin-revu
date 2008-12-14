package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.ElementsChooserPopup;
import org.sylfra.idea.plugins.revu.ui.editor.RevuEditorHandler;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueMainForm extends AbstractIssueForm
{
  private final boolean createMode;
  private TagsPane tagsPane;
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

  public IssueMainForm(@NotNull Project project, boolean createMode)
  {
    super(project);
    this.createMode = createMode;
    configureUI();
  }

  private void createUIComponents()
  {
    tagsPane = new TagsPane();
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

            tagsPane.setEnabled(true);
          }
          else
          {
            // "[Select a value]" String is selected
            cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));

            tagsPane.setEnabled(false);
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

    tagsPane.configureUI();
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
          new TreeSet<IssuePriority>(currentIssue.getReview().getDataReferential().getIssuePrioritiesByName(true).values()), true)));
        lbReview.setText(RevuBundle.message("issueForm.main.review.text", data.getReview().getName(),
          RevuUtils.buildReviewStatusLabel(data.getReview().getStatus())));
      }
    }

    taDesc.setText((data == null) ? "" : data.getDesc());
    taSummary.setText((data == null) ? "" : data.getSummary());
    cbPriority.setSelectedItem((data == null) ? null : data.getPriority());
    lbSync.setVisible((data != null) && (!isIssueSynchronized(data)));
    tagsPane.updateUI((data == null) ? null : data.getTags());
    tagsPane.setEnabled((!createMode) || (cbReview.getSelectedItem() != null));

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
    data.setTags(tagsPane.getSelectedTags());

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

    if (!checkEquals(tagsPane.getSelectedTags(), data.getTags()))
    {
      return true;
    }

    if (!checkEquals(bgLocation.getSelection().getActionCommand(), data.getLocationType().toString()))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    // @TODO handle status
    boolean mayReview = (((createMode) || (user != null))
      && ((currentIssue == null) || (currentIssue.getReview() == null)
        || (IssueStatus.CLOSED != currentIssue.getStatus())));
    RevuUtils.setWriteAccess(mayReview, cbPriority, taSummary, taDesc, rbLocationGlobal);

    Issue.LocationType locationType = (currentIssue == null) ? null : currentIssue.getLocationType();
    rbLocationFile.setEnabled(mayReview && !Issue.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(mayReview && !Issue.LocationType.GLOBAL.equals(locationType)
      && !Issue.LocationType.FILE.equals(locationType));

    tagsPane.setEnabled(mayReview && ((!createMode) || (cbReview.getSelectedIndex() > 0)));
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

  private final class TagsPane extends JPanel
  {
    private JPanel contentPane;
    private ElementsChooserPopup<IssueTag> popup;
    private JPanel pnTags;
    private final SortedList<IssueTag> selectedTags;
    private AnAction editAction;
    private JComponent toolbar;

    public TagsPane()
    {
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));
      selectedTags = new SortedList<IssueTag>(new Comparator<IssueTag>()
      {
        public int compare(IssueTag o1, IssueTag o2)
        {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }

    public void configureUI()
    {
      pnTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

      editAction = new AnAction(RevuBundle.message("issueForm.editTags.tip"), null,
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.EDIT_TAGS))
      {
        @Override
        public void actionPerformed(AnActionEvent e)
        {
          Review review = (createMode) ? (Review) cbReview.getSelectedItem() : currentIssue.getReview();

          List<IssueTag> tags = review.getDataReferential().getIssueTags(true);
          showEditPopup(tags);
        }

        @Override
        public void update(AnActionEvent e)
        {
          e.getPresentation().setEnabled(getTemplatePresentation().isEnabled());
        }
      };
      // Should use #registerCustomShortcutSet ?
      getActionMap().put(editAction, new AbstractAction()
      {
        public void actionPerformed(ActionEvent e)
        {
          if (editAction.getTemplatePresentation().isEnabled())
          {
            editAction.actionPerformed(null);
          }
        }
      });
      getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(lbTags.getDisplayedMnemonic(), KeyEvent.ALT_MASK), editAction);


      DefaultActionGroup actionGroup = new DefaultActionGroup();
      actionGroup.add(editAction);
      toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).getComponent();
      lbTags.setLabelFor(toolbar.getComponent(0));

      add(pnTags);
      add(toolbar);

      popup = new ElementsChooserPopup<IssueTag>(project, RevuBundle.message("issueForm.tagsPopup.title"),
        RevuPlugin.PLUGIN_NAME + ".TagsChooser",
        new ElementsChooserPopup.IPopupListener<IssueTag>()
        {
          public void apply(@NotNull List<IssueTag> markedElements)
          {
            updateUI(markedElements);
          }
        },
        new ElementsChooserPopup.IItemRenderer<IssueTag>()
        {
          public String getText(IssueTag issue)
          {
            return issue.getName();
          }
        });
    }

    @Override
    public void setEnabled(boolean enabled)
    {
      editAction.getTemplatePresentation().setEnabled(enabled);
    }

    public void showEditPopup(@NotNull List<IssueTag> allTags)
    {
      popup.show(toolbar, false, allTags, selectedTags);
    }

    public void updateUI(@Nullable List<IssueTag> issueTags)
    {
      pnTags.removeAll();

      selectedTags.clear();
      selectedTags.addAll(issueTags);

      if (issueTags != null)
      {
        for (IssueTag tag : issueTags)
        {
          pnTags.add(new ItemTagPanel(tag));
        }
      }

      pnTags.revalidate();
    }

    public List<IssueTag> getSelectedTags()
    {
      int count = pnTags.getComponentCount();

      List<IssueTag> result = new ArrayList<IssueTag>(count);
      for (int i = 0; i < count; i++)
      {
        result.add(((ItemTagPanel) pnTags.getComponent(i)).issueTag);
      }

      return result;
    }

    private class ItemTagPanel extends JLabel
    {
      private final IssueTag issueTag;

      public ItemTagPanel(IssueTag issueTag)
      {
        super(issueTag.getName());
        this.issueTag = issueTag;

        setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.TAG));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8),
          BorderFactory.createEtchedBorder()));
      }
    }
  }
}
