/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.dataStructureClasses;

import java.util.Hashtable;
import java.util.Random;
import sun.misc.Compare;
import sun.misc.Sort;

/**
 *
 * @author SONY
 */
public class LightManager
{
    Hashtable<Direction,Integer> lights;

    
    public LightManager()
    {
        lights=new Hashtable<Direction, Integer>();
        init();
    }

    public void clear()
    {
        init();
    }
    
    public void init()
    {
        lights.put(Direction.Up, 0);
        lights.put(Direction.Down, 0);
        lights.put(Direction.Right, 0);
        lights.put(Direction.Left, 0);
    }
    
    public void addDirection(Direction dir)
    {
        int x=lights.get(dir)+1;
        lights.put(dir, x);
    }
    
    public Direction getMostRequestedDirection()
    {
        int u=lights.get(Direction.Up);
        int d=lights.get(Direction.Down);
        int r=lights.get(Direction.Right);
        int l=lights.get(Direction.Left);
        
        if(u+d+r+l==0)   // if all were 0 then
        {
            return Direction.Nothing;
        }
        
        // first we must clear requests
        clear();
        
        // we must do that reversely
        final int waysCount=4;           // up , down , left , right
        Way[] ways=new Way[waysCount];
        for(int i=0;i<waysCount;i++)
        {
            ways[i]=new Way();
        }
        ways[0].setDir(Direction.Down);
        ways[0].setValue(u);
        ways[1].setDir(Direction.Up);
        ways[1].setValue(d);
        ways[2].setDir(Direction.Right);
        ways[2].setValue(l);
        ways[3].setDir(Direction.Left);
        ways[3].setValue(r);

        Compare c=new Compare()
        {
            // for descending sort
            public int doCompare(Object o1, Object o2)
            {
                double a=((Way)o1).getValue();
                double b=((Way)o2).getValue();

                if(a==b)
                {
                    return 0;
                }
                else if(a>b)
                {
                    return -1;
                }
                else
                {
                    return +1;
                }
            }
        };
        Sort.quicksort(ways, c);
        
        double maxValue=ways[0].getValue();
        int index=0;
        while(index<waysCount)
        {
            if(ways[index].getValue()==maxValue)
            {
                index++;
            }
            else
            {
                break;
            }
        }

        // random one of the max of ways' values
        Random rand=new Random();
        int selectedIndex=rand.nextInt(index);

        return ways[selectedIndex].getDir();
    }
}
