/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.core.net;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * @author Santhosh Kumar Tekuti
 */
public class SSLUtil{
    public static KeyStore newKeyStore(String type, String file, char password[]) throws SSLException{
        try{
            KeyStore ks = KeyStore.getInstance(type==null ? KeyStore.getDefaultType() : type);
            ks.load(file!=null ? new FileInputStream(file) : null , password!=null?password:null);
            return ks;
        }catch(Exception ex){
            throw new SSLException(ex);
        }
    }

    public static String getKeyStoreType(){
        return System.getProperty("javax.net.ssl.keyStoreType", "JKS");
    }

    public static String getKeyStoreLocation(){
        return System.getProperty("javax.net.ssl.keyStore");
    }

    public static char[] getKeyStorePassword(){
        String password = System.getProperty("javax.net.ssl.keyStorePassword");
        return password!=null ? password.toCharArray() : null;
    }

    private static KeyStore defaultKeyStore[];
    public static KeyStore defaultKeyStore() throws SSLException{
        if(defaultKeyStore==null){
            String location = getKeyStoreLocation();
            if(location!=null)
                defaultKeyStore = new KeyStore[]{ newKeyStore(getKeyStoreType(), location, getKeyStorePassword()) };
            else
                defaultKeyStore = new KeyStore[]{ null };
        }
        return defaultKeyStore[0];
    }

    public static String getTrustStoreType(){
        return System.getProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    public static String getTrustStoreLocation(){
        return System.getProperty("javax.net.ssl.trustStore");
    }

    public static char[] getTrustStorePassword(){
        String password = System.getProperty("javax.net.ssl.trustStorePassword");
        return password!=null ? password.toCharArray() : null;
    }

    private static KeyStore defaultTrustStore[];
    public static KeyStore defaultTrustStore() throws SSLException{
        if(defaultTrustStore==null){
            String location = getTrustStoreLocation();
            if(location!=null)
                defaultTrustStore = new KeyStore[]{ newKeyStore(getTrustStoreType(), location, getTrustStorePassword()) };
            else
                defaultTrustStore = new KeyStore[]{ null };
        }
        return defaultTrustStore[0];
    }

    public static final TrustManager DUMMY_TRUST_MANAGERS[] = new TrustManager[]{
       new X509TrustManager(){
           public X509Certificate[] getAcceptedIssuers(){
               return new X509Certificate[0];
           }
           public void checkClientTrusted(X509Certificate[] certs, String authType){}
           public void checkServerTrusted(X509Certificate[] certs, String authType){}
       }
    };

    public static SSLContext newContext(KeyStore keyStore, char[] keyStorePassword, KeyStore trustStore) throws SSLException, GeneralSecurityException{
        SSLContext sslContext = SSLContext.getInstance("TLS");

        TrustManager tm[];
        if(trustStore==null)
            tm = SSLUtil.DUMMY_TRUST_MANAGERS;
        else{
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(SSLUtil.defaultTrustStore());
            tm = tmf.getTrustManagers();
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword);

        sslContext.init(kmf.getKeyManagers(), tm , null);
        return sslContext;
    }

    private static SSLContext defaultContext;
    public static SSLContext defaultContext() throws SSLException, GeneralSecurityException{
        if(defaultContext==null)
            defaultContext = newContext(SSLUtil.defaultKeyStore(), SSLUtil.getKeyStorePassword(), SSLUtil.defaultTrustStore());
        return defaultContext;
    }
}
