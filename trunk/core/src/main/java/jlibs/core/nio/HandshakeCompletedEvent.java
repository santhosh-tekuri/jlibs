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

package jlibs.core.nio;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;
import java.io.Serializable;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.EventObject;

/**
 * The event object encapsulating the information about a completed SSL
 * handshake on a SSL connection.
 *
 * @author Santhosh Kumar T
 */
public class HandshakeCompletedEvent implements Serializable{
    private static final long serialVersionUID = 7914963744257769778L;
    private transient ClientChannel channel;
    private transient SSLSession session;

    public HandshakeCompletedEvent(ClientChannel channel, SSLSession session){
        this.channel = channel;
        this.session = session;
    }

    /** Returns the SSL session associated with this event. */
    public SSLSession getSession(){
        return session;
    }

    /** Returns the name of the cipher suite negotiated during this handshake. */
    public String getCipherSuite(){
        return session.getCipherSuite();
    }

    /**
     * Returns the list of local certificates used during the handshake. These
     * certificates were sent to the peer.
     */
    public Certificate[] getLocalCertificates(){
        return session.getLocalCertificates();
    }

    /**
     * Return the list of certificates identifying the peer during the
     * handshake.
     *
     * @return the list of certificates identifying the peer with the peer's
     *         identity certificate followed by CAs.
     * @throws SSLPeerUnverifiedException if the identity of the peer has not been verified.
     */
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException{
        return session.getPeerCertificates();
    }

    /**
     * Returns the list of certificates identifying the peer. The peer's
     * identity certificate is followed by the validated certificate authority
     * certificates.
     * <p/>
     * <b>Replaced by:</b> {@link #getPeerCertificates()}
     *
     * @return the list of certificates identifying the peer
     * @throws SSLPeerUnverifiedException if the identity of the peer has not been verified.
     */
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException{
        return session.getPeerCertificateChain();
    }

    /**
     * Returns the {@code Principal} identifying the peer.
     *
     * @return the {@code Principal} identifying the peer.
     * @throws SSLPeerUnverifiedException if the identity of the peer has not been verified.
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException{
        return session.getPeerPrincipal();
    }

    /**  Returns the {@code Principal} used to identify during the handshake. */
    public Principal getLocalPrincipal(){
        return session.getLocalPrincipal();
    }

    public ClientChannel getChannel(){
        return channel;
    }
}