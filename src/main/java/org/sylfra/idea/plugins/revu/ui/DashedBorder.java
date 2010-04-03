package org.sylfra.idea.plugins.revu.ui;

import javax.swing.border.Border;
import java.awt.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class DashedBorder implements Border
{
  private Color color;
  private int dashLength;
  private int dashThickness;

  public DashedBorder()
  {
    this(Color.black);
  }

  public DashedBorder(Color color)
  {
    this(color, 1, 1);
  }

  public DashedBorder(Color color, int dashThickness, int dashLength)
  {
    this.color = color;
    this.dashThickness = dashThickness;
    this.dashLength = dashLength;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
  {
    g.setColor(color);

    Insets insets = getBorderInsets(c);
    int wideDashCount = Math.round(width / dashLength);
    int highDashCount = Math.round(height / dashLength);

    int startPoint;
    for (int i = 0; i <= wideDashCount; i += 2)
    {
      startPoint = x + dashLength * i;
      g.fillRect(startPoint, y, dashLength, dashThickness);
      g.fillRect(startPoint, y + height - insets.bottom, dashLength, dashThickness);
    }

    for (int i = 0; i <= highDashCount; i += 2)
    {
      startPoint = x + dashLength * i;
      g.fillRect(x, startPoint, dashThickness, dashLength);
      g.fillRect(x + width - insets.right, startPoint, dashThickness, dashLength);
    }
  }

  public Insets getBorderInsets(Component c)
  {
    return new Insets(dashThickness, dashThickness, dashThickness, dashThickness);
  }

  public boolean isBorderOpaque()
  {
    return false;
  }
}
