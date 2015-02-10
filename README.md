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

1. **Add our plugin repository.** Open ```CREOLE Plugin Manager -> Configuration```, check if the ```User Plugin Directory``` is set, otherwise configure it. Finally, add the following repository to the list of plugin repositories:

    ```
    http://ner.vse.cz/GATE/gate-update-site.xml
    ```

2. **Install the plugin.**  In the ```CREOLE Plugin Manager``` go to ```Available to Install``` tab, check ```Entityclassifier_NER_Stand_Alone``` and click ```Apply all```, then go to the ```Available plugins``` tab and select ```load now``` and ```load always```.

3. **Run the build script.** The script can be found in the ```../GATE_Developer_8.0/plugins/Entityclassifier_NER_Stand_Alone/script``` folder. It will download all required datasets, compile and prepare all required configuration files. This can take a few minutes.

    ```
    bash build.sh
    ```

4. **Create a corpus pipeline.**

    * Document Reset PR
    * ANNIE English Tokeniser PR
    * ANNIE Sentence Splitter PR
    * ANNIE POS Tagger PR (for English) - **for German and Dutch you should use the [GenericTagger](https://gate.ac.uk/sale/tao/splitch23.html#x28-54000023.3) POS plugin**. For this you need also to install the binaries. Instructions on how to install the German and Dutch binaries can be found [here](http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/).
    * JAPE Transducer PR - with a JAPE grammar which will perform named entity spotting. For English use the ```en_entity_extraction-v1.jape```. We also provide grammars for entity spotting for German and Dutch. The grammars are located in ```../GATE_Developer_8.0/plugins/Entityclassifier_NER_Stand_Alone/data/entity-extraction-grammars``` folder
    * Entityclassifier.eu Stand-Alone PR - create an instance of classifier processing resource and add it at the end of the pipeline. When instantiating you can specify the language of the text on which you will run named entity recognition.

5. **Create a document corpus and run the pipeline.**

6. **Check the results!** - the spotted entities are annotated as ```NamedEntity``` annotations. Each entity has a ```disambiguation URI``` which is encoded as annotation feature ```itsrdf:taIdentRef=...```. Each assigned type is also present as annotation feature in the form of ```rdf:typeX=...```

![68747470733a2f2f6269746275636b65742e6f72672f7265706f2f64416e4b454b2f696d616765732f333433333137373733322d656e74697479636c61737369666965722d73612d676174652d706c7567696e2d73732d312e706e67.png](https://bitbucket.org/repo/jnRMq7/images/504392779-68747470733a2f2f6269746275636b65742e6f72672f7265706f2f64416e4b454b2f696d616765732f333433333137373733322d656e74697479636c61737369666965722d73612d676174652d706c7567696e2d73732d312e706e67.png)

***Enjoy discovering entities!***



If you need any help/support with the plugin free to contact us. Bugs please report as issues to this repository.

How well it performs?
------
We have done extensive evaluation of the tool. See the results below.

![Entity spotting evaluation results](https://docs.google.com/spreadsheets/d/1Zv9s91FOe84BpOWQMSWH57S7dk3C-l-UO_Wtqq63hEw/pubchart?oid=1365533162&format=image)

![Entity spotting and linking evaluation results](https://docs.google.com/spreadsheets/d/1Zv9s91FOe84BpOWQMSWH57S7dk3C-l-UO_Wtqq63hEw/pubchart?oid=1243738458&format=image)

License
------

Licensed under the [GNU General Public License Version 3 (GNU GPLv3)](http://www.gnu.org/licenses/gpl.html).

Copyright (c) 2014-2015

* Milan Dojchinovski - <milan.dojchinovski@fit.cvut.cz>

* Tomas Kliegr - <tomas.kliegr@vse.cz>