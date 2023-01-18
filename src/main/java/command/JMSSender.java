package command;
import javax.jms.*;
import Query.*;


public class JMSSender implements  EventSender {
    private  MessageProducer producer = null;
    private  CommandHandler singleInstance= null;
    private  Session session = null;

    public JMSSender(Connection connection, Session session, Destination destination) {
        if (singleInstance == null) {
            this.session =session;
            try {
                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                this.producer = producer;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    public  void  sendEvent(Events event){
        try{
            ObjectMessage message = session.createObjectMessage(event);
            producer.send(message);
        }catch(Exception e){System.out.println(e);}

    }
}
