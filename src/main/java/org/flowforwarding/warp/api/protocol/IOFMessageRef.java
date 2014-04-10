/**
 * © 2013 FlowForwarding.Org
 * All Rights Reserved.  Use is subject to license terms.
 */
package org.flowforwarding.warp.api.protocol;

/**
 * @author Infoblox Inc.
 *
 */
public interface IOFMessageRef {
   public void add (String name, String value);
   public void add (String name, IOFStructureRef struct);
}