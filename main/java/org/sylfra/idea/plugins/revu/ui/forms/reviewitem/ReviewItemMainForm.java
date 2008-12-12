package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.util.containers.SortedList;
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
public class ReviewItemMainForm extends AbstractReviewItemForm
{
  private ReviewItem currentReviewItem;
  private final boolean createMode;
  private TagsPane tagsPane;
  private JPanel contentPane;
  private JTextArea taDesc;
  private JComboBox cbPriority;
  private JTextArea taTitle;
  private JLabel lbLocation;
  private JComboBox cbReview;
  private JRadioButton rbLocationFile;
  private JRadioButton rbLocationGlobal;
  private JRadioButton rbLocationLineRange;
  private JComboBox cbResolutionType;
  private JLabel lbSync;
  private ButtonGroup bgLocation;

  public ReviewItemMainForm(@NotNull Project project, boolean createMode)
  {
    super(project);
    this.createMode = createMode;
    configureUI();
  }

  private void configureUI()
  {
    RevuUtils.configureTextAreaAsStandardField(taDesc, taTitle);

    RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    cbReview.setEnabled(createMode);
    cbReview.setModel(new ReviewComboBoxModel(buildComboItemsArray(reviewManager.getReviews(null, true,
      false, appSettings.getLogin()), true)));
    cbReview.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          value = ((Review) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbPriority.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message(cbReview.getSelectedItem() == null
            ? "general.selectReviewBeforeFillingCombo.text" : "general.selectComboValue.text");
        }
        else
        {
          value = ((ItemPriority) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbResolutionType.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          value = ((ItemResolutionType) value).getName();
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbReview.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Object selectedReview = cbReview.getSelectedItem();
        if (selectedReview instanceof Review)
        {
          DataReferential referential = ((Review) selectedReview).getDataReferential();

          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemPriority>(referential.getItemPrioritiesByName(true).values()), true)));

          cbResolutionType.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<ItemResolutionType>(referential.getItemResolutionTypesByName(true).values()), false)));
        }
        else
        {
          // "[Select a value]" String is selected
          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));
          cbResolutionType.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), false)));
        }
      }
    });

    ActionListener locationTypeListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ReviewItem.LocationType locationType = (rbLocationGlobal.isSelected())
          ? ReviewItem.LocationType.GLOBAL
          : ((rbLocationFile.isSelected()) ? ReviewItem.LocationType.FILE : ReviewItem.LocationType.LINE_RANGE);
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
        assert currentReviewItem != null;

        RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
        currentReviewItem.setHash(editorHandler.buildNewHash(currentReviewItem));
        currentReviewItem.getReview().fireItemUpdated(currentReviewItem);
      }
    });
  }

  public JComponent getPreferredFocusedComponent()
  {
    return taTitle;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI(@Nullable ReviewItem data, boolean requestFocus)
  {
    // This is not the standard behaviour used in other forms, but this one is not cancelable, so current review
    // item may be modified at any time, don't need to manage a copy before applying changes
    currentReviewItem = data;

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    Collection<Review> reviews = reviewManager.getReviews(true, false);

    Review defaultReview = ((data == null) || (data.getReview() == null))
      ? ((reviews.size() == 1) ? reviews.iterator().next() : null)
      : data.getReview();

    cbReview.setSelectedItem(defaultReview);

    taDesc.setText((data == null) ? "" : data.getDesc());
    taTitle.setText((data == null) ? "" : data.getSummary());
    cbPriority.setSelectedItem((data == null) ? null : data.getPriority());
    cbResolutionType.setSelectedItem((data == null) ? null : data.getResolutionType());
    lbSync.setVisible((data != null) && (!isReviewItemSynchronized(data)));
    tagsPane.updateUI((data == null) ? null : data.getTags());

    ReviewItem.LocationType locationType = (data == null) ? null : data.getLocationType();
    rbLocationFile.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(!ReviewItem.LocationType.GLOBAL.equals(locationType)
      && !ReviewItem.LocationType.FILE.equals(locationType));
    updateLocation(locationType);
  }

  protected void internalUpdateData(@NotNull ReviewItem data)
  {
    Review review = (Review) cbReview.getSelectedItem();

    data.setReview(review);

    data.setDesc(taDesc.getText());
    data.setSummary(taTitle.getText());
    data.setPriority((ItemPriority) cbPriority.getSelectedItem());
    data.setTags(tagsPane.getSelectedTags());
    data.setResolutionType((ItemResolutionType) cbResolutionType.getSelectedItem());

    // Location
    if (rbLocationGlobal.isSelected())
    {
      data.setFile(null);
      data.setLineStart(-1);
    }
    else if (rbLocationFile.isSelected())
    {
      data.setLineStart(-1);
    }
  }

  public boolean isModified(@NotNull ReviewItem data)
  {
    if (!checkEquals(taDesc.getText(), data.getDesc()))
    {
      return true;
    }

    if (!checkEquals(taTitle.getText(), data.getSummary()))
    {
      return true;
    }

    if (!checkEquals(cbReview.getSelectedItem(), data.getReview()))
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
    // @TODO
    RevuUtils.setWriteAccess(((currentReviewItem == null)
      || ((user != null) && (user.hasRole(User.Role.ADMIN)))), cbReview);

    boolean mayReview = (currentReviewItem == null)
      || (user != null) && (user.hasRole(User.Role.REVIEWER));
    RevuUtils.setWriteAccess(mayReview, cbPriority,
      rbLocationFile, rbLocationGlobal, rbLocationLineRange);
    tagsPane.setEnabled(mayReview);
  }

  public void internalValidateInput()
  {
    updateRequiredError(taTitle, "".equals(taTitle.getText().trim()));
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

  private boolean isReviewItemSynchronized(ReviewItem reviewItem)
  {
    RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
    return editorHandler.isSynchronized(reviewItem, true);
  }

  private void updateLocation(@Nullable ReviewItem.LocationType locationType)
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
        locationPath = RevuBundle.message("form.reviewitem.main.location.global.text");
        break;

      case FILE:
        rbLocationFile.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentReviewItem.getFile());
        locationPath = (currentReviewItem.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("form.reviewitem.main.location.pathWithVcsRev.text", filePath,
          currentReviewItem.getVcsRev());
        break;

      default:
        rbLocationLineRange.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentReviewItem.getFile());
        String filePathWithVcsRev = (currentReviewItem.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("form.reviewitem.main.location.pathWithVcsRev.text", filePath,
          currentReviewItem.getVcsRev());
        locationPath = RevuBundle.message("form.reviewitem.main.location.range.path.text",
          filePathWithVcsRev, (currentReviewItem.getLineStart() + 1), (currentReviewItem.getLineEnd() + 1));
    }

    lbLocation.setText(locationPath);
  }

  private void createUIComponents()
  {
    tagsPane = new TagsPane();
  }

  private class ReviewComboBoxModel extends DefaultComboBoxModel implements IReviewListener
  {
    private ReviewComboBoxModel(Object[] objects)
    {
      super(objects);
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(this);
    }

    public void reviewAdded(Review review)
    {
      super.addElement(review);
    }

    public void reviewChanged(Review review)
    {
    }

    public void reviewDeleted(Review review)
    {
      super.removeElement(review);
    }
  }

  private final class TagsPane extends JPanel
  {
    private JPanel contentPane;
    private ElementsChooser<ItemTag> chooser;
    private JBPopup popup;
    private final JPanel pnTags;
    private final SortedList<ItemTag> selectedTags;
    private AnAction editAction;
    private JComponent toolbar;

    public TagsPane()
    {
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));

      pnTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

      editAction = new AnAction(null, RevuBundle.message("form.reviewitem.editTags.tip"),
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.EDIT_TAGS))
      {
        @Override
        public void actionPerformed(AnActionEvent e)
        {
          List<ItemTag> tags = getEnclosingReview().getDataReferential().getItemTags(true);
          showEditPopup(tags);
        }
      };

      DefaultActionGroup actionGroup = new DefaultActionGroup();
      actionGroup.add(editAction);
      toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).getComponent();

      add(pnTags);
      add(toolbar);

      selectedTags = new SortedList<ItemTag>(new Comparator<ItemTag>()
      {
        public int compare(ItemTag o1, ItemTag o2)
        {
          return o1.getName().compareTo(o2.getName());
        }
      });

      chooser = new ElementsChooser<ItemTag>(true)
      {
        @Override
        protected String getItemText(ItemTag itemTag)
        {
          return itemTag.getName();
        }
      };
      chooser.setColorUnmarkedElements(false);
      configureUI();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
      editAction.getTemplatePresentation().setEnabled(false);
    }

    private void configureUI()
    {
      JButton bnOK = new JButton(RevuBundle.message("general.ok.action"));
      bnOK.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          popup.cancel();
          updateUI(chooser.getMarkedElements());
        }
      });

      JButton bnCancel = new JButton(RevuBundle.message("general.cancel.action"));
      bnCancel.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          popup.cancel();
        }
      });

      JPanel toolbar = new JPanel(new GridLayout(1, 2));
      toolbar.add(bnOK);
      toolbar.add(bnCancel);

      contentPane = new JPanel(new BorderLayout());
      contentPane.add(chooser, BorderLayout.CENTER);
      contentPane.add(toolbar, BorderLayout.SOUTH);
    }

    public void showEditPopup(@NotNull List<ItemTag> allTags)
    {
      chooser.setElements(allTags, false);
      chooser.markElements(selectedTags);

      popup = JBPopupFactory.getInstance().createComponentPopupBuilder(contentPane, chooser)
        .setModalContext(false)
        .setMovable(true)
        .setFocusable(true)
        .setResizable(false)
        .setRequestFocus(true)
        .setCancelOnClickOutside(false)
        .setTitle(RevuBundle.message("form.reviewitem.tagsPopup.title"))
        .createPopup();

      Point locationOnScreen = toolbar.getLocationOnScreen();
      Point location = new Point(
        (int) (locationOnScreen.getX()),
        (int) locationOnScreen.getY() - popup.getContent().getPreferredSize().height);
      popup.showInScreenCoordinates(toolbar, location);
    }

    public void updateUI(@Nullable List<ItemTag> itemTags)
    {
      pnTags.removeAll();

      selectedTags.clear();
      selectedTags.addAll(itemTags);

      if (itemTags != null)
      {
        for (ItemTag tag : itemTags)
        {
          pnTags.add(new ItemTagPanel(tag));
        }
      }
    }

    public List<ItemTag> getSelectedTags()
    {
      int count = pnTags.getComponentCount();

      List<ItemTag> result = new ArrayList<ItemTag>(count);
      for (int i = 0; i < count; i++)
      {
        result.add(((ItemTagPanel) pnTags.getComponent(i)).itemTag);
      }

      return result;
    }

    private class ItemTagPanel extends JLabel
    {
      private final ItemTag itemTag;

      public ItemTagPanel(ItemTag itemTag)
      {
        super(itemTag.getName());
        this.itemTag = itemTag;

        setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.TAG));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8),
          BorderFactory.createEtchedBorder()));
      }
    }
  }
}
