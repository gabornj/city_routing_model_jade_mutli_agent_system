/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

import javax.swing.JOptionPane;

/**
 *
 * @author SONY
 */
public class PrivateAvenue extends Area
{
    private Direction initDirection;
    private Point cityLocation;
    private String cityName;

    public static final double TRESHOLD=50;

    public PrivateAvenue()
    {
        super();
    }

    public PrivateAvenue(Point leftTopPoint, Point rightBottomPoint, String areaName, Direction d, String c)
    {
        super(leftTopPoint, rightBottomPoint, areaName);
        initDirection=d;
        cityName=c;
        calculateCityLocation();
    }

    public PrivateAvenue(double x1, double y1, double x2, double y2, String name, Direction d, String c)
    {
        super(x1, y1, x2, y2, name);
        initDirection=d;
        cityName=c;
        calculateCityLocation();
    }

    private void calculateCityLocation()
    {
        double cx=0,cy=0;
        switch(initDirection)
        {
            case Right:
                cx=getLeftTopPoint().x;
                cy=(getLeftTopPoint().y+getRightBottomPoint().y)/2;
                break;

            case Left:
                cx=getRightBottomPoint().x;
                cy=(getLeftTopPoint().y+getRightBottomPoint().y)/2;
                break;

            case Up:
                cx=(getLeftTopPoint().x+getRightBottomPoint().x)/2;
                cy=getRightBottomPoint().y;
                break;

            case Down:
                cx=(getLeftTopPoint().x+getRightBottomPoint().x)/2;
                cy=getLeftTopPoint().y;
                break;

            case Nothing:
            default:
                JOptionPane.showMessageDialog(null,"Error In calculateCityLocation switch Occurred ...","ERROR",JOptionPane.ERROR_MESSAGE);
        }
        cityLocation=new Point(cx, cy);
    }

    public void setInitDirection(Direction d)
    {
        initDirection=d;
    }

    public Direction getInitDirection()
    {
        return initDirection;
    }

    public void setCityLocation(Point p)
    {
        cityLocation=p;
    }

    public Point getCityLocation()
    {
        return cityLocation;
    }

    public void setCityName(String c)
    {
        cityName=c;
    }

    public String getCityName()
    {
        return cityName;
    }
}
