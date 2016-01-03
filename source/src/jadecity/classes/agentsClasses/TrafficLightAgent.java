/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jadecity.classes.agentsClasses;

import jade.domain.FIPAAgentManagement.Property;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jadecity.classes.dataStructureClasses.Direction;
import jadecity.classes.dataStructureClasses.LightManager;
import jadecity.classes.dataStructureClasses.MyPerformatives;
import jadecity.classes.dataStructureClasses.TrafficLightAlgorithm;
import jadecity.classes.dataStructureClasses.TrafficLightStatus;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JOptionPane;

/**
 *
 * @author SONY
 */
public class TrafficLightAgent extends Agent
{
    private TrafficLightStatus status;
    private LightManager manager;
    private boolean withGPS;
    private int lightDuration=5000;   // duration of green or red light in traffic light
    private Timer timer;
    private TrafficLightAlgorithm trafficLightAlgorithm;
    private AID mainAgentId;
    

    @Override
    protected void setup()
    {
        super.setup();
        
        // -- init variables and show frame --
        Object[] args=getArguments();
        status=(TrafficLightStatus)args[0];
        withGPS=(Boolean)args[1];
        trafficLightAlgorithm=(TrafficLightAlgorithm)args[2];
        mainAgentId=(AID)args[3];
        lightDuration=(Integer)args[4];
        manager=new LightManager();

        status.makeLeftGreen();   // by default
        
        initTimerAndChangeAlgorithm();
        
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
            JOptionPane.showMessageDialog(null, "Error In Registering Mobile Agent In DFService\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }


        // -- add behaviours to main agent --
        addBehaviour(new TellAboutLight());
        addBehaviour(new UpdateGpsAvailability());
        addBehaviour(new TakeTrafficLightDurationTimeStatus());
        addBehaviour(new TakeKillSignal());
        addBehaviour(new TakeBestDirectionForGreen());
    }

    @Override
    protected void takeDown() 
    {
        super.takeDown();
        
        status.makeAllRed();
        timer.stop();
        
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
        
        MainAgent.killAgent(this, getAID());
    }
    
    public void initTimerAndChangeAlgorithm()
    {
        ActionListener al=new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                // -- now it's time to make decision for change light algorithm --
                if(!withGPS)
                {
                    status.changeLightClockwise();   // it is clockwise yet with no aligorithm
                }
                else
                { 
                    switch(trafficLightAlgorithm)
                    {
                        case NumerialMode:    // count number of agents that they are behind of light
                            status.changeLight2Green(manager.getMostRequestedDirection()); 
                            break;
                            
                       case MinMode:        // min algorithm that minimize (distance until end of street or private avenue)/(time of reach to end of street or private avenue)
                            sendMsgForGetingTheBestDirectionForGreen();
                            break;
                    }
                }
            }
        };
        timer=new Timer(lightDuration, al);
        timer.setRepeats(true);
        timer.start();
    }

    public void sendMsgForGetingTheBestDirectionForGreen()
    {
        ACLMessage msg=new ACLMessage(MyPerformatives.BestWayForGreenLight);
        msg.setContent("Give Me The Best Direction To Make That Green .");
        msg.addReceiver(mainAgentId);
        this.send(msg);
    }
            
    
    // ----------------===== Private Classes =====----------------

    private class TellAboutLight extends CyclicBehaviour    // it is the behaviour that all of mobile agents ask traffic light agent that they can go throw light ro not (is it green or not) 
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.LightIsGreen);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                Direction dir=(Direction)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                boolean result=status.canIGo(dir);
                if(!result)
                {
                    manager.addDirection(dir);
                }
                ACLMessage reply=msg.createReply();
                reply.setPerformative(MyPerformatives.LightIsGreenResponse);
                reply.setContent(String.valueOf(result));
                myAgent.send(reply);
            }
            else
            {
                block();
            }
        }
    }    
    
    private class TakeTrafficLightDurationTimeStatus extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.TrafficLightDurationTime);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                lightDuration=Integer.parseInt(msg.getContent());
                timer.setDelay(lightDuration);
            }
            else
            {
                block();
            }
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
                System.out.println(getLocalName()+": I Gave Kill Signal From Main Now ...");
                takeDown();
            }
            else
            {
                block();
            }
        }

    }
    
    private class UpdateGpsAvailability extends CyclicBehaviour
    {
        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.LightGPSStatus);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                withGPS=Boolean.parseBoolean(msg.getContent());
            }
            else
            {
                block();
            }
        }        
    }
    
    private class TakeBestDirectionForGreen extends CyclicBehaviour
    {
        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(MyPerformatives.BestWayForGreenLightResponse);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                Direction result=(Direction)((Property)msg.getEnvelope().getAllProperties().next()).getValue();
                status.changeLight2Green(result);
            }
            else
            {
                block();
            }
        }
        
    }
}
