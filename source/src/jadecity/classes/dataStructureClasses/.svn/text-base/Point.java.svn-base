/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class Point
{
    public double x;
    public double y;

    public Point()
    {
    }

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Point(Point p)
    {
        this.x=p.x;
        this.y=p.y;
    }

    @Override
    public String toString()
    {
        return x+" , "+y;
    }

    public double distance(Point p)
    {
        return Math.sqrt(Math.pow((this.x-p.x),2)+Math.pow((this.y-p.y), 2));
    }

    public double moveUp(double unit,double speedCoef)   // return movement distance
    {
        double r=unit*speedCoef;
        y-=r;
        return r;
    }

    public double moveDown(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        y+=r;
        return r;
    }

    public double moveRight(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        x+=r;
        return r;
    }

    public double moveLeft(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        x-=r;
        return r;
    }
    
    public Point peekUp(double unit,double speedCoef)   // return destination Point without moving there
    {
        double r=unit*speedCoef;
        return new Point(x,y-r);
    }

    public Point peekDown(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        return new Point(x,y+r);
    }

    public Point peekRight(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        return new Point(x+r,y);
    }

    public Point peekLeft(double unit,double speedCoef)
    {
        double r=unit*speedCoef;
        return new Point(x-r,y);
    }
}
