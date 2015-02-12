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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Milan Dojchinovski
 <milan.dojchinovski@fit.cvut.cz>
 Twitter: @m1ci
 www: http://dojchinovski.mk
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
