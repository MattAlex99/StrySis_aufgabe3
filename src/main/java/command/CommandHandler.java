package command;

import Query.*;

import javax.jms.*;


public class CommandHandler {
    private static CommandHandler singleInstance= null;
    private static EventSender sender=null;

    public CommandHandler(Connection connection, Session session, Destination destination) {
        if (singleInstance == null) {
            CommandHandler.sender = new JMSSender( connection,  session,  destination);
        }
    }

    public static CommandHandler getSingleInstance(Connection connection, Session session, Destination destination) {
        if (CommandHandler.singleInstance== null) {
            CommandHandler.singleInstance = new CommandHandler(connection,session,destination);

        }
        return CommandHandler.singleInstance;
    }
    public static CommandHandler getSingleInstance() {
        if (CommandHandler.singleInstance== null) {
            return null;
        }
        return CommandHandler.singleInstance;
    }



    public  static void handle(ChangeValueCommand command){
        ValueChangedEvent event= new ValueChangedEvent(command.getName(),command.getValueData());
        CommandHandler.sender.sendEvent(event);
    }
    public static void handle(CreateItemCommand command){
        if(!UsedNamesAggregate.isNameUsed(command.getName())){
            ItemCreatedEvent event=new ItemCreatedEvent(command.getName(), new Position(0,0,0),0,0);
            UsedNamesAggregate.addName(command.getName());
            NumberOfMovesAggregate.addNewItem(command.getName());
            UsedPositionAggregate.addUnmovedItem(command.getName());

            CommandHandler.sender.sendEvent(event);
        }
    }

    public static void handle(DeleteItemCommand command){
        if (UsedNamesAggregate.isNameUsed(command.getName())){
            ItemDeletedEvent event = new ItemDeletedEvent(command.getName());
            UsedNamesAggregate.deleteName(command.getName());
            NumberOfMovesAggregate.removeItem(command.getName());
            UsedPositionAggregate.removeItem(command.getName());
            CommandHandler.sender.sendEvent(event);
        }
    }
    public static void handle(MoveItemCommand command){

        if (command.getMoveCoords().isZeroMove()){
            //atempted move with zero vector
            return;
        }
        if(NumberOfMovesAggregate.isMoveLimitReached(command.getName())){
            //attempted movement when movement limit is reached
            new DeleteItemCommand(command.getName());
            return;
        }
        Position oldPosition= UsedPositionAggregate.getPositionByName(command.getName());
        if(oldPosition==null){
            //attempted movement of non existent object name
            return;
        }
        Position newPosition= oldPosition.add(command.getMoveCoords());
        if(UsedPositionAggregate.isPositionUsed(newPosition)){
            //remove old object if position is already occupied
            String nameOfOldItemAtNewPosition = UsedPositionAggregate.getItemNameByPosition(newPosition);
            new DeleteItemCommand(nameOfOldItemAtNewPosition);
        }
        ItemMovedEvent event=new ItemMovedEvent(command.getName(), command.getMoveCoords());
        UsedPositionAggregate.changePosition(command.getName(),newPosition);
        CommandHandler.sender.sendEvent(event);

        }
}
