/*
 Copyright 2014 AT&T
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.att.aro.core.packetreader.pojo;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;

/**
 * A bean class that provides access to IP Packet data.
 */
public class IPPacket extends Packet implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@InjectLogger
	private static ILogger logger;
	
	private byte ipVersion;
	private byte priority;
	private int packetLength;
	private boolean rsvFrag;
	private boolean dontFrag;
	private boolean moreFrag;
	private short fragmentOffset;
	private short timeToLive;
	private short protocol;
	private InetAddress sourceIPAddress;
	private InetAddress destinationIPAddress;
	private int dataOffset;
	private int payloadLen;

	/**
	 * Creates a new instance of the IPPacket class.
	 */
	public IPPacket(long seconds, long microSeconds, int len, int datalinkHdrLen,
			byte[] data) {
		super(seconds, microSeconds, len, datalinkHdrLen, data);

		// Parse data
		ByteBuffer bytes = ByteBuffer.wrap(data);
		int headerOffset = super.getDataOffset();

		// check for IPv4 or IPv6
		ipVersion = (byte) ((bytes.get(headerOffset) & 0xf0) >> 4);
		int hlen = -1;
		if (ipVersion == 6) {
			hlen = 40;
			payloadLen = bytes.getShort(headerOffset + 4);
			packetLength = len;
		} else {
			hlen = ((bytes.get(headerOffset) & 0x0f) << 2);
/*		   
 * 			the packet length read could be negative, if the packet length is a large number (more
 *		   than 15 bits in storage size (but less than 16 bits).
 *		   So chop off the first two bytes (and hence the signed bits) before 
 *		   setting the field.
 */
			packetLength = bytes.getShort(headerOffset + 2)& 0x0000ffff;
			
			if (packetLength == 0) {
				// Assume TCP segmentation offload (TSO) so calculate our own packet length 
				packetLength = len - headerOffset;
			}
			
			payloadLen = packetLength - hlen;
/*	for test		
			if(payloadLen<0){				
 				int temp = (int)packetLength & 0x0000ffff;
				Log.d("test", "test"+ temp);
			}
*/			
			short ivalue = bytes.getShort(headerOffset + 6);
			rsvFrag = (ivalue & 0x8000) != 0;
			dontFrag = (ivalue & 0x4000) != 0;
			moreFrag = (ivalue & 0x2000) != 0;
			fragmentOffset = (short) (ivalue & 0x1fff);
		}

		dataOffset = headerOffset + hlen;

		short ivalue = bytes.getShort(headerOffset + 8);
		if (ipVersion == 4) {
			timeToLive = (short) ((ivalue & 0xff00) >> 8);
		}
		
		protocol = (short) (ivalue & 0xff);

		byte[] buffer = null;
		int addrLgth = -1;
		int addrOffset = -1;
		if (ipVersion == 6) {
			addrLgth = 16;
			addrOffset = 8;
		} else {
			addrLgth = 4;
			addrOffset = 12;
		}

		buffer = new byte[addrLgth];
		bytes.position(headerOffset + addrOffset);
		bytes.get(buffer, 0, addrLgth);
		try {
			sourceIPAddress = InetAddress.getByAddress(buffer);
		} catch (UnknownHostException e) {
			logger.warn("Unable to determine source IP - " + e.getMessage());
		}
		try {
			bytes.get(buffer, 0, addrLgth);
			destinationIPAddress = InetAddress.getByAddress(buffer);
		} catch (UnknownHostException e) {
			logger.warn("Unable to determine destination IP - " + e.getMessage());
		}
	}

	/**
	 * @see com.att.aro.pcap.Packet#getDataOffset()
	 * @return The offset within the data array of the packet data, excluding the header information.
	 */
	@Override
	public int getDataOffset() {
		return dataOffset;
	}

	/**
	 * @see com.att.aro.pcap.Packet#getPayloadLen()
	 */
	@Override
	public int getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Gets the IP version for IPv4 and IPv6.
	 * 
	 * @return A byte value that is the IP version. A value of 4 indicates IPv4.
	 */
	public byte getIPVersion() {
		return ipVersion;
	}

	/**
	 * Gets the priority value for the IPPacket. In Pv4 this is TOS, and in Pv6
	 * it is Traffic Class.
	 * 
	 * @return A byte value that is the priority value.
	 */
	public byte getPriority() {
		return priority;
	}

	/**
	 * Gets the length of the packet including the header.
	 * 
	 * @return An int value that is the length of the packet in bytes.
	 */
	public int getPacketLength() {
		return packetLength;
	}

	/**
	 * Gets the value of the Fragmentation Reservation flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the Fragmentation Reservation flag.
	 */
	public boolean isRsvFrag() {
		return rsvFrag;
	}

	/**
	 * Gets the value of the Don't Fragment flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the Don't Fragment flag.
	 */
	public boolean isDontFrag() {
		return dontFrag;
	}

	/**
	 * Gets the value of the More Fragment flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the More Fragment flag.
	 */
	public boolean isMoreFrag() {
		return moreFrag;
	}

	/**
	 * gets the fragment offset.(IPv4 only)
	 * 
	 * @return A short value that is the fragment offset.
	 */
	public short getFragmentOffset() {
		return fragmentOffset;
	}

	/**
	 * Gets theTime to Live (TTL) value. (IPv4 only)
	 * 
	 * @return A short that is the TTL value.
	 */
	public short getTimeToLive() {
		return timeToLive;
	}

	/**
	 * Gets the protocol for the IPPacket.
	 * 
	 * @return A short value that is the protocol.
	 */
	public short getProtocol() {
		return protocol;
	}

	/**
	 * Gets the source IP Address.
	 * 
	 * @return The source IP Address in the InetAddress format.
	 */
	public InetAddress getSourceIPAddress() {
		return sourceIPAddress;
	}

	/**
	 * Returns Gets the destination IP address.
	 * 
	 * @return The destination IP address in the InetAddress format.
	 */
	public InetAddress getDestinationIPAddress() {
		return destinationIPAddress;
	}

}
