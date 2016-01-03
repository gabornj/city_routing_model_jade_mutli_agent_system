/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

/**
 *
 * @author SONY
 */
public class City
{
    private String name;
    private Point location;

    public City() 
    {
    }

    public City(String name, Point location)
    {
        this.name = name;
        this.location = location;
    }
    
    public String getName()
    {
        return name;
    }

    public Point getLocation()
    {
        return location;
    }
}
