package Query;

import javax.jms.*;

public class EventConsumer implements MessageListener {
    public void onMessage(Message message) {
        if(message==null){
            return;
        }
        if (message instanceof TextMessage) {
        }
        if (message instanceof ObjectMessage){
            try {
                ObjectMessage objectMessage=(ObjectMessage) message;
                if(objectMessage!=null){
                    Events event = (Events) objectMessage.getObject();
                    if(event!=null){
                        //System.out.println("Event erhallten "+event.getName());
                        EventHandler.handle(event);
                    }
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
