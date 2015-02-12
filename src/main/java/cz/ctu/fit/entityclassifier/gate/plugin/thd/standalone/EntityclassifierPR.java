/*
 * #%L
 * Stand Alone GATE plugin of the Entityclassifier.eu NER
 * %%
 * Copyright (C) 2015 Knowledge Engineering Group (KEG) and Web Intelligence Research Group (WIRG) - Milan Dojchinovski (milan.dojchinovski@fit.cvut.cz)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package cz.ctu.fit.entityclassifier.gate.plugin.thd.standalone;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Milan Dojchinovski
 <milan.dojchinovski@fit.cvut.cz>
 Twitter: @m1ci
 www: http://dojchinovski.mk
 */
@CreoleResource(name = "Entityclassifier.eu Stand-Alone PR",
        comment = "Perform named entity recognition over a GATE corpus")
public class EntityclassifierPR extends gate.creole.AbstractLanguageAnalyser
    implements ProgressListener {

    /**
     * Language of the texts is going to be processed. Default language is English.
     */
    private String lang = null;
  
    /**
     * Location of the entity extraction JAPE grammar.
     */
    private String entityExtractionGrammar = null;
    
    /**
     * Location of the config file.
     */
    private URL configFileUrl = null;
    
    /**
     * The location of the LHD core dataset.
     */
    private String lhdCoreLocation = null;
        
    /**
     * The location of the DBpedia Ontology on your disk.
     * http://wiki.dbpedia.org/Downloads39#dbpedia-ontology
     */
    private String dbpediaOntologyLocation = null;
    
    /**
     * Wikipedia Search API endpoints (used for disambiguation of the spotted entities).
     * The default are the one from the live Wikipedia.
     * If you run local Wikipedia mirror you can also specify your Wikipedia Search endpoint.
     * Note that on a local mirror, the processing is faster.
     */
    private String wikipediaSearchEndpoint = "http://en.wikipedia.org/w/api.php";
    
    /**
     * Types filter parameter.
     * 
     */
    private String typesFilter = null;
        
    /**
     * Parameter for the LHD inferred dataset location.
     * 
     */
    private String lhdInferredLocation = null;
    
    /**
     * Parameter to specify the types of entities to extract: named or common entities.
     * 
     */
    private String entityType = null;
    
    /** Initialise this resource, and return it. */
    public Resource init() throws ResourceInstantiationException {
        System.out.println("Initialising the plugin.");
        try {
            // Reading configuration file.
            Properties prop = new Properties();
            InputStream in = getConfigFileUrl().openStream();
            prop.load(in);
            in.close();
            
            String dbpediaOntologyDir = prop.get("dbpediaOntologyDir").toString();
            String lhdCoreLocationEn = prop.get("lhd10En").toString();
            String lhdCoreLocationDe = prop.get("lhd10De").toString();
            String lhdCoreLocationNl = prop.get("lhd10Nl").toString();
            String inferredEn = prop.getProperty("inferredEn");
            String inferredDe = prop.getProperty("inferredDe");
            String inferredNl = prop.getProperty("inferredNl");
                        
            this.dbpediaOntologyLocation = dbpediaOntologyDir;
            
            switch(lang) {
                case "en":
                    this.lhdInferredLocation = inferredEn;
                    this.lhdCoreLocation = lhdCoreLocationEn;
                    break;

                case "de":
                    this.lhdInferredLocation = inferredDe;
                    this.lhdCoreLocation = lhdCoreLocationDe;
                    break;

                case "nl":
                    this.lhdInferredLocation = inferredNl;
                    this.lhdCoreLocation = lhdCoreLocationNl;
                    break;
            }
            
            // Initialize the EntityClassifier.
            EntityClassifier.getInstance().init(
                    lang,
                    dbpediaOntologyDir, 
                    lhdCoreLocationEn, 
                    lhdCoreLocationDe, 
                    lhdCoreLocationNl, 
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
        AnnotationSet annSetTokensCheck = as_default.get("Token",futureMap);
        
        if(annSetTokensCheck.size()>0) {
            
            if(entityType.equals("all") || entityType.equals("ne")) {
                AnnotationSet annSetNamedEntities = as_default.get("NamedEntity",futureMap);
                ArrayList namedEntityAnnotations = new ArrayList(annSetNamedEntities);
                fireStatusChanged("Started linking and classifying named entities");
                // looop through the Token annotations

                for(int j = 0; j < namedEntityAnnotations.size(); ++j) {
                    try {

                        Annotation entity = (Annotation)namedEntityAnnotations.get(j);
                        Node isaStart = entity.getStartNode();
                        Node isaEnd = entity.getEndNode();
                        String underlyingString = document.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
                        String link = DBpediaLinker.getInstance().getDBpediaLink(underlyingString, wikipediaSearchEndpoint);

                        Set<String> set = new HashSet<String>();
                        if(link != null) {
                            FeatureMap entityFM = entity.getFeatures();
                            entityFM.put("itsrdf:taIdentRef", link);
                            ArrayList<String> typesList = EntityClassifier.getInstance().getClasses(link, typesFilter);
                            for(String typeURI : typesList){
                                set.add(typeURI);
                                if(typeURI.contains("/ontology")) {
                                    for(String derTypeURI : EntityClassifier.getInstance().deriveTypes(typeURI)) {
                                        set.add(derTypeURI);
                                    }
                                }
                            }
                            int counter = 0;
                            for(String type : set) {
                                entityFM.put("rdf:type"+counter, type);
                                counter++;
                            }
                        }
                    } catch (InvalidOffsetException ex) {
                        Logger.getLogger(EntityclassifierPR.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            if(entityType.equals("all") || entityType.equals("ce")) {
                AnnotationSet annSetCommonEntities = as_default.get("CommonEntity",futureMap);
                ArrayList commonEntityAnnotations = new ArrayList(annSetCommonEntities);
                // looop through the Token annotations

                for(int j = 0; j < commonEntityAnnotations.size(); ++j) {
                    try {

                        // get a token annotation
                        Annotation entity = (Annotation)commonEntityAnnotations.get(j);
                        // get the underlying string for the Token
                        Node isaStart = entity.getStartNode();
                        Node isaEnd = entity.getEndNode();
                        String underlyingString = document.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
                        String link = DBpediaLinker.getInstance().getDBpediaLink(underlyingString, wikipediaSearchEndpoint);

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
                            int counter = 0;
                            for(String type : set) {
                                entityFM.put("rdf:type"+counter, type);
                                counter++;
                            }
                        }
                    } catch (InvalidOffsetException ex) {
                        Logger.getLogger(EntityclassifierPR.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            throw new ExecutionException("No tokens to process in document "+document.getName()+"\n" +
                                     "Please run a tokenizer, sentence splitter"+
                                     "and POS tagger first!");
        }    
        // progress
        progressChanged(100);        
    }
    
    /**
   * Getter for the language parameter.
   * 
   */
    public String getLang() {
    return lang;
  }

   /**
   * Setter for the language parameter. 
   * 
   */
    @CreoleParameter(comment = "language parameter. You can choose between English - en, German - de, or Dutch - nl.",
            defaultValue="en")
    public void setLang(String lang) {
    this.lang = lang;
  }
  
    /**
    * Getter for the typesFilter parameter.
    * 
    */
    public String getTypesFilter() {
        return typesFilter;
    }

    /**
    * Setter for the typesFilter parameter.
    * 
    */
    @CreoleParameter(comment = "Specify whether you prefer the entities to be classified with DBpedia Ontology types (dbo), DBpedia resources (dbinstance), or both (all). ",
            defaultValue="all")
    @RunTime
    public void setTypesFilter(String typesFilter) {
        this.typesFilter = typesFilter;
    }
      
    /**
     * Getter for the DBpedia ontology location.
     */
    public String getDbpediaOntologyLocation() {
        return dbpediaOntologyLocation;
    }
    
    /**
     * Setter for the DBpedia ontology location.
     * 
     */
    @CreoleParameter(comment = "Location on your disk for the DBpedia Ontology.")
    @RunTime
    public void setDbpediaOntologyLocation(String dbpediaOntologyLocation) {
        this.dbpediaOntologyLocation = dbpediaOntologyLocation;
    }
    
    /**
     * Getter for the LHD core datasets location.
     * 
     */
    public String getLhdCoreLocation() {
        return lhdCoreLocation;
    }
  
    /**
     * Setter for the LHD core dataset location.
     * 
     */
    @CreoleParameter(comment = "Location on your disk for the LHD Core dataset.")
    @RunTime
    public void setLhdCoreLocation(String lhdCoreLocation) {
        this.lhdCoreLocation = lhdCoreLocation;
    }
    
    /**
     * Setter for the LHD inferred dataset location.
     * 
     */
    @CreoleParameter(comment = "Location on your disk for the LHD inferred dataset.")
    @RunTime
    public void setLhdInferredLocation(String lhdInferredLocation) {
        this.lhdInferredLocation = lhdInferredLocation;
    }
    
    /**
     * Getter for the LHD inferred dataset location.
     * 
     */
    public String getLhdInferredLocation() {
        return lhdInferredLocation;
    }
      
    /**
     * Getter for the Wikipedia Search API endpoint.
     * 
     */
    public String getWikipediaSearchEndpoint() {
        return wikipediaSearchEndpoint;
    }
  
    /**
     * Setter for the Wikipedia Search API endpoint.
     * 
     */
    @CreoleParameter(comment = "The endpoint for the Wikipedia Search API.")
    @RunTime
    @Optional
    public void setWikipediaSearchEndpoint(String wikipediaSearchEndpoint) {
        this.wikipediaSearchEndpoint = wikipediaSearchEndpoint;
    }
    
    /**
     * Getter for the config file.
     * 
     */
    public URL getConfigFileUrl() {
        return configFileUrl;
    }

    /**
     * Setter for the config file.
     * 
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

    /**
     * @return the entityType
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    @CreoleParameter(comment = "Specify the types of entities to be processed from the text. Named or common entities. Possibel values: ne/ce/all  ",
            defaultValue="ne")
    @RunTime
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }    
}
