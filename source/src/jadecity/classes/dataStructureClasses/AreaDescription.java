/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jadecity.classes.dataStructureClasses;

import jadecity.classes.agentsClasses.MainAgent;

/**
 *
 * @author SONY
 */
public class AreaDescription extends Area
{
    private Direction direction2Crossroad;
    private Point endPoint;    // end and center point of area to crossroad
    private Point acrossEndPoint;
    private double minTime;
    
    public AreaDescription()
    {
    }

    public AreaDescription(Direction direction2Crossroad, Point leftTopPoint, Point rightBottomPoint, String areaName)
    {
        super(leftTopPoint, rightBottomPoint, areaName);
        this.direction2Crossroad = direction2Crossroad;
        endPoint=calculateEndPoint(direction2Crossroad);
        acrossEndPoint=calculateEndPoint(MainAgent.getInverse(direction2Crossroad));
    }
    
    public AreaDescription(Direction direction2Crossroad, double x1, double y1, double x2, double y2, String name)
    {
        super(x1, y1, x2, y2, name);
        this.direction2Crossroad = direction2Crossroad;
        endPoint=calculateEndPoint(direction2Crossroad);
        acrossEndPoint=calculateEndPoint(MainAgent.getInverse(direction2Crossroad));
    }

    public final Point calculateEndPoint(Direction dir)
    {
        switch(dir)
        {
            case Right:
                return new Point(getRightBottomPoint().x, getLeftTopPoint().y+(getRightBottomPoint().y-getLeftTopPoint().y)/2);
                
            case Left:
                return new Point(getLeftTopPoint().x, getLeftTopPoint().y+(getRightBottomPoint().y-getLeftTopPoint().y)/2);
                
            case Up:
                return new Point(getLeftTopPoint().x+(getRightBottomPoint().x-getLeftTopPoint().x)/2, getLeftTopPoint().y);
               
            case Down:
                return new Point(getLeftTopPoint().x+(getRightBottomPoint().x-getLeftTopPoint().x)/2, getRightBottomPoint().y);
                
            case Nothing:
            default:
                System.err.println("No direction2Crossroad setted in calculateEndPoint function ...");
                break;
        }
        return null;    // it means error
    }
    
    public Point getEndPoint()
    {
        return endPoint;
    }

    public void setEndPoint(Point endPoint) 
    {
        this.endPoint = endPoint;
    }

    public Direction getDirection2Crossroad()
    {
        return direction2Crossroad;
    }

    public void setDirection2Crossroad(Direction direction2Crossroad)
    {
        this.direction2Crossroad = direction2Crossroad;
    }

    public double getMinTime() {
        return minTime;
    }
    
    public void setMinTime(double minTime) {
        this.minTime = minTime;
    }

    public Point getAcrossEndPoint() {
        return acrossEndPoint;
    }

    public void setAcrossEndPoint(Point acrossEndPoint) {
        this.acrossEndPoint = acrossEndPoint;
    }
    
}
