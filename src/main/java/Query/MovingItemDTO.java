package Query;

import command.DeleteItemCommand;
import command.Position;

public class MovingItemDTO implements MovingItem {
    private final String name;
    private final Position location;
    private int numberOfMoves;
    private int value;
    public MovingItemDTO(String name, Position location, int numberOfMoves, int value  ){
        this.name=name;
        this.location=location;
        this.numberOfMoves=numberOfMoves;
        this.value=value;
    }
    public String getName(){
        return this.name;
    }
    public Position getLocation(){
        return this.location;
    }
    public int getNumberOfMoves(){
        return this.numberOfMoves;
    }
    public int getValue(){
        return this.value;
    }
    public void move(Position moveCoords){
        if(this.numberOfMoves>=19){
            new DeleteItemCommand(this.name);
            return;
        }
        this.location.setX(this.location.getX()+moveCoords.getX());
        this.location.setY(this.location.getY()+moveCoords.getY());
        this.location.setZ(this.location.getZ()+moveCoords.getZ());
        this.numberOfMoves += 1;
    }
    public void changeValue(int newValue){
        this.value=newValue;
    }

    @Override
    public String toString() {
        return name+"("+ location.getX()+","+location.getY()+","+location.getZ()+")"+"value: "+value+" number of moves:"+numberOfMoves;
    }
}
