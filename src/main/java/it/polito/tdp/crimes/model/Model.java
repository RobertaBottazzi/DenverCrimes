package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private SimpleWeightedGraph<String,DefaultWeightedEdge> grafo; //non serve idMap perchè ho dei vertici che non sono oggetti ma delle "cose semplici" come le stringhe
	private EventsDao dao;
	private List<String> percorsoMigliore;
	
	public Model() {
		dao= new EventsDao();
	}
	
	public void creaGrafo(String categoria, int mese) {
		grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiunta vertici
		Graphs.addAllVertices(grafo, dao.getVertici(categoria, mese));
		//aggiunta archi
		for(Adiacenza a:dao.getAdiacenze(categoria, mese)) {
			if(this.grafo.getEdge(a.getV1(), a.getV2())==null)
				Graphs.addEdgeWithVertices(grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		//System.out.println(grafo.edgeSet().size());
		//System.out.println(grafo.vertexSet().size());
	}
	
	public List<Adiacenza> getArchiSuperioriAllaMedia(){
		//calcolo il peso medio di tutti gli archi
		double pesoMedio=0.0;
		for(DefaultWeightedEdge e: grafo.edgeSet()) {
			pesoMedio+=this.grafo.getEdgeWeight(e);
		}
		pesoMedio=pesoMedio/this.grafo.edgeSet().size();
		//filtro gli archi tenendo solo quelli con peso>pesoMedio
		List<Adiacenza> result= new ArrayList<>();
		for(DefaultWeightedEdge e: grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>pesoMedio)
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e), this.grafo.getEdgeWeight(e)));
		}
		return result;
	}
	
	public List<String> trovaPercorso(String sorgente, String destinazione){
		percorsoMigliore= new ArrayList<>();
		List<String> parziale= new ArrayList<>();
		parziale.add(sorgente);
		cerca(destinazione,parziale);
		return percorsoMigliore;
	}
	
	private void cerca(String destinazione, List<String> parziale) {
		//caso terminale
		if(parziale.get(parziale.size()-1).equals(destinazione)) { //ultimo elemento inserito in parizale è destinazione allora ho finito la ricerca
			if(parziale.size()>this.percorsoMigliore.size()) //se parziale ha percorso un numero di archi maggiore di percorsoMigliore a sovrascrivo
				percorsoMigliore=new LinkedList<>(parziale);
			return;
		}
		//altrimenti..
		//ciclo sui vicini dell'ultimo elemento di parziale e provo ad aggiungerli uno ad uno tornando indietro con il backtracking
		for(String vicino: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))){ //metodo per prendere i vicini di un nodo
			//non permessi cicli, quindi non posso reinserire un nodo che ho già inserito
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione,parziale);
				parziale.remove(parziale.size()-1);
			}
		}
		
	}


	public List<String> getCategorie(){
		return dao.getCategorie();
	}
}
