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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Milan Dojchinovski
 <milan.dojchinovski@fit.cvut.cz>
 Twitter: @m1ci
 www: http://dojchinovski.mk
 */
public class DBpediaLinker {
    
    private static DBpediaLinker instance = null;
    
    public static DBpediaLinker getInstance(){
        if(instance == null) {
            instance = new DBpediaLinker();
        }
        return instance;
    }
    
    public String getDBpediaLink(String query, String endpoint) {
        
        String dbpediaLink = null;
        try {
            URL url = null;
            query = URLEncoder.encode(query, "UTF-8");
            String path = endpoint+"?action=query&format=xml&list=search&srlimit=1&srsearch="+query;
            url = new URL(path);
            StringBuffer buffer = new StringBuffer();
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            InputStream is = connection.getInputStream();
            Reader isr = new InputStreamReader(is,"UTF-8");
            Reader in = new BufferedReader(isr);
            int ch;
            
            while ((ch = in.read()) > -1) {
                buffer.append((char) ch);
            }
            in.close();
            isr.close();
            
            String responseText = buffer.toString();
            Pattern articleTitle = Pattern.compile("title=\"(.*?)\"", Pattern.DOTALL);
            Matcher titleMatcher = articleTitle.matcher(responseText);
            while (titleMatcher.find()) {
                String title = titleMatcher.group(1);
                title = title.replaceAll(" ", "_");
                String dbpediaURI = "http://dbpedia.org/resource/"+title;
//                if(validateDBpediaURI(dbpediaURI)) {
                    dbpediaLink = dbpediaURI;
//                    break;
//                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(DBpediaLinker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DBpediaLinker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbpediaLink;
    }
    
    private boolean validateDBpediaURI(String sURL) {
        boolean valid = false;
        try {
            URL url = null;
            url = new URL(sURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                valid = true;
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(DBpediaLinker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DBpediaLinker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return valid;    
    }
}
