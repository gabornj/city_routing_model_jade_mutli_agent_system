/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class Crossroad extends Area
{
    public Crossroad()
    {
        super();
    }

    public Crossroad(Point leftTopPoint, Point rightBottomPoint, String areaName)
    {
        super(leftTopPoint, rightBottomPoint, areaName);
    }

    public Crossroad(double x1, double y1, double x2, double y2, String name)
    {
        super(x1, y1, x2, y2, name);
    }
}
