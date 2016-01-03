/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class Area   // base class for areas in map
{
    private Point leftTopPoint;
    private Point rightBottomPoint;
    private String areaName;

    public Area()
    {
    }

    public Area(Point leftTopPoint, Point rightBottomPoint, String areaName)
    {
        this.leftTopPoint = leftTopPoint;
        this.rightBottomPoint = rightBottomPoint;
        this.areaName=areaName;
    }

    public Area(double x1, double y1, double x2, double y2, String name)
    {
        leftTopPoint=new Point(x1, y1);
        rightBottomPoint=new Point(x2, y2);
        areaName=name;
    }

    public boolean isInArea(Point p)
    {
        return (leftTopPoint.x<=p.x && p.x<=rightBottomPoint.x && leftTopPoint.y<=p.y && p.y<=rightBottomPoint.y);
    }

    public boolean isInArea(Point p,Direction dir)
    {
        switch(dir)
        {
            case Up:
                return isInRightOfArea(p);
                
            case Down:
                return isInLeftOfArea(p);
                
            case Right:
                return isInBottomOfArea(p);
                
            case Left:
                return isInTopOfArea(p);
        }
        return false;
    }
    
    public boolean isInAreaWithBorders(Point p,Direction dir)
    {
        switch(dir)
        {
            case Up:
                return isInRightOfAreaWithBorders(p);
                
            case Down:
                return isInLeftOfAreaWithBorders(p);
                
            case Right:
                return isInBottomOfAreaWithBorders(p);
                
            case Left:
                return isInTopOfAreaWithBorders(p);
        }
        return false;
    }
    
    private boolean checkInBounds(Point lt,Point rb,Point p)
    {
        return (lt.x<p.x && p.x<rb.x && lt.y<p.y && p.y<rb.y);
    }

    public boolean isInAreaWithoutBorders(Point p)
    {
        return (leftTopPoint.x<p.x && p.x<rightBottomPoint.x && leftTopPoint.y<p.y && p.y<rightBottomPoint.y);
    }

    public boolean isInRightOfArea(Point p)
    {
        if(isInAreaWithoutBorders(p))
        {
            double r=width();
            return ((leftTopPoint.x+r/2)<p.x && p.x<rightBottomPoint.x);
        }
        return false;
    }

    public boolean isInLeftOfArea(Point p)
    {
        if(isInAreaWithoutBorders(p))
        {
            double r=width();
            return (leftTopPoint.x<p.x && p.x<(r/2+leftTopPoint.x)); 
        }
        return false;
    }

    public boolean isInTopOfArea(Point p)
    {   
        if(isInAreaWithoutBorders(p))
        {
            double r=height();
            return (leftTopPoint.y<p.y && p.y<(leftTopPoint.y+r/2));
        }
        return false;
    }

    public boolean isInBottomOfArea(Point p)
    {
        if(isInAreaWithoutBorders(p))
        {
            double r=height();
            return ((leftTopPoint.y+r/2)<p.y && p.y<rightBottomPoint.y);
        }
        return false;
    }

    public boolean isInRightOfAreaWithBorders(Point p)
    {
        if(isInArea(p))
        {
            double r=width();
            return ((leftTopPoint.x+r/2)<=p.x && p.x<=rightBottomPoint.x);
        }
        return false;
    }

    public boolean isInLeftOfAreaWithBorders(Point p)
    {
        if(isInArea(p))
        {
            double r=width();
            return (leftTopPoint.x<=p.x && p.x<=(r/2+leftTopPoint.x)); 
        }
        return false;
    }

    public boolean isInTopOfAreaWithBorders(Point p)
    {   
        if(isInArea(p))
        {
            double r=height();
            return (leftTopPoint.y<=p.y && p.y<=(leftTopPoint.y+r/2));
        }
        return false;
    }

    public boolean isInBottomOfAreaWithBorders(Point p)
    {
        if(isInArea(p))
        {
            double r=height();
            return ((leftTopPoint.y+r/2)<=p.y && p.y<=rightBottomPoint.y);
        }
        return false;
    }
    
    public double height()
    {
        return rightBottomPoint.y-leftTopPoint.y;
    }

    public double width()
    {
        return rightBottomPoint.x-leftTopPoint.x;
    }


    public void setAreaName(String name)
    {
        areaName=name;
    }

    public String getAreaName()
    {
        return areaName;
    }

    public void setLeftTopPoint(Point p)
    {
        leftTopPoint=p;
    }

    public Point getLeftTopPoint()
    {
        return leftTopPoint;
    }

    public void setRightBottomPoint(Point p)
    {
        rightBottomPoint=p;
    }

    public Point getRightBottomPoint()
    {
        return rightBottomPoint;
    }
}

