/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vse.fis.keg.entityclassifier.gate.plugin.sa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Milan
 */
public class DBpediaLinker {
    
    private static DBpediaLinker instance = null;
    
    public static DBpediaLinker getInstance(){
        if(instance == null) {
            instance = new DBpediaLinker();
        }
        return instance;
    }
    
    public String getDBpediaLink(String query) {
        String dbpediaLink = null;
        try {
            URL url = null;
            query = URLEncoder.encode(query, "UTF-8");
            String path = "http://en.wikipedia.org/w/api.php?action=query&format=xml&list=search&srlimit=1&srsearch="+query;
//            System.out.println(path);
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
//            System.out.println(responseText);
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
//        System.out.println(sURL + " " + valid);
        return valid;    
    }
}
