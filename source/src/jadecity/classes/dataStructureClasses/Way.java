/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class Way
{
    private Direction dir;
    private double value;

    public Way()
    {
    }

    public Way(Direction dir, double value)
    {
        this.dir = dir;
        this.value = value;
    }

    public Direction getDir()
    {
        return dir;
    }

    public void setDir(Direction dir)
    {
        this.dir = dir;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }
    
}
