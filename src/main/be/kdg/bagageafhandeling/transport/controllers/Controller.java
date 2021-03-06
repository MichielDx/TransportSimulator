package main.be.kdg.bagageafhandeling.transport.controllers;

import main.be.kdg.bagageafhandeling.transport.engines.BaggageScheduler;
import main.be.kdg.bagageafhandeling.transport.engines.DayScheduler;
import main.be.kdg.bagageafhandeling.transport.engines.RouteScheduler;
import main.be.kdg.bagageafhandeling.transport.models.enums.DelayMethod;
import main.be.kdg.bagageafhandeling.transport.models.enums.FormatOption;
import main.be.kdg.bagageafhandeling.transport.models.enums.SimulatorMode;
import main.be.kdg.bagageafhandeling.transport.models.FrequencySchedule;
import main.be.kdg.bagageafhandeling.transport.models.TimePeriod;
import main.be.kdg.bagageafhandeling.transport.repository.BaggageRepository;
import main.be.kdg.bagageafhandeling.transport.repository.ConveyorRepository;
import main.be.kdg.bagageafhandeling.transport.services.Publisher;
import main.be.kdg.bagageafhandeling.transport.services.PublisherXmlServiceImpl;
import main.be.kdg.bagageafhandeling.transport.services.Retriever;
import main.be.kdg.bagageafhandeling.transport.services.bagage.*;
import main.be.kdg.bagageafhandeling.transport.services.interfaces.ConveyorService;
import main.be.kdg.bagageafhandeling.transport.services.interfaces.MessageInputService;
import main.be.kdg.bagageafhandeling.transport.services.interfaces.MessageOutputService;
import main.be.kdg.bagageafhandeling.transport.services.interfaces.RecorderConversionService;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.*;

/**
 * Created by Arthur Haelterman on 4/11/2016.
 */
public class Controller {
    private String logpath = new File("src/main/log4j.properties").getAbsolutePath();
    //private String log4jConfPath = "C:\\Users\\Arthur Haelterman\\SoftwareEngineering3\\src\\main\\log4j.properties";
    private FrequencySchedule f;
    private DayScheduler dayScheduler;
    private BaggageScheduler baggageScheduler;
    private RouteScheduler routeScheduler;
    private Thread day;
    private Thread bagage;
    private FormatOption option;
    private SimulatorMode mode;
    private DelayMethod method;
    private String recordPath;
    private ConveyorService conveyorService;
    private MessageInputService routeInputQueue;
    private MessageOutputService sensorOutputQueue;
    private MessageOutputService baggageOutputQueue;
    private BaggageRecorder baggageRecorder;
    private BaggageReader baggageReader;
    private long timeToCacheClear;
    private boolean record;


    public Controller() {
    }

    public void initialize() {

        f = getFrequencySchedule();
        PropertyConfigurator.configure(logpath);

        initializeRecorderAndReader();

        PublisherXmlServiceImpl publisherXmlService = new PublisherXmlServiceImpl();
        Publisher sensorMessagePublisher = new Publisher(sensorOutputQueue, publisherXmlService);
        Publisher baggagePublisher = new Publisher(baggageOutputQueue, publisherXmlService);
        BaggageRepository baggageRepository = new BaggageRepository();
        ConveyorRepository conveyorRepository = new ConveyorRepository();
        baggageScheduler = new BaggageScheduler(baggageRepository, baggagePublisher, f.getCurrentTimePeriod(), mode, baggageRecorder, baggageReader);
        routeScheduler = new RouteScheduler(baggageRepository, conveyorRepository, method, 2000, getSecurityList(), conveyorService, sensorMessagePublisher);
        dayScheduler = new DayScheduler(f);
        dayScheduler.addObserver(baggageScheduler);
        Retriever routeInputRetriever = new Retriever(routeInputQueue, routeScheduler);

        day = new Thread(dayScheduler);
        bagage = new Thread(baggageScheduler);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                conveyorRepository.clearCache();
            }
        }, new Date(), timeToCacheClear);
    }

    public void start() {
        day.start();
        bagage.start();
    }

    private void initializeRecorderAndReader() {
        RecorderConversionService service;
        if (option == FormatOption.JSON) {
            service = new RecorderJsonService();
        } else {
            service = new RecorderXmlService();
        }
        if (record) baggageRecorder = new BaggageRecorder(recordPath, service);
        if (mode == SimulatorMode.REPLAY) baggageReader = new BaggageReader(recordPath, service);
    }


    public void setOption(FormatOption option) {
        this.option = option;
    }

    public void setMode(SimulatorMode mode) {
        this.mode = mode;
    }

    public void setMethod(DelayMethod method) {
        this.method = method;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    public void setConveyorService(ConveyorService conveyorService) {
        this.conveyorService = conveyorService;
    }

    public void setRouteInputQueue(MessageInputService routeInputQueue) {
        this.routeInputQueue = routeInputQueue;
    }

    public void setSensorOutputQueue(MessageOutputService sensorOutputQueue) {
        this.sensorOutputQueue = sensorOutputQueue;
    }

    public void setBaggageOutputQueue(MessageOutputService baggageOutputQueue) {
        this.baggageOutputQueue = baggageOutputQueue;
    }

    private Map<Integer, Integer> getSecurityList() {
        Map<Integer, Integer> hashMap = new HashMap<>();
        return hashMap;
    }


    private FrequencySchedule getFrequencySchedule() {
        ArrayList<TimePeriod> periods = new ArrayList<>();
        periods.add(new TimePeriod(0, 2, 5000));
        periods.add(new TimePeriod(2, 6, 10000));
        periods.add(new TimePeriod(6, 12, 3000));
        periods.add(new TimePeriod(12, 16, 2000));
        periods.add(new TimePeriod(16, 20, 1000));
        periods.add(new TimePeriod(20, 24, 4000));
        return new FrequencySchedule(periods);
    }

    public void setClearCacheTime(long time){
        this.timeToCacheClear =time;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }
}
