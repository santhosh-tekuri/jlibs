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

import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.security.KeyStore;

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

    public static KeyStore newKeyStore() throws SSLException{
        String location = getKeyStoreLocation();
        if(location!=null)
            return newKeyStore(getKeyStoreType(), location, getKeyStorePassword());
        else
            return null;
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

    public static KeyStore newTrustStore() throws SSLException{
        String location = getTrustStoreLocation();
        if(location!=null)
            return newKeyStore(getTrustStoreType(), location, getTrustStorePassword());
        else
            return null;
    }
}
