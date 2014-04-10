/**
 * © 2013 FlowForwarding.Org
 * All Rights Reserved.  Use is subject to license terms.
 */
package org.flowforwarding.warp.api.protocol;

/**
 * @author Infoblox Inc.
 *
 */
public interface IOFProvider {
   public IOFMessageRef buildMessage(String msg);
   public byte [] encode(IOFMessageRef msg);
}