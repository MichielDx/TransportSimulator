package main.be.kdg.bagageafhandeling.transport.model;

import java.util.List;

/**
 * Created by Michiel on 4/11/2016.
 */
public class Conveyor {
    private int conveyorID;
    private int length;
    private int speed;
    private List<Connector> connectors;
    private List<Segment> segments;

    public Conveyor(int conveyorID, int length, int speed, List<Connector> connectors, List<Segment> segments) {
        this.conveyorID = conveyorID;
        this.length = length;
        this.speed = speed;
        this.connectors = connectors;
        this.segments = segments;
    }

    public int getConveyorID() {
        return conveyorID;
    }

    public void setConveyorID(int conveyorID) {
        this.conveyorID = conveyorID;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
