/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vse.fis.keg.entityclassifier.gate.plugin.sa;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class Configurator {
    public static void main(String[] args) {
        Properties prop = new Properties();
        OutputStream output = null;
        
        try {
            String currentDir = System.getProperty("user.dir"); 
            output = new FileOutputStream("config.properties");
            prop.setProperty("dbpediaOntologyDir", currentDir+"/data/dbpedia-3.9/dbpedia_3.9.owl");
            prop.setProperty("lhd10En", currentDir+"/data/lhd-2.3.9/en.LHDv1.draft.nt");
            prop.setProperty("lhd10De", currentDir+"/data/lhd-2.3.9/de.LHDv1.draft.nt");
            prop.setProperty("lhd10Nl", currentDir+"/data/lhd-2.3.9/nl.LHDv1.draft.nt");
            prop.setProperty("inferredEn", currentDir+"/data/lhd-2.3.9/en.inferredmappingstoDBpedia.nt");
            prop.setProperty("inferredDe", currentDir+"/data/lhd-2.3.9/de.inferredmappingstoDBpedia.nt");
            prop.setProperty("inferredNl", currentDir+"/data/lhd-2.3.9/nl.inferredmappingstoDBpedia.nt");
            prop.store(output, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Configurator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Configurator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
 
	}
    
    }
}
