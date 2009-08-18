package playground.christoph.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.population.PlanElement;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.MatsimConfigReader;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkFactory;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.utils.misc.Time;

import playground.christoph.knowledge.container.MapKnowledgeDB;
import playground.christoph.knowledge.container.NodeKnowledge;
import playground.christoph.knowledge.container.dbtools.DBConnectionTool;
import playground.christoph.knowledge.nodeselection.SelectNodesDijkstra;
import playground.christoph.mobsim.MyQueueNetwork;
import playground.christoph.network.util.SubNetworkCreator;
import playground.christoph.network.util.SubNetworkTools;
import playground.christoph.router.DijkstraWrapper;
import playground.christoph.router.KnowledgePlansCalcRoute;
import playground.christoph.router.MyDijkstra;
import playground.christoph.router.costcalculators.KnowledgeTravelCostWrapper;
import playground.christoph.router.costcalculators.KnowledgeTravelTimeCalculator;
import playground.christoph.router.costcalculators.KnowledgeTravelTimeWrapper;
import playground.christoph.router.costcalculators.OnlyTimeDependentTravelCostCalculator;
import playground.christoph.router.util.KnowledgeTools;

public class TestSubNetwork {

	private final static Logger log = Logger.getLogger(TestSubNetwork.class);
	
	private NetworkLayer network;
	private PopulationImpl population;
	private Config config;
	private PersonImpl person;
	private SelectNodesDijkstra selectNodesDijkstra;
	private Map<Id, NodeImpl> nodesMap;

	private KnowledgeTravelTimeCalculator travelTime;
	private KnowledgeTravelTimeWrapper travelTimeWrapper;
	private OnlyTimeDependentTravelCostCalculator travelCost;
	private KnowledgeTravelCostWrapper travelCostWrapper;
	
	private Dijkstra dijkstra;
	private DijkstraWrapper dijkstraWrapper;
	private KnowledgePlansCalcRoute dijkstraRouter;
	private NetworkFactory networkFactory;
	private MyQueueNetwork myQueueNetwork;
	
	private final String configFileName = "mysimulations/kt-zurich/config.xml";
	private final String dtdFileName = null;
	private final String networkFile = "mysimulations/kt-zurich/input/network.xml";
	private final String populationFile = "mysimulations/kt-zurich/input/plans.xml";
	
	public static void main(String[] args)
	{
		new TestSubNetwork();
	}
	
	public TestSubNetwork()
	{	
		DBConnectionTool dbct = new DBConnectionTool();
		dbct.connect();		 
		
		loadNetwork();
		loadPopulation();
		loadConfig();
				
		log.info("Network size: " + network.getLinks().size());
		log.info("Population size: " + population.getPersons().size());
				
		setKnowledgeStorageHandler();
		
		initReplanner();
		
		createSubNetworks();
	}
	
	private void loadNetwork()
	{
		networkFactory = new NetworkFactory(this.network);
		networkFactory.setLinkFactory(new MyLinkFactoryImpl());

		network = new NetworkLayer();
		network.setFactory(networkFactory);
		
		new MatsimNetworkReader(network).readFile(networkFile);
		
		log.info("Loading Network ... done");
	}
	
	private void loadPopulation()
	{
		population = new PopulationImpl();
		new MatsimPopulationReader(population, network).readFile(populationFile);
		log.info("Loading Population ... done");
	}
	
	private void setKnowledgeStorageHandler()
	{
		for(PersonImpl person : population.getPersons().values())
		{
			Map<String, Object> customAttributes = person.getCustomAttributes();

			customAttributes.put("NodeKnowledgeStorageType", MapKnowledgeDB.class.getName());
			
			MapKnowledgeDB mapKnowledgeDB = new MapKnowledgeDB();
			mapKnowledgeDB.setPerson(person);
			mapKnowledgeDB.setNetwork(network);
			
			customAttributes.put("NodeKnowledge", mapKnowledgeDB);
		}
	}
	
	private void initReplanner()
	{
		travelTime = new KnowledgeTravelTimeCalculator();
		travelTimeWrapper = new KnowledgeTravelTimeWrapper(travelTime);
		travelCost = new OnlyTimeDependentTravelCostCalculator(travelTimeWrapper);
		travelCostWrapper = new KnowledgeTravelCostWrapper(travelCost);
		
		travelTimeWrapper.checkNodeKnowledge(false);
		travelCostWrapper.checkNodeKnowledge(false);
		travelTimeWrapper.useLookupTable(false);
		travelCostWrapper.useLookupTable(false);
		
		// Don't use Knowledge for CostCalculations
		dijkstra = new MyDijkstra(network, travelCostWrapper, travelTimeWrapper);
		// dijkstra = new Dijkstra(network, travelCostWrapper, travelTimeWrapper);
		dijkstraWrapper = new DijkstraWrapper(dijkstra, travelCostWrapper, travelTimeWrapper, network);
	
		dijkstraRouter = new KnowledgePlansCalcRoute(network, dijkstraWrapper, dijkstraWrapper);
		
		myQueueNetwork = new MyQueueNetwork(network);
		dijkstraRouter.setMyQueueNetwork(myQueueNetwork);
	}
	
	private void createSubNetworks()
	{
		SubNetworkCreator snc = new SubNetworkCreator(this.network);
		SubNetworkTools snt = new SubNetworkTools();
		KnowledgeTools kt = new KnowledgeTools();
		
		int i = 0;
		for(PersonImpl person : population.getPersons().values())
		{
			Map<String, Object> customAttributes = person.getCustomAttributes();

			NodeKnowledge nk = kt.getNodeKnowledge(person);
			
			((MapKnowledgeDB)nk).readFromDB();
			
			SubNetwork subNetwork = snc.createSubNetwork(nk);

			customAttributes.put("SubNetwork", subNetwork);
			
//			if(!checkNodeKnowledge(subNetwork, nk)) log.error("Not all Nodes included!");
	
			replanRoute(person);
			
//			log.info("Nodes: " + subNetwork.getNodes().size() + ", Links: " + subNetwork.getLinks().size());
			
			// clear memory
			((MapKnowledgeDB)nk).clearLocalKnowledge();
			snt.resetSubNetwork(person);
			
			i++;
			if (i % 1000 == 0) log.info("Subnetworks: " + i);
//			customAttributes.put("SubNetwork", new SubNetwork(this.network));
		}
	}
	
	private boolean checkNodeKnowledge(SubNetwork subNetwork, NodeKnowledge nodeKnowledge)
	{
		for (NodeImpl node : nodeKnowledge.getKnownNodes().values())
		{
			if (!subNetwork.getNodes().containsKey(node.getId())) return false;
		}
		
		for(Link link : subNetwork.getLinks().values())
		{
			if(!nodeKnowledge.getKnownNodes().containsKey(link.getToNode().getId())) return false;
			if(!nodeKnowledge.getKnownNodes().containsKey(link.getFromNode().getId())) return false;
		}
		
		return true;
	}
	
	private void replanRoute(PersonImpl person)
	{
//		dijkstraRouter.setPerson(person);
		dijkstraRouter.setTime(Time.UNDEFINED_TIME);
		
		dijkstraRouter.run(person.getSelectedPlan());
	}
	
	private void loadConfig()
	{
		this.config = new Config();
		this.config.addCoreModules();
		this.config.checkConsistency();
		try {
			new MatsimConfigReader(this.config).readFile(this.configFileName, this.dtdFileName);
		} catch (IOException e) {
			log.error("Problem loading the configuration file from " + this.configFileName);
			throw new RuntimeException(e);
		}
		Gbl.setConfig(config);
		log.info("Loading Config ... done");
	}
}
