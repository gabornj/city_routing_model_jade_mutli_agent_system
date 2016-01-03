/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class PointImage extends Point
{
    private Direction dir;
    private double value=0;   // this value is going to be filled by heuristic function for select best one
    private Area area;

    public static final double DISTANCE=100;


    public PointImage()
    {
        dir=Direction.Nothing;
    }

    public PointImage(Point p)
    {
        this.x=p.x;
        this.y=p.y;
        dir=Direction.Nothing;
    }
    
    public PointImage(Point p, Direction dir)
    {
        this.x=p.x;
        this.y=p.y;
        this.dir = dir;

        switch(dir)
        {
            case Up:
                this.moveUp(DISTANCE,1);
                break;

            case Down:
                moveDown(DISTANCE,1);
                break;

            case Right:
                this.moveRight(DISTANCE,1);
                break;

            case Left:
                this.moveLeft(DISTANCE,1);
                break;

            case Nothing:
            default:
                break;
        }
    }

    public Direction getDirection()
    {
        return dir;
    }

    public void setDirection(Direction d)
    {
        dir=d;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double v)
    {
        value=v;
    }

    public Area getArea()
    {
        return area;
    }

    public void setArea(Area a)
    {
        area=a;
    }
}
