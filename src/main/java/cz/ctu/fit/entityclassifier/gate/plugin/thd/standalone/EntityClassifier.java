/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vse.fis.keg.entityclassifier.gate.plugin.sa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.InputStream;
import java.util.ArrayList;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class EntityClassifier {
    
    private static EntityClassifier instance = null;
    private static Model lhdModel10 = null;
    private static Model inferredModel = null;
    private static Model dbpediaOntologyModel = null;
    
    private static String lang = null;
    private static String dbpediaOntology = null;
    private static String lhd10En = null;
    private static String lhd10De = null;
    private static String lhd10Nl = null;
    private static String inferredEn = null;
    private static String inferredDe = null;
    private static String inferredNl = null;
    
    public static EntityClassifier getInstance(){
    
        if(instance == null) {
            instance = new EntityClassifier();
        }
        return instance;
    }
    
    public void init( 
            String lang,
            String dbpediaOntology,
            String lhd10En,
            String lhd10De,
            String lhd10Nl,
            String inferredEn,
            String inferredDe,
            String inferredNl ) {
        
        this.lang = lang;
        this.dbpediaOntology = dbpediaOntology;
        this.lhd10En = lhd10En;
        this.lhd10De = lhd10De;
        this.lhd10Nl = lhd10Nl;
        this.inferredEn = inferredEn;
        this.inferredDe = inferredDe;
        this.inferredNl = inferredNl;
        
        loadLHD();
    }
    
    public ArrayList getClasses(String entityURI, String typesFilter) {
        
        ArrayList typesList = new ArrayList<String>();
        
        StmtIterator typeIter = lhdModel10.listStatements( new SimpleSelector(lhdModel10.createResource(entityURI), RDF.type,  (RDFNode)null));
        while(typeIter.hasNext()) {
            
            Statement stm = typeIter.next();
            String typeURI = stm.getObject().asResource().getURI();
            
            switch(typesFilter) {
                case "dbo":
                    if(typeURI.contains("/resource/")) {
                        // The type is DBpedia instance.
                        StmtIterator dbOntoTypesIter = inferredModel.listStatements( new SimpleSelector(inferredModel.createResource(typeURI), RDFS.subClassOf,  (RDFNode)null));
                        while(dbOntoTypesIter.hasNext()) {
                            Statement ontoStm = dbOntoTypesIter.next();
                            typesList.add(ontoStm.getObject().asResource().getURI());
                        }                        
                    } else {
                        typesList.add(typeURI);                    
                    }
                break;
                
                case "dbinstance":
                    if(typeURI.contains("/resource/")) {
                        // The type is DBpedia instance.
                        typesList.add(typeURI);
                    }
                break;
                
                case "all":                    
                    typesList.add(typeURI);
                    if(typeURI.contains("/resource/")) {
                        // The type is DBpedia instance.
                        StmtIterator dbOntoTypesIter = inferredModel.listStatements( new SimpleSelector(inferredModel.createResource(typeURI), RDFS.subClassOf,  (RDFNode)null));
                        while(dbOntoTypesIter.hasNext()) {
                            Statement ontoStm = dbOntoTypesIter.next();
                            typesList.add(ontoStm.getObject().asResource().getURI());
                        }                        
                    }
                break;
            }
        }
        
        return typesList;
    }
    
    public ArrayList<String> deriveTypes(String typeURI) {
        
        ArrayList derivedTypes = new ArrayList<String>();
        
        StmtIterator typeIter = dbpediaOntologyModel.listStatements( new SimpleSelector(dbpediaOntologyModel.createResource(typeURI), RDFS.subClassOf,  (RDFNode)null));
        while(typeIter.hasNext()) {
            Statement stm = typeIter.next();
            Resource derivedTypeRes = stm.getObject().asResource();
            String derivedTypeURI = derivedTypeRes.getURI();
//            System.out.println(derivedTypeURI);
            derivedTypes.add(derivedTypeURI);
            typeIter = dbpediaOntologyModel.listStatements( new SimpleSelector(derivedTypeRes, RDFS.subClassOf,  (RDFNode)null));            
        }
        return derivedTypes;
    }
    
    public static void loadLHD() {
        
        System.out.println("Started loading the LHD dataset ...");
        lhdModel10 = ModelFactory.createDefaultModel();
        inferredModel = ModelFactory.createDefaultModel();
        dbpediaOntologyModel = ModelFactory.createDefaultModel();
        
        InputStream inDBpediaOnto = FileManager.get().open( dbpediaOntology );
        dbpediaOntologyModel.read(inDBpediaOnto, null, "RDF/XML");
                
        switch(lang) {
            case "en":
                InputStream in1en = FileManager.get().open( lhd10En );
                InputStream in2en = FileManager.get().open( inferredEn );
                lhdModel10.read(in1en, null, "N-TRIPLE");
                inferredModel.read(in2en, null, "N-TRIPLE");
                break;
            case "de":
                InputStream in1de = FileManager.get().open( lhd10De );
                InputStream in2de = FileManager.get().open( inferredDe );
                lhdModel10.read(in1de, null, "N-TRIPLE");
                inferredModel.read(in2de, null, "N-TRIPLE");
                break;
            case "nl":
                InputStream in1nl = FileManager.get().open( lhd10Nl );
                InputStream in2nl = FileManager.get().open( inferredNl );
                lhdModel10.read(in1nl, null, "N-TRIPLE");
                inferredModel.read(in2nl, null, "N-TRIPLE");
                break;
        }        
        System.out.println("Finished loading the LHD datasets.");
    }
}
