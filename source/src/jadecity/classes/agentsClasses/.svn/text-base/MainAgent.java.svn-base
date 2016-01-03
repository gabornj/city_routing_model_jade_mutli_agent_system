/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.agentsClasses;

import jadecity.frames.agentsFrame.MainFrame;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import java.util.LinkedList;
import java.util.List;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import jadecity.classes.dataStructureClasses.Area;
import jadecity.classes.dataStructureClasses.AreaDescription;
import jadecity.classes.dataStructureClasses.Direction;
import jadecity.classes.dataStructureClasses.InitData;
import jadecity.classes.dataStructureClasses.MobileAgentLocation;
import jadecity.classes.dataStructureClasses.MyPerformatives;
import jadecity.classes.dataStructureClasses.Point;
import jadecity.classes.dataStructureClasses.PointImage;
import jadecity.classes.dataStructureClasses.Report;
import jadecity.classes.dataStructureClasses.TrafficLightAlgorithm;
import jadecity.classes.dataStructureClasses.TrafficLightStatus;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.JLabel;
import sun.misc.Compare;
import sun.misc.Sort;


/**
 *
 * @author SONY
 */
public class MainAgent extends Agent
{
    private MainFrame frame;
    private int numberOfAgents;
    private Hashtable<AID,Point> agentsLocation;
    public long delayTimeForUpdateGui;
    public String InfoFilePath;
    public String indexFilePath;
    private int agentCounter;
    private List<AID> mobileAgents;
    private Hashtable<String,AID> trafficLightsAgents;   // this shows what is the AID of traffic light agent in a crossroad with known name string
    public boolean linearMode;
    public boolean sequentailMode;
    public List<InitData> initAgentsData;
    public boolean GPSIsOn;
    private int agentsTotalCounter;   // it shows that how many agents is created in total up to now whether they're dead or alive now
    public boolean lightsWithGPS;
    public String highLightedAgentName;
    public boolean dontKillAtFinishingTime;
    public boolean useCenterEndpoints;
    public boolean useAcrossCenterEndpoints;
    public TrafficLightAlgorithm trafficLightAlgorithm;
    private Hashtable<String,List<AreaDescription>> trafficLightsInfo;
    public int PRIORITY_COEFFICIENT_FOR_TRAFFIC=100;
    public int lightDuration;
    
    public static Area ENVIRONMENT;
    public List<Report> reports;


    @Override
    protected void setup()
    {
        // -- init variables and show frame --
        frame=new MainFrame(this);
        frame.show();
        agentsLocation=new Hashtable<AID, Point>();
        agentsTotalCounter=0;
        initAgentsData=new ArrayList<InitData>();
        mobileAgents=Collections.synchronizedList(new ArrayList<AID>());
        trafficLightsAgents=new Hashtable<String, AID>();
        numberOfAgents=0;  // init it by 0 then in frame is going to be changed
        delayTimeForUpdateGui=1000;  // by default 1000 ms or 1 seconds
        indexFilePath=InfoFilePath="";
        agentCounter=1;
        linearMode=sequentailMode=false;
        GPSIsOn=true;
        lightsWithGPS=false;
        ENVIRONMENT=new Area(100, 100, 1200, 600, "WholeEnvironment");
        reports=new ArrayList<Report>();
        highLightedAgentName="0";
        dontKillAtFinishingTime=false;
        useCenterEndpoints=false;
        useAcrossCenterEndpoints=true;
        initTrafficLightsInformation();
            

        // -- init jade agent platform --
        ContentManager cmanager=getContentManager();
        Codec lang=new SLCodec();
        cmanager.registerLanguage(lang);
        cmanager.registerOntology(JADEManagementOntology.getInstance(), JADEManagementOntology.NAME);
        DFAgentDescription dfAgDesc=new DFAgentDescription();
        dfAgDesc.addLanguages(lang.getName());
        dfAgDesc.setName(getAID());
        ServiceDescription service=new ServiceDescription();
        service.setName(getLocalName());
        service.addLanguages(lang.getName());
        service.setType("Main-Agent");
        dfAgDesc.addServices(service);
        try
        {
            DFService.register(this, dfAgDesc);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(frame, "Error In Registering Main Agent In DFService\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        
        // -- create the traffic light agents now --
        //createTrafficLightAgents();

        // -- add behaviours to main agent --
        addBehaviour(new TakeCoordinates());
        addBehaviour(new TakeCreateNextMobileAgentSignal());
        addBehaviour(new TakeGPSRequestAndRespond());
        addBehaviour(new TakeReportMessage());
        addBehaviour(new SendSpeedCoefficient());
        addBehaviour(new TakeKillMsgFromMobiles());
        addBehaviour(new TakeInitDataOfMobileAgents());
        addBehaviour(new SendBestWayOfCrossroadForGreenLight());
    }

    @Override
    protected void takeDown()
    {
        super.takeDown();
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(frame,"Error In Deregistering Main Agent\n"+ex.getStackTrace(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("Main Agent Terminated ...");
        //frame.dispose();
        killAgent(this, getAID());
    }

    public static void killAgent(Agent ag,AID aid)
    {
        try
        {
            KillAgent kill=new KillAgent();
            kill.setAgent(aid);
            // create and send the message to the ams
            ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
            ag.getContentManager().registerOntology(JADEManagementOntology.getInstance(),JADEManagementOntology.NAME);
            msg.setOntology(JADEManagementOntology.NAME);
            msg.setLanguage(ag.getContentManager().lookupLanguage("fipa-sl").getName());
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            ag.getContentManager().fillContent(msg, new Action(ag.getAMS(), kill));
            msg.addReceiver(ag.getAMS());
            ag.send(msg);
        }
        catch (Codec.CodecException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        catch (OntologyException ex)
        {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void killAllMobileAgents()
    {
        java.util.Iterator<Entry<AID,Point>> it=agentsLocation.entrySet().iterator();
        ACLMessage msg=new ACLMessage(MyPerformatives.killYourSelf);
        msg.setContent("Please Kill Yourself Now !!!");
        while(it.hasNext())
        {
            msg.addReceiver(it.next().getKey());
        }      
        this.send(msg);
        
        //destruct and clear last data here and prepare for next generation   << CHECK HERE >>
        agentsLocation.clear();
        initAgentsData.clear();
        reports.clear();
        agentCounter=1;
        agentsTotalCounter=0;   // << CHECK HERE AGAIN >>
        
        indexFilePath=InfoFilePath="";
    }
    
    public void killAllLightAgents()
    {
        java.util.Iterator<Entry<String,AID>> it=trafficLightsAgents.entrySet().iterator();
        ACLMessage msg=new ACLMessage(MyPerformatives.killYourSelf);
        msg.setContent("Please Kill Yourself Now !!!");
        while(it.hasNext())
        {
            msg.addReceiver(it.next().getValue());
        }      
        this.send(msg);
        
        // we must clear trafficLightAgents hashtable
        trafficLightsAgents.clear();
    }
    
    public int getNumberOfAgents()
    {
        return numberOfAgents;
    }

    public void setNumberOfAgents(int n)
    {
        numberOfAgents=n;
    }

    public void updateMobileAgentsGPSEnablity()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.GPS_Status);
        msg.setContent(String.valueOf(GPSIsOn));
        java.util.Iterator<InitData> it=initAgentsData.iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().name);
        }
        this.send(msg);
    }

    public void updateUseCenterEndpointsUsability()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.UseCenterEndpointsInCrossroads);
        msg.setContent(String.valueOf(useCenterEndpoints));
        java.util.Iterator<InitData> it=initAgentsData.iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().name);
        }
        this.send(msg);
    }
    
    public void updateUseAcrossCenterEndpointsUsability()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.UseAcrossCenterEndpointsInCrossroads);
        msg.setContent(String.valueOf(useAcrossCenterEndpoints));
        java.util.Iterator<InitData> it=initAgentsData.iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().name);
        }
        this.send(msg);
    }
    
    public void updateMobileAgentsDontKillAfterFinishing()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.DontKillAfterFinishing);
        msg.setContent(String.valueOf(dontKillAtFinishingTime));
        java.util.Iterator<InitData> it=initAgentsData.iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().name);
        }
        this.send(msg);
    }
    
    public InitData findMobileAgent(String agentName)
    {
        for(InitData d : initAgentsData)
        {
            if(d.name.getLocalName().compareToIgnoreCase(agentName)==0)
            {
                return d;
            }
        }
        return null;    // if not found
    }
    
    public void updateTrafficAgentsGPSEnablity()
    {
        if(trafficLightsAgents.size()==0)      // this means no traffic lights were created
        {
            return ;
        }
        
        ACLMessage msg=new ACLMessage(MyPerformatives.LightGPSStatus);
        msg.setContent(String.valueOf(lightsWithGPS));
        java.util.Iterator<Entry<String,AID>> it=trafficLightsAgents.entrySet().iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().getValue());
        }
        this.send(msg);
    }
    
    public void add2AgentsTotalCounter(int num)
    {
        agentsTotalCounter+=num;
    }

    public int getAgentsTotalCounter()
    {
        return agentsTotalCounter;
    }

    public boolean firstTime=true;
    public void showAllAgents()
    {
        frame.updateFrame();
        frame.drawEnvironment();
        synchronized(agentsLocation)
        {
            if(!linearMode || firstTime)   // equals =>   (!linearmode || (linearMode && firstTime))
            {
                if(agentsLocation.size()==numberOfAgents) firstTime=false;
                java.util.Iterator<Entry<AID,Point>> it=agentsLocation.entrySet().iterator();
                while(it.hasNext())
                {
                    Entry<AID,Point> en=it.next();
                    if(sequentailMode)
                    {
                        frame.drawAgentNowPlace(en.getValue().x, en.getValue().y,en.getKey().getLocalName().substring(11));
                    }
                    else
                    {
                        frame.drawAgent(en.getValue().x, en.getValue().y,en.getKey().getLocalName().substring(11));
                    }
                }
            }
        }
    }

    public void add2agentsLocations(Point loc,AID ag)
    {
        synchronized(agentsLocation)
        {
            Point before=agentsLocation.get(ag);
            if(before!=null)
            {
                if(linearMode)
                {
                    frame.drawAgentMovement(before.x , before.y, loc.x, loc.y);
                }
                else if(sequentailMode)
                {
                    frame.drawAgent(before.x, before.y, ag.getLocalName().substring(11));
                }
            }
            agentsLocation.put(ag, loc);
        }
    }

    public void add2agentsLocationsAndShow(Point loc,AID ag)
    {
        add2agentsLocations(loc, ag);
        showAllAgents();
        //System.out.println(ag.getLocalName()+" =>  "+loc.x+" "+loc.y+" showed .");
    }

    public void createTrafficLightAgents()
    {
       try 
       {          
            PlatformController pc=getContainerController();
            for(int i=1;i<=6;i++)     // because we have 6 crossroads
            {
                // traffic light agent's arguments
                TrafficLightStatus st=new TrafficLightStatus("crossroad"+i, frame.findLabel("left"+i), frame.findLabel("right"+i), frame.findLabel("up"+i), frame.findLabel("down"+i));
                Object[] args=new Object[5];
                args[0]=st;
                args[1]=lightsWithGPS;
                args[2]=trafficLightAlgorithm;
                args[3]=getAID();
                args[4]=lightDuration;

                pc.createNewAgent("TrafficLightAgent"+i, TrafficLightAgent.class.getName(), args).start();
                System.out.println("TrafficLightAgent"+i+" Createad ...");
                AID id=new AID("TrafficLightAgent"+i,AID.ISLOCALNAME);
                trafficLightsAgents.put("crossroad"+i, id);
            }         
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(frame, "An Error While Creating Traffic Light Agents Occured\n\n"+ex.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void createNextMobileAgent()
    {
        PlatformController pc=getContainerController();
        try
        {
            Object[] args=new Object[9];
            args[0]=getName();   // main agent's full name
            args[1]=String.valueOf(delayTimeForUpdateGui);
            args[2]=InfoFilePath;
            args[3]=indexFilePath;
            args[4]=trafficLightsAgents;
            args[5]=dontKillAtFinishingTime;
            args[6]=useCenterEndpoints;
            args[7]=useAcrossCenterEndpoints;
            args[8]=GPSIsOn;
            
            if(agentCounter<=getAgentsTotalCounter())
            {
//                JLabel label=new JLabel(String.valueOf(agentCounter));
//                frame.add(label);
//                args[6]=label;
                
                pc.createNewAgent("MobileAgent"+(agentCounter), MobileAgent.class.getName(), args).start();
                System.out.println("MobileAgent"+(agentCounter)+" Createad ...");
                AID id=new AID("MobileAgent"+agentCounter,AID.ISLOCALNAME);
                mobileAgents.add(id);
                agentCounter++;
            }
        }
        catch (ControllerException ex)
        {
            JOptionPane.showMessageDialog(frame, "An Error While Creating Mobile Agents Occured\n\n"+ex.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean isInEnvironment(Point loc)
    {
        return ENVIRONMENT.isInArea(loc);
    }

    public void sendStartSignal2MobileAgents()
    {
        addBehaviour(new SendStartSignals());  // this is more standard way to send one or some messages
    }

    public synchronized int GPSFunction(Area ar,Direction dir)
    {
        int cnt=0;
        synchronized(agentsLocation)
        {
            try
            {
                java.util.Iterator<Entry<AID,Point>> it=agentsLocation.entrySet().iterator();
                while(it.hasNext())
                {
                    Entry<AID,Point> en=it.next();
                    if(ar.isInArea(en.getValue(),dir))
                    {
                        cnt++;
                    }
                }
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(frame, "An Error Occured In GPSFunction ...\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
        return cnt;
    }

    public synchronized double getCoefficientOfSpeed(Area ar,String me,String agentsdirection)     // this is for speed multiplication , count the number of agents except him/her self and return 1/number and if number of agents in that area was 0 return 1.0 for more speed
    {        
        int cnt=0;
        Direction direction=Direction.Nothing;
        if(agentsdirection.equalsIgnoreCase(Direction.Down.toString()))
        {
            direction=Direction.Down;
        }
        else if(agentsdirection.equalsIgnoreCase(Direction.Up.toString()))
        {
            direction=Direction.Up;
        }
        else if(agentsdirection.equalsIgnoreCase(Direction.Right.toString()))
        {
            direction=Direction.Right;
        }
        else if(agentsdirection.equalsIgnoreCase(Direction.Left.toString()))
        {
            direction=Direction.Left;
        }
        
        synchronized(agentsLocation)
        {
            try
            {
                java.util.Iterator<Entry<AID,Point>> it=agentsLocation.entrySet().iterator();
                while(it.hasNext())
                {
                    Entry<AID,Point> en=it.next();
                    if(en.getKey().getLocalName().compareToIgnoreCase(me)!=0 && ar.isInArea(en.getValue(),direction))
                    {
                        cnt++;
                    }
                }
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(frame, "An Error Occured In GPSFunction ...\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            }
        }
        
        if(cnt==0) return 1.0;    // << CHECK HERE >>  maybe this will make a problem , if its problem is so awfull change it to 1
        return 1.0/(double)cnt;
    }

    public static Direction getInverse(Direction direction)
    {
        switch(direction)
        {
            case Down:
                return Direction.Up;
                
            case Up:
                return Direction.Down;
                
            case Left:
                return Direction.Right;
                
            case Right:
                return Direction.Left;
                
            case Nothing:
                return Direction.Nothing;
        }
        return null;     // it means error
    }
    
    public void sendDurationTime2TrafficLights()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.TrafficLightDurationTime);
        msg.setContent(String.valueOf(lightDuration));
        java.util.Iterator<Entry<String,AID>> it=trafficLightsAgents.entrySet().iterator();
        while(it.hasNext())
        {
            msg.addReceiver(it.next().getValue());
        }
        this.send(msg);
    }

    public void sortReports()
    {
        Compare cmp=new Compare() {

            public int doCompare(Object o1, Object o2)
            {
                String n1=((Report)o1).name;
                String n2=((Report)o2).name;
                if(n1.length()>n2.length()) return 1;
                else if(n1.length()<n2.length()) return -1;
                else return n1.compareToIgnoreCase(n2);      // it means when n1 and n2 has same length
            }
        };
        Object[] obj=reports.toArray();
        Sort.quicksort(obj, cmp);
        reports.clear();
        for(int i=0;i<obj.length;i++)
        {
            reports.add((Report)obj[i]);
        }
    }

    void addReportIfNotExistIn(Report r)
    {
        for (int i = 0; i < reports.size(); i++)
        {
            if(r.name.equalsIgnoreCase(reports.get(i).name))
            {
                return ;
            }
        }
        reports.add(r);
    }

    public void initTrafficLightsInformation()
    {
        trafficLightsInfo=new Hashtable<String, List<AreaDescription>>();
        
        List<AreaDescription> t1=new ArrayList<AreaDescription>();
        t1.add(new AreaDescription(Direction.Right, 100, 200, 300, 300, "avenue1"));
        t1.add(new AreaDescription(Direction.Down, 300, 100, 400, 200, "avenue4"));
        t1.add(new AreaDescription(Direction.Left, 400, 200, 600, 300, "street1"));
        t1.add(new AreaDescription(Direction.Up, 300, 300, 400, 400, "street2"));
        trafficLightsInfo.put("TrafficLightAgent1", t1);
        
        List<AreaDescription> t2=new ArrayList<AreaDescription>();
        t2.add(new AreaDescription(Direction.Right, 100, 400, 300, 500, "avenue2"));
        t2.add(new AreaDescription(Direction.Down, 300, 300, 400, 400, "street2"));
        t2.add(new AreaDescription(Direction.Left, 400, 400, 600, 500, "street3"));
        t2.add(new AreaDescription(Direction.Up, 300, 500, 400, 600, "avenue3"));
        trafficLightsInfo.put("TrafficLightAgent2", t2);
        
        List<AreaDescription> t3=new ArrayList<AreaDescription>();
        t3.add(new AreaDescription(Direction.Right, 400, 200, 600, 300, "street1"));
        t3.add(new AreaDescription(Direction.Down, 600, 100, 700, 200, "avenue6"));
        t3.add(new AreaDescription(Direction.Left, 700, 200, 900, 300, "street5"));
        t3.add(new AreaDescription(Direction.Up, 600, 300, 700, 400, "street4"));
        trafficLightsInfo.put("TrafficLightAgent3", t3);
        
        List<AreaDescription> t4=new ArrayList<AreaDescription>();
        t4.add(new AreaDescription(Direction.Right, 400, 400, 600, 500, "street3"));
        t4.add(new AreaDescription(Direction.Down, 600, 300, 700, 400, "street4"));
        t4.add(new AreaDescription(Direction.Left, 700, 400, 900, 500, "street6"));
        t4.add(new AreaDescription(Direction.Up, 600, 500, 700, 600, "avenue5"));
        trafficLightsInfo.put("TrafficLightAgent4", t4);
        
        List<AreaDescription> t5=new ArrayList<AreaDescription>();
        t5.add(new AreaDescription(Direction.Right, 700, 200, 900, 300, "street5"));
        t5.add(new AreaDescription(Direction.Down, 900, 100, 1000, 200, "avenue8"));
        t5.add(new AreaDescription(Direction.Left, 1000, 200, 1200, 300, "avenue9"));
        t5.add(new AreaDescription(Direction.Up, 900, 300, 1000, 400, "street7"));
        trafficLightsInfo.put("TrafficLightAgent5", t5);

        List<AreaDescription> t6=new ArrayList<AreaDescription>();
        t6.add(new AreaDescription(Direction.Right, 700, 400, 900, 500, "street6"));
        t6.add(new AreaDescription(Direction.Down, 900, 300, 1000, 400, "street7"));
        t6.add(new AreaDescription(Direction.Left, 1000, 400, 1200, 500, "avenue10"));
        t6.add(new AreaDescription(Direction.Up, 900, 500, 1000, 600, "avenue7"));
        trafficLightsInfo.put("TrafficLightAgent6", t6);
    }
    

    // ----------------===== Private Classes =====----------------

    private class TakeInitDataOfMobileAgents extends SimpleBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.SendInitData);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                initAgentsData.add((InitData)((Property)msg.getEnvelope().getAllProperties().next()).getValue());
            }
            else
            {
                block();
            }
        }

        @Override
        public boolean done()
        {
            return numberOfAgents!=0 && initAgentsData.size()==numberOfAgents;
        }

    }

    private class SendStartSignals extends OneShotBehaviour
    {
        @Override
        public void action() 
        {
            ACLMessage msg=new ACLMessage(MyPerformatives.startSignal);
            msg.setContent("Please Start Your Move.");
            for(AID id:mobileAgents)
            {
                msg.addReceiver(id);
            }
            myAgent.send(msg);
            mobileAgents.clear();   // because we don't need it any more cause we have agentsLocation synchronizedList for locations and aids
            System.gc();
        }
    }

    private class TakeCoordinates extends CyclicBehaviour   // from mobile agents
    {
        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                Envelope env=msg.getEnvelope();
                Iterator it=env.getAllProperties();
                Property pr=(Property)it.next();
                Point loc=(Point)pr.getValue();
                if(!isInEnvironment(loc))
                {
                    numberOfAgents--;
                    agentsLocation.remove(msg.getSender());
                    
                    ACLMessage killMessage=new ACLMessage(MyPerformatives.killYourSelf);
                    killMessage.addReceiver(msg.getSender());
                    killMessage.setContent("Please Kill Yourself Now !!!");
                    myAgent.send(killMessage);
                    if(dontKillAtFinishingTime)
                    {
                        Point p=new Point(loc.x, loc.y);
                        add2agentsLocationsAndShow(p, msg.getSender());
                    }
                }
                else
                {
                    Point p=new Point(loc.x, loc.y);
                    add2agentsLocationsAndShow(p, msg.getSender());
                }
            }
            else
            {
                block();
            }
        }
    }

    private class TakeCreateNextMobileAgentSignal extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.createNextAgent);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                createNextMobileAgent();
            }
            else
            {
                block();
            }
        }
    }

    private class TakeKillMsgFromMobiles extends CyclicBehaviour
    {
        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.SendKillSignalOfMe);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                agentsLocation.remove(msg.getSender());
                numberOfAgents=agentsLocation.size();
                if(numberOfAgents==0)    // this means all of mobile agents were kiiled now kill all of light agents
                {
                    killAllLightAgents();
                }
            }
            else
            {
                block();
            }
        }
        
    }

    private class TakeGPSRequestAndRespond extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GPSRequest);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                List<PointImage> requestPoints=(List<PointImage>)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                for(PointImage pi:requestPoints)
                {
                    int res=GPSFunction(pi.getArea(),pi.getDirection());
                    pi.setValue(pi.getValue()+PRIORITY_COEFFICIENT_FOR_TRAFFIC*(res-1));     // res-1 because of only one agent in an area doesn't worry to travel because this won't reduce its speed
                }
                
                ACLMessage reply=msg.createReply();
                reply.setPerformative(MyPerformatives.GPSResponse);
                Envelope env=new Envelope();
                env.addProperties(new Property("resultPointsImage", requestPoints));
                reply.setEnvelope(env);
                reply.setContent("These Are Your Points After GPS Results Added");
                myAgent.send(reply);
                // clearing envelope now
                msg.getEnvelope().clearAllProperties();
                msg.getEnvelope().clearAllTo();
            }
            else
            {
                block();
            }
        }
    }

    private class TakeReportMessage extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.ReportMsg);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                Report rep=(Report)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                addReportIfNotExistIn(rep);
            }
            else
            {
                block();
            }
        }
    }

    private class SendSpeedCoefficient extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.GiveSpeedCoefAboutTraffic);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                String senderName=msg.getSender().getLocalName();
                String agentDirection=msg.getContent();
                Area senderArea=(Area)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                double res=getCoefficientOfSpeed(senderArea, senderName, agentDirection);
                ACLMessage reply=msg.createReply();
                reply.setPerformative(MyPerformatives.GiveSpeedCoefAboutTrafficAck);
                reply.setContent(String.valueOf(res));
                myAgent.send(reply);
            }
            else
            {
                block();
            }
        }

    }
    
    private class SendBestWayOfCrossroadForGreenLight extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.BestWayForGreenLight);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {                
                double largeNumber=999999;
                List<AreaDescription> connectedAreas=trafficLightsInfo.get(msg.getSender().getLocalName());
                
                for(AreaDescription areaDesc : connectedAreas)
                {
                    synchronized(agentsLocation)
                    {
                        java.util.Iterator<Entry<AID,Point>> it=agentsLocation.entrySet().iterator();
                        int cnt=0;
                        double minTime=largeNumber;
                        while(it.hasNext())
                        {
                            Entry<AID,Point> en=it.next();
                            if(areaDesc.isInAreaWithBorders(en.getValue(),areaDesc.getDirection2Crossroad()))
                            {
                                cnt++;
                                double distance=en.getValue().distance(areaDesc.getEndPoint());
                                double speed=findMobileAgent(en.getKey().getLocalName()).speed;
                                double time=distance/speed;
                                if(time<minTime)
                                {
                                    minTime=time;
                                }
                            }
                        }
                        double coef;
                        if(cnt==0) 
                        {
                            coef=1.0;
                        }
                        else
                        {
                            coef=1.0/(double)cnt;
                        }
                        areaDesc.setMinTime(minTime/coef);
                    }
                }
                
                // now sort it and get min
                Compare comp=new Compare()
                {
                    public int doCompare(Object o1, Object o2)
                    {
                        double a=((AreaDescription)o1).getMinTime();
                        double b=((AreaDescription)o2).getMinTime();

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
                Object[] conAreas=connectedAreas.toArray();
                Sort.quicksort(conAreas, comp);
                AreaDescription best=(AreaDescription)conAreas[conAreas.length-1];      // min
                Direction resultDir=Direction.Nothing;    // and if nothing sends then make green by clockwise order
                if(best.getMinTime()!=largeNumber)
                {
                    resultDir=getInverse(best.getDirection2Crossroad());
                }
                ACLMessage response=msg.createReply();
                response.setPerformative(MyPerformatives.BestWayForGreenLightResponse);
                Envelope env=new Envelope();
                env.addProperties(new Property("ResultDirection", resultDir));
                response.setEnvelope(env);
                response.setContent("Best Direction Is In Envelop For Green Light .");
                myAgent.send(response);
            }
            else
            {
                block();
            }
        }
    } 
}
