package main;

import Query.EventConsumer;
import Query.MovingItemDTO;
import Query.QueryImpl;
import command.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static boolean testCreationSuccess(String name){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        QueryImpl query=new QueryImpl();
        String mvName = query.getMovingItemByName(name).getName();
        if (Objects.equals(mvName, name)) {
            System.out.println("Test (getByName) sucessfull for " + name);
            return true;
        }
        else {
            System.out.println("Error in test (getByName) for  "+name+" got "+mvName);
            return false;
        }
    };
    public static boolean testDeletion(String name){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //test assumes that given name does not exist
        QueryImpl query=new QueryImpl();
        if(query.getMovingItemByName(name)==null){
            System.out.println("Test (delete) sucessfull for " + name);
            return true;
        }
        else {
            System.out.println("Error in test (getByName) for  "+name);
            return false;
        }
    }
    public static boolean testChangeValue(String name, Integer expectedValue){

        QueryImpl query=new QueryImpl();
        MovingItemDTO item=query.getMovingItemByName(name);
        if (item==null && expectedValue==null){
            System.out.println("Test (testChangeValue) sucessfull for " + name+", "+expectedValue);
            return true;
        }
        if (item==null && expectedValue!=null){
            System.out.println("Error in test (testChangeValue) for  "+name+ "Object of Name doesnt exist but Value was expected ");
            return false;
        }
        Integer givenValue = query.getMovingItemByName(name).getValue();
        if (givenValue.equals(expectedValue)) {
            System.out.println("Test (testChangeValue) sucessfull for " + name+", "+expectedValue);
            return true;
        }
        else {
            System.out.println("Error in test (testChangeValue) for  "+name+" got "+givenValue+", expected "+expectedValue);
            return false;
        }
    };
    public static boolean testMovementSuccess(String name, int[] expectedPosition){
        QueryImpl query=new QueryImpl();
        MovingItemDTO item = query.getMovingItemByName(name);
        if (item==null && expectedPosition==null){
            System.out.println("Test (testMovementSuccess) sucessfull for " + name+", "+expectedPosition);
            return true;
        }
        if (item==null && expectedPosition!=null){
            System.out.println("Error in test (testMovementSuccess) for  "+name+ "Object of Name doesnt exist but Value was expected ");
            return false;
        }
        Position pExpected= new Position(expectedPosition[0],expectedPosition[1],expectedPosition[2]);
        Position pItem = item.getLocation();
        if (pExpected.equals(pItem)) {
            System.out.println("Test (testMovementSuccess) sucessfull for " + name);
            return true;
        }
        else {
            System.out.println("Error in test (testMovementSuccess) for  "+name+" got "+pItem+" expected "+pExpected );
            return false;
        }
    };

    public static boolean testItemAtPositionSuccess(int[] position,String expectedName){
        QueryImpl query=new QueryImpl();
        ArrayList<MovingItemDTO> items = (ArrayList<MovingItemDTO>) query.getMovingItemsAtPosition(new Position(position[0],position[1],position[2]));
        boolean contains=false;
        for (MovingItemDTO item : items) {
            if (Objects.equals(item.getName(), expectedName)) {
                contains = true;
                break;
            }
        }
        if (contains) {
            System.out.println("Test (testItemAtPositionSuccess) sucessfull for " + expectedName);
            return true;
        }
        else {
            System.out.println("Error in test (testItemAtPositionSuccess) for  "+expectedName);
            return false;
        }
    };

    public static boolean testExistenceofAllItems(String[] expectedNames) {
        QueryImpl query = new QueryImpl();
        ArrayList<MovingItemDTO> items = new ArrayList<MovingItemDTO> (query.getMovingItems());
        ArrayList <String> foundNames= new ArrayList<>();
        for (int i=0;i<items.size();++i){
            foundNames.add((items.get(i)).getName());
        }
        boolean contains = foundNames.containsAll(List.of(expectedNames));
        if (contains) {
            System.out.println("Test (testExistenceofAllItems) sucessfull for " + expectedNames);
            return true;
        }
        else {
            System.out.println("Error in test (testExistenceofAllItems) for  "+expectedNames);
            return false;
        }
    }
    public static void main(String[] args) {

        //Erzeuge Consumer
        try {
            System.out.println("----Erzeuge Consumer----");
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic("EVENTS.Topic");
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(new EventConsumer());

            System.out.println("----Erzeuge Producer----");
            CommandHandler commandHandler = CommandHandler.getSingleInstance(connection,session,destination);
        } catch (JMSException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("----Erzeuge Items----");
        CommandsAcces ca = new CommandsAcces();
        QueryImpl query=new QueryImpl();
        ca.createItem( new MovingItemsImpl("Test1"));
        ca.createItem( new MovingItemsImpl("Test2"));
        ca.createItem( new MovingItemsImpl("Test3"));
        ca.createItem( new MovingItemsImpl("Test4"));
        ca.createItem( new MovingItemsImpl("Test5"));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        testCreationSuccess("Test1");
        testCreationSuccess("Test2");
        testCreationSuccess("Test3");
        testCreationSuccess("Test4");
        testCreationSuccess("Test5");

        System.out.println("----Lösche Items----");
        ca.deleteItem("Test3");
        ca.deleteItem("Test4");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //Test ob Delete funktioniert hat:
        testDeletion("Test3");
        testDeletion("Test4");

        System.out.println("");
        System.out.println("----Ändere Value Items----");
        ca.changeValue("Test1",192);
        ca.changeValue("Test2",-1337);
        ca.changeValue("Test5",69);
        ca.changeValue("Test1",420);
        ca.changeValue("Test3",5); //No Item with name Test3 exists
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //Test ob Change Value funktioniert hat:
        testChangeValue("Test2",-1337);
        testChangeValue("Test5",69);
        testChangeValue("Test1",420);
        testChangeValue("Test3",null);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("");
        System.out.println("----Bewege Items----");
        ca.moveItem("Test1",new int[]{1,-2,-3});
        ca.moveItem("Test2",new int[]{1,-5,-3});
        ca.moveItem("Test5",new int[]{4,5,-3});
        ca.moveItem("Test1",new int[]{4,5,0});
        ca.moveItem("DoesntExist1",new int[]{4,5,0});
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //test ob move Item funktioniert hat:
        testMovementSuccess("Test1",new int[]{5,3,-3});
        testMovementSuccess("Test2",new int[]{1,-5,-3});
        testMovementSuccess("Test5",new int[]{4,5,-3});
        testMovementSuccess("DoesntExist1",null);


        System.out.println("");
        System.out.println("----Teste Get Item At Position----");

        testItemAtPositionSuccess(new int[]{5,3,-3},"Test1");
        ca.moveItem("Test2",new int[]{4,8,0});
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());}
        testItemAtPositionSuccess(new int[]{5,3,-3},"Test2");


        System.out.println("");
        System.out.println("----Lösche Items----");
        ca.deleteItem("Test1");
        ca.deleteItem("Test2");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        testExistenceofAllItems(new String[]{"Test5"});
        ca.deleteItem("Test5");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        testExistenceofAllItems(new String[]{});

        System.out.println("");
        System.out.println("----Bewege item über 20 mal----");
        ca.createItem( new MovingItemsImpl("Test1"));
        for (int i=0;i<30;++i){
            ca.moveItem("Test1",new int[]{1,-2,-3});
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        testDeletion("Test1");

        /*
        System.out.println("----Erzeuge Items----");
        QueryImpl query=new QueryImpl();
        new CreateItemCommand("Test1");
        new CreateItemCommand("Test1");
        new CreateItemCommand("Test2");
        new CreateItemCommand("Test3");
        new CreateItemCommand("Test4");
        new CreateItemCommand("Test5");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        //Test ob Create funktioniert:
        System.out.println("Test1 was created: "+(query.getMovingItemByName("Test1")!=null));
        System.out.println("Test2 was created: "+(query.getMovingItemByName("Test2")!=null));
        System.out.println("Test3 was created: "+(query.getMovingItemByName("Test3")!=null));
        System.out.println("Test4 was created: "+(query.getMovingItemByName("Test4")!=null));
        System.out.println("Test5 was created: "+(query.getMovingItemByName("Test5")!=null));
        System.out.println("All created: "+(query.getMovingItems().size()==5));
        System.out.println("----Lösche Items----");

        new DeleteItemCommand("Test3");
        new DeleteItemCommand("Test4");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //Test ob Delete funktioniert hat:
        System.out.println("Test3 was deleted: "+(query.getMovingItemByName("Test3")==null));
        System.out.println("Test4 was deleted: "+(query.getMovingItemByName("Test4")==null));


        System.out.println("----Ändere Value Items----");
        new ChangeValueCommand("Test1",123);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //Test ob Change Value funktioniert hat:
        System.out.println("Test1 has new Value: "+(query.getMovingItemByName("Test1").getValue()==123));

        new ChangeValueCommand("Test2",-1337);
        new ChangeValueCommand("Test5",69);
        new ChangeValueCommand("Test1",420);
        new ChangeValueCommand("Test3",5); //Achtung Test3 wurde bereits gelöscht

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //Test ob Change Value funktioniert hat:
        System.out.println("Test1 has new Value: "+(query.getMovingItemByName("Test1").getValue()==420));
        System.out.println("Test2 has new Value: "+(query.getMovingItemByName("Test2").getValue()==-1337));
        System.out.println("Test5 has new Value: "+(query.getMovingItemByName("Test5").getValue()==69));
        System.out.println("Test3 still deleted: "+(query.getMovingItemByName("Test3")==null));


        System.out.println("----Bewege Items----");
        Position move=new Position(1,-2,-3);
        new MoveItemCommand("Test1",move);//Test1(1,-2,-3)

        //test ob move Item funktioniert hat:
        move=new Position(1,-5,-3);
        new MoveItemCommand("Test2",move);//Test2(1,-5,-3)
        move=new Position(4,5,-3);
        new MoveItemCommand("Test5",move);//Test5(4,5,-3)
        move=new Position(4,5,0);
        new MoveItemCommand("Test1",move);//Neue Position Test1(5,3,-3)

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //test ob move Item funktioniert hat:
        Position test1Location=query.getMovingItemByName("Test1").getLocation();
        boolean test1PositionCorrect=test1Location.equals(new Position(5,3,-3));
        Position test2Location=query.getMovingItemByName("Test2").getLocation();
        boolean test2PositionCorrect=test2Location.equals(new Position(1,-5,-3));
        Position test5Location=query.getMovingItemByName("Test5").getLocation();
        boolean test5PositionCorrect=test5Location.equals(new Position(4,5,-3));
        System.out.println("Test1 has Correct Position :"+test1PositionCorrect);
        System.out.println("Test2 has Correct Position :"+test2PositionCorrect);
        System.out.println("Test5 has Correct Position :"+test5PositionCorrect);

        System.out.println("----Teste Get Item At Position----");
        //Teste, ob Get Item at position funktioniert:
        Position targetPos=new Position(5,3,-3);
        String itemName = query.getMovingItemsAtPosition(targetPos).iterator().next().getName();

        System.out.println("Correct Item At Position: "+(itemName.equals("Test1")));
        move=new Position(4,8,0);
        new MoveItemCommand("Test2",move);//Neue Position Test2(5,3,-3)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        int numberOfItems= query.getMovingItemsAtPosition(targetPos).size();
        itemName=query.getMovingItemsAtPosition(targetPos).iterator().next().getName();
        System.out.println("Correct Item At Position: "+(itemName.equals("Test2")&&numberOfItems==1));



        System.out.println("----Bewege Objekt bis es gelöscht wird----");

        new CreateItemCommand("Test6");
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        for (int i=0;i<20;++i){
            new MoveItemCommand("Test6",new Position(1,1,1));
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Item Test6 deleted after 20 moves: "+(query.getMovingItemByName("Test6")==null));
        System.out.println("----Lösche Items----");
        new DeleteItemCommand("Test1");
        new DeleteItemCommand("Test2");
        new DeleteItemCommand("Test5");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        //teste, ob alle Items gelöscht wurden:
        System.out.println("Test1 has been deleted:"+ (query.getMovingItemByName("Test1")==null));
        System.out.println("Test2 has been deleted:"+ (query.getMovingItemByName("Test2")==null));
        System.out.println("Test5 has been deleted:"+ (query.getMovingItemByName("Test5")==null));
         */

        System.out.println("----Random Bewegungen Items----");

        //Viele zufällige Bewegungen
        moveTest moveTest = new moveTest();
        moveTest.create_MovingObjects();
        moveTest.moveObjects();
        moveTest.printExpectedPositions();
        moveTest.printTruePositions();
    }
}
