/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vse.fis.keg.entityclassifier.gate.plugin.sa;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Node;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.event.ProgressListener;
import gate.util.InvalidOffsetException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
@CreoleResource(name = "Entityclassifier.eu Stand-Alone PR", comment = "Perform named entity recognition over a GATE corpus")
public class EntityclassifierPR extends gate.creole.AbstractLanguageAnalyser
    implements ProgressListener {

    /**
     * Language of the texts is going to be processed. Default language is English
     */
    private String lang = "en";
  
    /**
     * Location of the entity extraction JAPE grammars for each language (English, German, Dutch).
     */
    private String entityExtractionGrammar = null;
    
    private URL configFileUrl = null;
    
    /**
     * The location of the LHD v1.0 datasets for each language on your disk.
     */
    private String lhd10Location = "/Users/Milan/Documents/research/repositories/linked-tv/code/thd-v04/scripts/datasets/lhd-2.3.8/1.0/en.LHDv1.draft.nt";
        
    /**
     * The location of the DBpedia Ontology on your disk.
     * http://wiki.dbpedia.org/Downloads39#dbpedia-ontology
     */
    private String dbpediaOntologyLocation = "/Users/Milan/Documents/research/repositories/linked-tv/code/thd-v04/scripts/datasets/dbpedia-3.9/dbpedia_3.9.owl";
    
    /**
     * Wikipedia Search API endpoints (used for disambiguation of the spotted entities).
     * The default are the one from the live Wikipedia.
     * If you run local Wikipedia mirror you can also specify your Wikipedia Search endpoint.
     * Note that on a local mirror, the processing is faster.
     */
    private String wikipediaSearchEndpoint = "http://en.wikipedia.org/w/api.php";
    
    /**
     * typesFilter.
     * Note that on a local mirror, the processing is faster.
     */
    private String typesFilter = null;
        
    /**
     * lhdInferredLocation.
     * 
     */
    private String lhdInferredLocation = null;
    
    /** Initialise this resource, and return it. */
    public Resource init() throws ResourceInstantiationException {
        
        try {
            // Reading configuration file.
            Properties prop = new Properties();
            InputStream in = getConfigFileUrl().openStream();
            prop.load(in);
            in.close();
            
            String dbpediaOntologyDir = prop.get("dbpediaOntologyDir").toString();
            String lhd10En = prop.get("lhd10En").toString();
            String lhd10De = prop.get("lhd10De").toString();
            String lhd10Nl = prop.get("lhd10Nl").toString();
            String inferredEn = prop.getProperty("inferredEn");
            String inferredDe = prop.getProperty("inferredDe");
            String inferredNl = prop.getProperty("inferredNl");
            
            switch(lang) {
                case "en":
                    this.lhdInferredLocation = inferredEn;
                    break;

                case "de":
                    this.lhdInferredLocation = inferredDe;
                    break;

                case "nl":
                    this.lhdInferredLocation = inferredNl;
                    break;
            }
            
            // Initialize the EntityClassifier.
            EntityClassifier.getInstance().init(
                    lang,
                    dbpediaOntologyDir, 
                    lhd10En, 
                    lhd10De, 
                    lhd10Nl, 
                    inferredEn, 
                    inferredDe, 
                    inferredNl);
            
            fireStatusChanged("Finished initializing.");
            
            return this;
        } catch (IOException ex) {
            Logger.getLogger(EntityclassifierPR.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return this;
    }
    
    /* this method is called to reinitialize the resource */
    public void reInit() throws ResourceInstantiationException {
        // reinitialization code
        init();
    }
    
    /**
     * Should be called to execute this PR on a document.
     */
    public void execute() throws ExecutionException {
        
        System.out.println("Processing started.");
        
        fireStatusChanged("Checking runtime parameters");
        progressChanged(0);
        
        // if no document provided
        if(document == null) { throw new ExecutionException("Document is null!"); }
        
        String documentContent = document.getContent().toString();
        
        if(documentContent.trim().length() == 0) return;
        
        FeatureMap futureMap = null;
        AnnotationSet as_default = document.getAnnotations();
        AnnotationSet annSetTokens = as_default.get("NamedEntity",futureMap);
        
        ArrayList tokenAnnotations = new ArrayList(annSetTokens);

        fireStatusChanged("Started linking and classifying named entities");
        // looop through the Token annotations
        for(int j = 0; j < tokenAnnotations.size(); ++j) {
            try {
                
                // get a token annotation
                Annotation entity = (Annotation)tokenAnnotations.get(j);
                
                // get the underlying string for the Token
                Node isaStart = entity.getStartNode();
                Node isaEnd = entity.getEndNode();
                String underlyingString = document.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
//                System.out.println("NamedEntity: " + underlyingString);
                String link = DBpediaLinker.getInstance().getDBpediaLink(underlyingString);
//                System.out.println(link);
                
                Set<String> set = new HashSet<String>();
                
                if(link != null) {
                    
                    FeatureMap entityFM = entity.getFeatures();
                    entityFM.put("itsrdf:taIdentRef", link);
                        
                    ArrayList<String> typesList = EntityClassifier.getInstance().getClasses(link, typesFilter);
                    for(String typeURI : typesList){
                        set.add(typeURI);
                        if(typeURI.contains("/ontology")){
                            for(String derTypeURI : EntityClassifier.getInstance().deriveTypes(typeURI)) {
                                set.add(derTypeURI);
                            }
                        }
                    }
                    
//                    String types = "";
                    int counter = 0;
                    for(String type : set) {
//                        types+="["+type+"]";
                        entityFM.put("rdf:type"+counter, type);
                        counter++;
                    }
                }
            } catch (InvalidOffsetException ex) {
                Logger.getLogger(EntityclassifierPR.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
        // progress
        progressChanged(100);
        
    }
    
    /**
   * The language parameter
   * 
   */
    public String getLang() {
    return lang;
  }

    /**
   * developer key. One has to obtain this from Zemanta by creating an account
   * online
   */
    @CreoleParameter(comment = "language parameter. You can choose between English - en, German - de, or Dutch - nl.",
            defaultValue="en")
    public void setLang(String lang) {
    this.lang = lang;
  }
  
    /**
    * The onlyDBpediaOntologyTypes parameter
    * 
    */
    public String getTypesFilter() {
        return typesFilter;
    }

    /**
    * onlyDBpediaOntologyTypes
    * 
    */
    @CreoleParameter(comment = "Specify whether you want only DBpedia Ontology types (yes) or not (no).",
            defaultValue="all")
    @RunTime
    public void setTypesFilter(String typesFilter) {
        this.typesFilter = typesFilter;
    }
      
    /**
     * Getters and setters for the LHD datasets location.
     */
    public String getDbpediaOntologyLocation() {
        return dbpediaOntologyLocation;
    }
    
    @CreoleParameter(comment = "Location on your disk for the DBpedia Ontology.")
    @RunTime
    public void setDbpediaOntologyLocation(String dbpediaOntologyLocation) {
        this.dbpediaOntologyLocation = dbpediaOntologyLocation;
    }
    
    /**
     * Getters and setters for the LHD datasets location.
     */
    public String getLhd10Location() {
        return lhd10Location;
    }
  
    @CreoleParameter(comment = "Location on your disk for the v1.0 dataset.")
    @RunTime
    public void setLhd10Location(String lhd10Location) {
        this.lhd10Location = lhd10Location;
    }
    
    @CreoleParameter(comment = "Inferred dataset.")
    @RunTime
    public void setLhdInferredLocation(String lhdInferredLocation) {
        this.lhdInferredLocation = lhdInferredLocation;
    }
    
    /**
     * Getters and setters for the LHD datasets location.
     */
    public String getLhdInferredLocation() {
        return lhdInferredLocation;
    }
      
    /**
     * Getters and setters for the Wikipedia Search API endpoints.
     */
    public String getWikipediaSearchEndpoint() {
        return wikipediaSearchEndpoint;
    }
  
    @CreoleParameter(comment = "The live endpoint for the English Wikipedia Search API.")
    @RunTime
    @Optional
    public void setWikipediaSearchEndpoint(String wikipediaSearchEndpoint) {
        this.wikipediaSearchEndpoint = wikipediaSearchEndpoint;
    }
    
    /**
    * @return the configFileUrl
    */
    public URL getConfigFileUrl() {
        return configFileUrl;
    }

    /**
    * @param configFileUrl the configFileUrl to set
    */
    @CreoleParameter(defaultValue = "config.properties")
    public void setConfigFileUrl(URL configFileUrl) {
        this.configFileUrl = configFileUrl;
    }
  
    @Override
    public void progressChanged(int i) {
        fireProgressChanged(i);
    }

    @Override
    public void processFinished() {
        fireProcessFinished();
    }
    
}
