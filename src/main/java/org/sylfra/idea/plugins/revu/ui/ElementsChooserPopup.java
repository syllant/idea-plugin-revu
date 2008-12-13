package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ElementsChooserPopup<T>
{
  private ElementsChooser<T> chooser;
  private JBPopup popup;
  private final String title;
  private final IPopupListener<T> popupListener;

  public ElementsChooserPopup(@NotNull String title, @NotNull IPopupListener<T> popupListener,
    @NotNull final IItemRenderer<T> itemRenderer)
  {
    this.title = title;
    this.popupListener = popupListener;

    chooser = new ElementsChooser<T>(true)
    {
      @Override
      protected String getItemText(T item)
      {
        return itemRenderer.getText(item);
      }
    };
    chooser.setColorUnmarkedElements(false);
  }

  private JPanel buildContentPane()
  {
    JButton bnOK = new JButton(RevuBundle.message("general.ok.action"));
    bnOK.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        popup.cancel();
        popupListener.apply(chooser.getMarkedElements());
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

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(chooser, BorderLayout.CENTER);
    contentPane.add(toolbar, BorderLayout.SOUTH);

    return contentPane;
  }

  public void show(@NotNull Component owner, boolean showUnderneath, @NotNull java.util.List<T> allElements,
    @NotNull java.util.List<T> markedElements)
  {
    chooser.setElements(allElements, false);
    chooser.markElements(markedElements);

    popup = JBPopupFactory.getInstance().createComponentPopupBuilder(buildContentPane(), chooser)
      .setModalContext(false)
      .setMovable(true)
      .setFocusable(true)
      .setResizable(false)
      .setRequestFocus(true)
      .setCancelOnClickOutside(false)
      .setTitle(title)
      .createPopup();

    Point locationOnScreen = owner.getLocationOnScreen();

    int x = (int) (locationOnScreen.getX());
    int y = (showUnderneath)
      ? (int) locationOnScreen.getY()
      : (int) locationOnScreen.getY() - popup.getContent().getPreferredSize().height;

    popup.showInScreenCoordinates(owner, new Point(x, y));
  }

  public static interface IItemRenderer<T>
  {
    String getText(T item);
  }

  public static interface IPopupListener<T>
  {
    void apply(@NotNull List<T> markedElements);
  }
}
