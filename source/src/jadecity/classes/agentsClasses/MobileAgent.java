/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.agentsClasses;

import jadecity.frames.agentsFrame.MobileFrame;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadecity.classes.dataStructureClasses.Area;
import jadecity.classes.dataStructureClasses.AreaDescription;
import jadecity.classes.dataStructureClasses.AreaType;
import jadecity.classes.dataStructureClasses.Crossroad;
import jadecity.classes.dataStructureClasses.Direction;
import jadecity.classes.dataStructureClasses.InitData;
import jadecity.classes.dataStructureClasses.MyPerformatives;
import jadecity.classes.dataStructureClasses.Point;
import jadecity.classes.dataStructureClasses.PointImage;
import jadecity.classes.dataStructureClasses.PrivateAvenue;
import jadecity.classes.dataStructureClasses.Report;
import jadecity.classes.dataStructureClasses.Street;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import sun.misc.Compare;
import sun.misc.Sort;

/**
 *
 * @author SONY
 */
public class MobileAgent extends Agent
{
    private Point location;    // the location in every single seconds
    private Point source;
    private Point destination;
    private MobileFrame frame;
    private double speed;
    private AID mainAgentId;
    private long delayTimeForUpdateGui;
    private String InfoFilePath;
    private String indexFilePath;
    private Area[] areas;
    private Direction direction;
    private TickerBehaviour moveAndSendCoordinatesBehaviour;
    private boolean listenForGPSResponse;
    private double totalDistance;
    private long beginTime;
    public boolean GPSIsOn;
    private Hashtable<String,AID> trafficLightsAgents;   // this shows what is the AID of traffic light agent in a crossroad with known name string
    private boolean dontKillAtFinishingTime;
    private boolean useCenterEndpoints;
    private boolean useAcrossCenterEndpoints;
    //private JLabel myLabel;

    private Object[] perceptPoints;      // points that are image points and we have to do heuristic functions on them
    private Hashtable<String,List<AreaDescription>> trafficLightsInfo;
    
    public static final int AVENUE_NUMBER=10;
    public static final int STREET_NUMBER=7;
    public static final int CROSSROAD_NUMBER=6;
    public static final int AREA_NUMBER=AVENUE_NUMBER+STREET_NUMBER+CROSSROAD_NUMBER;
    //public static final int PRIORITY_COEFFICIENT_FOR_TRAFFIC=100;


    // only for synchronization
    public final Object locker=new Object();


    @Override
    protected void setup()
    {
        // -- init variables and show frame --

        frame=new MobileFrame(this);
        frame.show();

        totalDistance=0;
        Object[] args=getArguments();
        String mainName=(String)args[0];
        mainAgentId=new AID(mainName, AID.ISGUID);
        delayTimeForUpdateGui=Long.parseLong((String)args[1]);
        InfoFilePath=(String)args[2];
        indexFilePath=(String)args[3];
        trafficLightsAgents=(Hashtable<String, AID>)args[4];
        listenForGPSResponse=false;
        dontKillAtFinishingTime=(Boolean)args[5];
        useCenterEndpoints=(Boolean)args[6];
        useAcrossCenterEndpoints=(Boolean)args[7];
        GPSIsOn=(Boolean)args[8];
        //myLabel=(JLabel)args[9];
        
        location=new Point();
        destination=new Point();
        direction=Direction.Nothing;

        initEnvironmentInformation();
        initTrafficLightsInformation();

        if(InfoFilePath.equalsIgnoreCase(""))
        {
            // by random
            setPropertiesByRandom();
        }
        else
        {
            // from file
            try
            {
                RandomAccessFile raf=new RandomAccessFile(InfoFilePath, "rws");
                RandomAccessFile indexFile=new RandomAccessFile(indexFilePath, "rws");   // created beside of info file
                synchronized(locker)
                {
                    int index=indexFile.read();
                    indexFile.seek(0);
                    indexFile.write(index+1);
                    indexFile.close();
                    raf.seek(0);
                    for(int i=0;i<index-1;i++)
                    {
                        raf.readLine();
                    }
                    String[] res=null;     // while(raf.getFilePointer()<raf.length())
                    try
                    {
                        res=raf.readLine().split(",");
                        
                        location.x=Double.parseDouble(res[0]);
                        location.y=Double.parseDouble(res[1]);
                        speed=Double.parseDouble(res[2]);
                        destination.x=Double.parseDouble(res[3]);
                        destination.y=Double.parseDouble(res[4]);
                    }
                    catch(Exception exc)
                    {
                        // it means file is finished now
                        // by random
                        setPropertiesByRandom();
                    }
                }
            }
            catch(FileNotFoundException ex)
            {
                JOptionPane.showMessageDialog(frame, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
            catch(IOException exc)
            {
                JOptionPane.showMessageDialog(frame, exc.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }

        source=new Point(location);   // copy source location to source property for that agent will know about his/her source location

        // send create next agent signal for main agent
        addBehaviour(new CreateNextAgentSignal());   // << CHECK HERE >>


        // view info on frame
        updateFrameWithNewCoordinates();
        frame.xDestLabel.setText(String.valueOf(destination.x));
        frame.yDestLabel.setText(String.valueOf(destination.y));
        frame.speedLabel.setText(String.valueOf(speed));
        frame.sourceCityLabel.setText(getCityName(source));
        frame.destCityLabel.setText(getCityName(destination));


        // -- init jade agent platform --
        ContentManager cmanager=getContentManager();
        Codec lang=new SLCodec();
        cmanager.registerLanguage(lang);
        cmanager.registerOntology(JADEManagementOntology.getInstance(), JADEManagementOntology.NAME);
        DFAgentDescription dfd=new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addLanguages(lang.getName());
        ServiceDescription service=new ServiceDescription();
        service.addLanguages(lang.getName());
        service.setType("Mobile-Agent");
        service.setName(getLocalName());
        dfd.addServices(service);
        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(frame, "Error In Registering Mobile Agent In DFService\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }


        // -- add behaviours to main agent --
        addBehaviour(new listenForStartSignal());
        addBehaviour(new TakeKillSignal());
        addBehaviour(new TakeGPSRespond());
        addBehaviour(new SendInitData());
        addBehaviour(new TakeGPSStatus());
        addBehaviour(new TakeDontKillSignal());
        addBehaviour(new TakeUseCenterEndpointsSignal());
        addBehaviour(new TakeAcrossUseCenterEndpointsSignal());
    }

    public void setPropertiesByRandom()
    {
        // simple random generatoin
        Random rand=new Random();
        //location.x=rand.nextInt(2)*1000+200;
        //location.y=(1+rand.nextInt(2))*200+rand.nextInt(100);
        int i=rand.nextInt(10);
        int j=rand.nextInt(10);

        location=((PrivateAvenue)areas[i]).getCityLocation();
        //int d=rand.nextInt(5);
        //location.x+=d;
        //location.y+=d;

        destination=((PrivateAvenue)areas[j]).getCityLocation();

        speed=5+rand.nextInt(25);
        
        //destination.x=rand.nextInt(2)*1000+200;
        //destination.y=(1+rand.nextInt(2))*200+rand.nextInt(100);
    }

    @Override
    protected void takeDown()
    {
        //reportThis2Finished();

        super.takeDown();
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException ex)
        {
            System.err.println("Error In Deregistering "+getLocalName()+"\n"+ex.getStackTrace());
            //JOptionPane.showMessageDialog(frame,"Error In Deregistering Main Agent\n"+ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        System.out.println(getLocalName()+" Terminated Successfully Now ...");
        //frame.dispose();
        MainAgent.killAgent(this, getAID());
    }

    public Area getArea(Point p)
    {
        for(int i=0;i<AREA_NUMBER;i++)
        {
            if(areas[i].isInArea(p))
            {
                return areas[i];
            }
        }
        return null;   // it means not found
    }

    public boolean canIGoNow(Area nextArea,Area nowArea)
    {        
        if(!(nowArea instanceof Crossroad) && (nextArea instanceof Crossroad))
        {
            ACLMessage directionMsg=new ACLMessage(MyPerformatives.LightIsGreen);
            directionMsg.addReceiver(trafficLightsAgents.get(nextArea.getAreaName()));
            Envelope en=new Envelope();
            en.addProperties(new Property("My Direction", direction));
            directionMsg.setEnvelope(en);
            directionMsg.setContent("Can I Go Through Now ?");
            this.send(directionMsg);

            MessageTemplate msgTmpl=MessageTemplate.MatchPerformative(MyPerformatives.LightIsGreenResponse);
            ACLMessage resMsg=this.blockingReceive(msgTmpl);
            boolean canIGo=Boolean.parseBoolean(resMsg.getContent());
            return canIGo;
        }
        return true;
    }

    public void doMoveOn()
    {      
        Area ar=getArea(location);
        Area nextAr=null;
        frame.areaLabel.setText(ar.getAreaName());

        ACLMessage msg=new ACLMessage(MyPerformatives.GiveSpeedCoefAboutTraffic);
        msg.addReceiver(mainAgentId);
        Envelope env=new Envelope();
        env.addProperties(new Property("MyArea", ar));
        msg.setEnvelope(env);
        msg.setContent(direction.toString());
        // msg.setContent("Give Me The Speed Coefficient About Where I Am Now .");
        this.send(msg);

        MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GiveSpeedCoefAboutTrafficAck);
        ACLMessage resultMsg=this.blockingReceive(mt);
        double speedCoef=Double.parseDouble(resultMsg.getContent());
        frame.currentSpeedLabel.setText(String.valueOf(speed*speedCoef));
        
        switch(direction)
        {
            case Up:
                if(ar.isInLeftOfArea(location))
                {
                    totalDistance+=location.moveRight(ar.width()/2,speedCoef);
                    break;
                }
                nextAr=getArea(location.peekUp(speed,speedCoef));
                if(canIGoNow(nextAr,ar))
                {
                    totalDistance+=location.moveUp(speed,speedCoef);
                }
                break;

            case Down:
                if(ar.isInRightOfArea(location))
                {
                    totalDistance+=location.moveLeft(ar.width()/2,speedCoef);
                    break;
                }
                nextAr=getArea(location.peekDown(speed,speedCoef));
                if(canIGoNow(nextAr,ar))
                {
                    totalDistance+=location.moveDown(speed,speedCoef);
                }
                break;

            case Right:
                if(ar.isInTopOfArea(location))
                {
                    totalDistance+=location.moveDown(ar.height()/2,speedCoef);
                    break;
                }
                nextAr=getArea(location.peekRight(speed,speedCoef));
                if(canIGoNow(nextAr,ar))
                {
                    totalDistance+=location.moveRight(speed,speedCoef);
                }
                break;

            case Left:
                if(ar.isInBottomOfArea(location))
                {
                    totalDistance+=location.moveUp(ar.height()/2,speedCoef);
                    break;
                }
                nextAr=getArea(location.peekLeft(speed,speedCoef));
                if(canIGoNow(nextAr,ar))
                {
                    totalDistance+=location.moveLeft(speed,speedCoef);
                }
                break;

            case Nothing:
            default:
                break;
        }
        frame.totalDistanceLabel.setText(String.valueOf(totalDistance));
        //myLabel.setBounds((int)location.x,(int)location.y+10,10,10);
    }

    public void understandFirstDirection(PrivateAvenue pa)
    {
        direction=pa.getInitDirection();
    }

    public void initEnvironmentInformation()    // for routing in map
    {
        areas=new Area[AREA_NUMBER];
        int cnt=0;


        //-- Private Avenues --
        // avenues that they are near to cities and there are no other cities in their way
        areas[cnt++]=new PrivateAvenue(100, 200, 300, 300, "avenue1",Direction.Right,"A");    // avenue1
        areas[cnt++]=new PrivateAvenue(100, 400, 300, 500, "avenue2",Direction.Right,"B");    // avenue2
        areas[cnt++]=new PrivateAvenue(300, 500, 400, 600, "avenue3",Direction.Up,"F");    // avenue3
        areas[cnt++]=new PrivateAvenue(300, 100, 400, 200, "avenue4",Direction.Down,"C");   // avenue4
        areas[cnt++]=new PrivateAvenue(600, 500, 700, 600, "avenue5",Direction.Up,"G");   // avenue5
        areas[cnt++]=new PrivateAvenue(600, 100, 700, 200, "avenue6",Direction.Down,"D");   // avenue6
        areas[cnt++]=new PrivateAvenue(900, 500, 1000, 600, "avenue7",Direction.Up,"H");   // avenue7
        areas[cnt++]=new PrivateAvenue(900, 100, 1000, 200, "avenue8",Direction.Down,"E");   // avenue8
        areas[cnt++]=new PrivateAvenue(1000, 200, 1200, 300, "avenue9",Direction.Left,"I");   // avenue9
        areas[cnt++]=new PrivateAvenue(1000, 400, 1200, 500, "avenue10",Direction.Left,"J");   // avenue10


        // -- Streets --
        // streets which they are between crossroads and connect private avenues and crossroads to each other
        // these usually have teraffic
        areas[cnt++]=new Street(400, 200, 600, 300, "street1");
        areas[cnt++]=new Street(300, 300, 400, 400, "street2");
        areas[cnt++]=new Street(400, 400, 600, 500, "street3");
        areas[cnt++]=new Street(600, 300, 700, 400, "street4");
        areas[cnt++]=new Street(700, 200, 900, 300, "street5");
        areas[cnt++]=new Street(700, 400, 900, 500, "street6");
        areas[cnt++]=new Street(900, 300, 1000, 400, "street7");


        // -- CrossRoads --
        // crossroads which they are between streets and streets or between streets and private avenues that mobile agent must make a right desicion in them for geting to him/her destination
        areas[cnt++]=new Crossroad(300, 200, 400, 300, "crossroad1");
        areas[cnt++]=new Crossroad(300, 400, 400, 500, "crossroad2");
        areas[cnt++]=new Crossroad(600, 200, 700, 300, "crossroad3");
        areas[cnt++]=new Crossroad(600, 400, 700, 500, "crossroad4");
        areas[cnt++]=new Crossroad(900, 200, 1000, 300, "crossroad5");
        areas[cnt++]=new Crossroad(900, 400, 1000, 500, "crossroad6");


        if(cnt!=AREA_NUMBER)
        {
            JOptionPane.showMessageDialog(frame,"Error In Number Of Areas In initEnvironmentInformation Function Occurred ...","ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateFrameWithNewCoordinates()
    {
        frame.xLocLabel.setText(String.valueOf(location.x));
        frame.yLocLabel.setText(String.valueOf(location.y));
    }

    public boolean isThisPrivateAvenueDestination(PrivateAvenue p)
    {
        return (destination.distance(p.getCityLocation())<=PrivateAvenue.TRESHOLD);  // or  return isThisLocationInThisPrivateAvenue(destination, p);
    }

    public boolean isThisLocationInThisPrivateAvenue(Point po,PrivateAvenue pa)
    {
        return (po.distance(pa.getCityLocation())<=PrivateAvenue.TRESHOLD);
    }

    public String getCityName(Point p)
    {
        String res="-";
        for(int i=0;i<10;i++)
        {
            PrivateAvenue pa=(PrivateAvenue)areas[i];
            if(isThisLocationInThisPrivateAvenue(p,pa))
            {
                res=pa.getCityName();
            }
        }
        return res;
    }

    public void initTrafficLightsInformation()
    {
        trafficLightsInfo=new Hashtable<String, List<AreaDescription>>();
        
        List<AreaDescription> t1=new ArrayList<AreaDescription>();
        t1.add(new AreaDescription(Direction.Right, 100, 200, 300, 300, "avenue1"));
        t1.add(new AreaDescription(Direction.Down, 300, 100, 400, 200, "avenue4"));
        t1.add(new AreaDescription(Direction.Left, 400, 200, 600, 300, "street1"));
        t1.add(new AreaDescription(Direction.Up, 300, 300, 400, 400, "street2"));
        trafficLightsInfo.put("crossroad1", t1);
        
        List<AreaDescription> t2=new ArrayList<AreaDescription>();
        t2.add(new AreaDescription(Direction.Right, 100, 400, 300, 500, "avenue2"));
        t2.add(new AreaDescription(Direction.Down, 300, 300, 400, 400, "street2"));
        t2.add(new AreaDescription(Direction.Left, 400, 400, 600, 500, "street3"));
        t2.add(new AreaDescription(Direction.Up, 300, 500, 400, 600, "avenue3"));
        trafficLightsInfo.put("crossroad2", t2);
        
        List<AreaDescription> t3=new ArrayList<AreaDescription>();
        t3.add(new AreaDescription(Direction.Right, 400, 200, 600, 300, "street1"));
        t3.add(new AreaDescription(Direction.Down, 600, 100, 700, 200, "avenue6"));
        t3.add(new AreaDescription(Direction.Left, 700, 200, 900, 300, "street5"));
        t3.add(new AreaDescription(Direction.Up, 600, 300, 700, 400, "street4"));
        trafficLightsInfo.put("crossroad3", t3);
        
        List<AreaDescription> t4=new ArrayList<AreaDescription>();
        t4.add(new AreaDescription(Direction.Right, 400, 400, 600, 500, "street3"));
        t4.add(new AreaDescription(Direction.Down, 600, 300, 700, 400, "street4"));
        t4.add(new AreaDescription(Direction.Left, 700, 400, 900, 500, "street6"));
        t4.add(new AreaDescription(Direction.Up, 600, 500, 700, 600, "avenue5"));
        trafficLightsInfo.put("crossroad4", t4);
        
        List<AreaDescription> t5=new ArrayList<AreaDescription>();
        t5.add(new AreaDescription(Direction.Right, 700, 200, 900, 300, "street5"));
        t5.add(new AreaDescription(Direction.Down, 900, 100, 1000, 200, "avenue8"));
        t5.add(new AreaDescription(Direction.Left, 1000, 200, 1200, 300, "avenue9"));
        t5.add(new AreaDescription(Direction.Up, 900, 300, 1000, 400, "street7"));
        trafficLightsInfo.put("crossroad5", t5);

        List<AreaDescription> t6=new ArrayList<AreaDescription>();
        t6.add(new AreaDescription(Direction.Right, 700, 400, 900, 500, "street6"));
        t6.add(new AreaDescription(Direction.Down, 900, 300, 1000, 400, "street7"));
        t6.add(new AreaDescription(Direction.Left, 1000, 400, 1200, 500, "avenue10"));
        t6.add(new AreaDescription(Direction.Up, 900, 500, 1000, 600, "avenue7"));
        trafficLightsInfo.put("crossroad6", t6);
    }
    
    public synchronized void routingFunction()
    {
        Area here=getArea(location);
        if(here instanceof PrivateAvenue)
        {
            // -- PRIVATE AVENUE --
            if(direction==Direction.Nothing)  // first time and mobile agent is near its source location
            {
                understandFirstDirection((PrivateAvenue)here);
            }
            doMoveOn();   // whether the first time or not this agent now know his/her direction and it is time to move on
        }
        else if(here instanceof Crossroad)
        {
            // -- CROSSROAD --
            List<PointImage> points=Collections.synchronizedList(new ArrayList<PointImage>());
            if(useCenterEndpoints)
            {
                for(AreaDescription ar : trafficLightsInfo.get(here.getAreaName()))
                {
                    PointImage pi=new PointImage(ar.getEndPoint());
                    pi.setDirection(MainAgent.getInverse(ar.getDirection2Crossroad()));
                    points.add(pi);                   
                }
            }
            else if(useAcrossCenterEndpoints)
            {
                for(AreaDescription ar : trafficLightsInfo.get(here.getAreaName()))
                {
                    PointImage pi=new PointImage(ar.getAcrossEndPoint());
                    pi.setDirection(MainAgent.getInverse(ar.getDirection2Crossroad()));
                    points.add(pi);                   
                }
            }
            else
            {
                points.add(new PointImage(location, Direction.Up));
                points.add(new PointImage(location, Direction.Down));
                points.add(new PointImage(location, Direction.Right));
                points.add(new PointImage(location, Direction.Left));
            }
            
            for(int i=0;i<points.size();i++)
            {
                PointImage pi=points.get(i);
                Area ar=getArea(pi);
                pi.setArea(ar);
                if(ar instanceof PrivateAvenue)
                {
                    if(isThisPrivateAvenueDestination((PrivateAvenue)ar))
                    {
                        direction=pi.getDirection();
                        doMoveOn();
                        return;   // this means the mobile agent found the destination way so , set that direction and go for it
                    }
                    else
                    {
                        points.remove(i);
                        i--;
                    }
                }
                else if(!(ar instanceof Street))
                {
                    JOptionPane.showMessageDialog(frame,"Error In Crossroad Decision Making And Retrieving Area And Instance Of ...","ERROR",JOptionPane.ERROR_MESSAGE);
                }
            }

            // now all elements of points list are Streets
            // now we must select one way that has less traffic and less distance from destination
            // so we have 2 heuristics :  traffic jam and distance with destination
            // one simple method that is used is that sum of heuristics will be our heuristic
            perceptPoints=points.toArray();
            System.out.println("\n\n"+getLocalName()+" Has Been Selected These Areas To Look At :");
            for(PointImage pi:points)
            {
                System.out.println(pi.getArea().getAreaName());
            }
            System.out.print("\n");
            heuristicFunction();
        }
        else if(here instanceof Street)
        {
            // -- STREET --
            
            if(direction==null || direction==Direction.Nothing)    // if agent starts from streets so he/she hasn't any direction and now we should set his/her direction
            {
                Area sourceArea = getArea(source);
                if(sourceArea.width()>sourceArea.height())   // horizontal street
                {
                    if(sourceArea.isInBottomOfArea(source))
                    {
                        direction=Direction.Right;
                    }
                    else
                    {
                        direction=Direction.Left;
                    }
                }
                else   // vertical street
                {
                    if(sourceArea.isInRightOfArea(source))
                    {
                        direction=Direction.Up;
                    }
                    else
                    {
                        direction=Direction.Down;
                    }
                }
            }
            
            doMoveOn();
        }
        else
        {
            if(here!=null)
            {
                // raise an error message
                JOptionPane.showMessageDialog(frame,"Error In Retrieving Area And Instance Of ...","ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void heuristicFunction()
    {
        // distance values will be setted
        for(Object pi:perceptPoints)
        {
            ((PointImage)pi).setValue(destination.distance((PointImage)pi));
        }

        if(!GPSIsOn)
        {
            heuriticFunctionContinues();
            return ;
        }


        // we must get GPS locations from main agent now

        List<PointImage> arg=new ArrayList<PointImage>();
        for(Object pi:perceptPoints)
        {
            arg.add((PointImage)pi);
        }

        System.out.println("For "+getLocalName()+" Distances Were Computed And Now He/She Is Sending GPS Request ...");

        ACLMessage msg=new ACLMessage(MyPerformatives.GPSRequest);
        msg.setContent("Please Tell Me How Many Mobile Agents Are In These Specific Areas Now ?");
        Envelope env=new Envelope();
        env.addProperties(new Property("pointImageVector", arg));
        msg.setEnvelope(env);
        msg.addReceiver(mainAgentId);
        listenForGPSResponse=true;
        this.send(msg);
    }

    public void heuriticFunctionContinues()    // after give GPS signals and add to values of each imagePoint   // perceptPoints is global argument in fact
    {
        // it continues like this :
        // now time to compare them with their value property
        Compare comp=new Compare()
        {
            public int doCompare(Object o1, Object o2)
            {
                double p1=((PointImage)o1).getValue();
                double p2=((PointImage)o2).getValue();
                if(p1 == p2)
                {
                    return 0;
                }
                else if(p1 < p2)
                {
                    return 1;
                }
                else    // p1 > p2
                {
                    return -1;
                }
            }
        };
        Sort.quicksort(perceptPoints, comp);  // sort that descending
        PointImage bestWay=(PointImage)perceptPoints[perceptPoints.length-1];
        direction=(bestWay).getDirection();   // best one (the smallest)
        System.out.println(getLocalName()+" Selected "+bestWay.getArea().getAreaName()+" And Direction "+direction.toString()+" For Best Approach Now ...");
        doMoveOn();
    }

    private void reportThis2Finished() 
    {
        long time=System.currentTimeMillis()-beginTime;
        Report rep=new Report(getLocalName(), source, destination, speed, time, totalDistance);
        ACLMessage msg=new ACLMessage(MyPerformatives.ReportMsg);
        msg.addReceiver(mainAgentId);
        Envelope env=new Envelope();
        env.addProperties(new Property("ReportMessage", rep));
        msg.setEnvelope(env);
        this.send(msg);
    }
    

    // ----------------===== Private Classes =====----------------

    private class CreateNextAgentSignal extends OneShotBehaviour
    {
        @Override
        public void action()
        {
            ACLMessage msg=new ACLMessage(MyPerformatives.createNextAgent);
            msg.addReceiver(mainAgentId);
            msg.setContent("Create Next Mobile Agent Now Please Because I Had Been Completed My Critical Works .");
            myAgent.send(msg);
        }

    }

    private class SendInitData extends OneShotBehaviour
    {
        @Override
        public void action()
        {
            ACLMessage msg=new ACLMessage(MyPerformatives.SendInitData);
            Envelope env=new Envelope();
            InitData data=new InitData(source, destination, speed,getAID());
            env.addProperties(new Property("MyInitData", data));
            msg.setEnvelope(env);
            msg.addReceiver(mainAgentId);
            myAgent.send(msg);
        }

    }

    private class listenForStartSignal extends SimpleBehaviour
    {
        private boolean isItDone=false;


        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.startSignal);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                // move and send his/her coordinates to main agent for show and log
                moveAndSendCoordinatesBehaviour=new TickerBehaviour(MobileAgent.this, delayTimeForUpdateGui)
                {
                    @Override
                    protected void onTick()
                    {
                        ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
                        msg.addReceiver(mainAgentId);
                        msg.setContent("This Is My Coordinates In Envelope.");
                        Envelope env=new Envelope();
                        env.addProperties(new Property("MyNowLocation", location));
                        msg.setEnvelope(env);
                        myAgent.send(msg);

                        // ---==> now move with knowing about map , gps and its algorithms <==---
                        routingFunction();   // very important function of mobile agent

                        if(!MainAgent.isInEnvironment(location))
                        {
                            ACLMessage message=new ACLMessage(MyPerformatives.SendKillSignalOfMe);
                            message.addReceiver(mainAgentId);
                            message.setContent("I Am Going To Kill Me Because I Finished My Job Now .");
                            myAgent.send(message);

                            reportThis2Finished();
                            
                            if(dontKillAtFinishingTime)
                            {
                                speed=0;
                                frame.speedLabel.setText(String.valueOf(speed));
                                return ;
                            }
                            
                            removeBehaviour(this);
                            takeDown();
                        }
                        updateFrameWithNewCoordinates();

                        /*
                        try
                        {
                            Thread.sleep(delayTimeForUpdateGui);    // 1000 by default
                        }
                        catch (InterruptedException ex)
                        {
                            //JOptionPane.showMessageDialog(frame,"Error In Sleeping In Thread\n"+ex.getMessage()+"\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
                        }*/
                    }
                };
                myAgent.addBehaviour(moveAndSendCoordinatesBehaviour);
                beginTime=System.currentTimeMillis();
                isItDone=true;
            }
            else
            {
                block();
            }
        }

        @Override
        public boolean done()
        {
            return isItDone;
        }
    }

    private class TakeKillSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.killYourSelf);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                System.out.println("I Gave Kill Signal From Main Now ...");
                        
                reportThis2Finished();
                
                if(dontKillAtFinishingTime)
                {
                    speed=0;
                    frame.speedLabel.setText(String.valueOf(speed));
                    return ;
                }
                
                myAgent.removeBehaviour(moveAndSendCoordinatesBehaviour);
                takeDown();
            }
            else
            {
                block();
            }
        }

    }

    private class TakeGPSStatus extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GPS_Status);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                GPSIsOn=Boolean.parseBoolean(msg.getContent());
            }
            else
            {
                block();
            }
        }
        
    }

    private class TakeDontKillSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.DontKillAfterFinishing);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                dontKillAtFinishingTime=Boolean.parseBoolean(msg.getContent());
            }
            else
            {
                block();
            }
        }
        
    }
    
    private class TakeUseCenterEndpointsSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UseCenterEndpointsInCrossroads);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                useCenterEndpoints=Boolean.parseBoolean(msg.getContent());
                useAcrossCenterEndpoints=!useCenterEndpoints;
            }
            else
            {
                block();
            }
        }
        
    }
    
    private class TakeAcrossUseCenterEndpointsSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.UseAcrossCenterEndpointsInCrossroads);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                useAcrossCenterEndpoints=Boolean.parseBoolean(msg.getContent());
                useCenterEndpoints=!useAcrossCenterEndpoints;
            }
            else
            {
                block();
            }
        }
        
    }
    
    private class TakeGPSRespond extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GPSResponse);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                if(!listenForGPSResponse)
                {
                    return ;   // throw away this message
                }
                listenForGPSResponse=false;
                List<PointImage> resultPoints=(List<PointImage>)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                perceptPoints=resultPoints.toArray();
                System.out.println("Now "+getLocalName()+" Has Been Received GPS Info And He/She Is Going To Use Heuristic Function For Selection .");
                heuriticFunctionContinues();
            }
            else
            {
                block();
            }
        }

    }
}
