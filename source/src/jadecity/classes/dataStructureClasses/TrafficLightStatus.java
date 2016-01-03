/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jadecity.classes.dataStructureClasses;

import java.awt.Label;
import java.awt.Color;

/**
 *
 * @author SONY
 */
public class TrafficLightStatus {

    private String crossRoadName;
    private Label left, right, up, down;
    
    private final Color greenColor = new Color(145,235,167);  //Color.GREEN;
    private final Color redColor = new Color(251,134,158);    //Color.RED;

    public TrafficLightStatus() {
    }

    public TrafficLightStatus(String crossRoadName, Label left, Label right, Label up, Label down) {
        this.crossRoadName = crossRoadName;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }

    public String getCrossRoadName() {
        return crossRoadName;
    }
    
    public Label getDown() {
        return down;
    }

    public Label getLeft() {
        return left;
    }

    public Label getRight() {
        return right;
    }

    public Label getUp() {
        return up;
    }

    public void setCrossRoadName(String crossRoadName) {
        this.crossRoadName = crossRoadName;
    }
    
    public void setDown(Label down) {
        this.down = down;
    }

    public void setLeft(Label left) {
        this.left = left;
    }

    public void setRight(Label right) {
        this.right = right;
    }

    public void setUp(Label up) {
        this.up = up;
    }

    public boolean isLeftGreen() {
        return left.getBackground().equals(greenColor);
    }

    public boolean isRightGreen() {
        return right.getBackground().equals(greenColor);
    }

    public boolean isUpGreen() {
        return up.getBackground().equals(greenColor);
    }

    public boolean isDownGreen() {
        return down.getBackground().equals(greenColor);
    }
    
    public boolean canIGo(Direction dir)
    {
        boolean result=false;
        switch(dir)
        {
            case Right:
                result=isLeftGreen();
                break;
                
            case Left:
                result=isRightGreen();
                break;
                
            case Up:
                result=isDownGreen();
                break;
                
            case Down:
                result=isUpGreen();
                break;
                
            case Nothing:
            default:
                result=false;
                break;
        }
        return result;
    }

    public void makeLeftGreen()
    {
        left.setBackground(greenColor);
    }

    public void makeRightGreen()
    {
        right.setBackground(greenColor);
    }

    public void makeUpGreen()
    {
        up.setBackground(greenColor);
    }

    public void makeDownGreen()
    {
        down.setBackground(greenColor);
    }

    public void makeLeftRed()
    {
        left.setBackground(redColor);
    }

    public void makeRightRed()
    {
        right.setBackground(redColor);
    }

    public void makeUpRed()
    {
        up.setBackground(redColor);
    }

    public void makeDownRed()
    {
        down.setBackground(redColor);
    }

    public void changeLightClockwise()
    {
        if(isLeftGreen())
        {
            makeLeftRed();
            makeUpGreen();
        }
        else if(isUpGreen())
        {
            makeUpRed();
            makeRightGreen();
        }
        else if(isRightGreen())
        {
            makeRightRed();
            makeDownGreen();
        }
        else if(isDownGreen())
        {
            makeDownRed();
            makeLeftGreen();
        }
    }
    
    public void makeAllRed()
    {
        makeDownRed();
        makeUpRed();
        makeLeftRed();
        makeRightRed();
    }
    
    public void changeLight2Green(Direction dir)
    {
        if(dir==null || dir==Direction.Nothing)
        {
            changeLightClockwise();
            return ;
        }
        
        makeAllRed();
                
        switch(dir)
        {
            case Up:
                makeUpGreen();
                break;
                
            case Down:
                makeDownGreen();
                break;
                
            case Left:
                makeLeftGreen();
                break;
                
            case Right:
                makeRightGreen();
                break;  
        }
    }
}
