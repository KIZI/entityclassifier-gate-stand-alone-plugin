# Entityclassifier.eu Stand-Alone Plugin for GATE #

This is a GATE **stand-alone** plugin for the Entityclassifier.eu NER system. You can use it perform Named Entity Recognition over English, German and Dutch written texts. All resources you need for entity spotting, disambiguation and classification are provided with the plugin. If you want, you can use the **light** version of the plugin which is communicating with our REST API endpoint. Using this plugin you can perform:

* ***Entity Spotting*** - each detected named entity in the text will be marked with ```NamedEntity``` annotation.

* ***Entity Disambiguation*** - each spotted entity is further disambiguated with a an URI from the *DBpedia namespace*. E.g. http://dbpedia.org/resource/Prague for the entity Prague.

* ***Entity Classification*** - for each disambiguated entity we provide set of types represented as *DBpedia instances* or *DBpedia Ontology* types. When using the plugin you can set the ```typesFilter``` parameter to filter out only types as DBpedia instances, DBpedia Ontology types, or both. The possible parameter values are:
    * **dbo** - filter only DBpedia Ontology types
    * **dbinstance** - filter only types defined as DBpedia instances
    * **all** - the entity types can be either DBpedia Ontology clases or DBpedia instances


### How to start using it? ###

#### Steps: ####

1. **Add our plugin repository.** Open ```CREOLE Plugin Manager -> Configuration``` and add the following repository

    ```
    http://ner.vse.cz/GATE/gate-update-site.xml
    ```

2. **Install the plugin.**  In the CREOLE Plugin Manager open the ```Available to Install``` tab, check the ```Entityclassifier NER Light``` plugin and click ```Apply All```.

3. **Request a free API key for the REST API.** Request an API key using the Web form http://entityclassifier.eu/thd/docs/#apikeyformfree!

4. **Enable the plugin.** In the CREOLE Plugin Manager go to ```Available to Install```, search for ```Entityclassifier_NER_Light``` and select load now and load always.

5. **Run the build script found in the ```script``` folder.** It will download all required datasets, compile and prepare all required configuration files. It will take some minutes.

    ```
    sh build.sh
    ```

6. **Create a corpus pipeline.**

    * Document Reset PR
    * ANNIE English Tokeniser PR
    * ANNIE Sentence Splitter PR
    * ANNIE POS Tagger PR
    * JAPE Transducer PR - with a JAPE grammar which will perform named entity spotting. For English use the ```en_entity_extraction-v1.jape```. We also provide grammars for entity spotting for German and Dutch. The grammars are located in ```data/entity-extraction-grammars```
    * Entityclassifier.eu Stand-Alone PR - create an instance of classifier processing resource and add it at the end of the pipeline. When instantiating you can specify the language of the text on which you will run named entity recognition.

6. **Create a document corpus and run the pipeline.**

7. **Check the results!** - the spotted entities are annotated as ```NamedEntity``` annotations. Each entity has a ```disambiguation URI``` which is encoded as annotation feature ```itsrdf:taIdentRef=...```. Each assigned type is also present as annotation feature in the form of ```rdf:typeX=...```

![entityclassifier-sa-gate-plugin-ss-1.png](https://bitbucket.org/repo/dAnKEK/images/3433177732-entityclassifier-sa-gate-plugin-ss-1.png)

***Enjoy discovering entities!***



If you need any help/support with the plugin free to contact us. Bugs please report as issues to this repository.

License
------

Licensed under the [GNU General Public License Version 3 (GNU GPLv3)](http://www.gnu.org/licenses/gpl.html).

Copyright (c) 2014

* Milan Dojchinovski - <milan.dojchinovski@fit.cvut.cz>

* Tomas Kliegr - <tomas.kliegr@vse.cz>