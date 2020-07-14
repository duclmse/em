package com.viettel.ems.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpGet {
    private static final String ipAddress = "127.0.0.1";

    private static final String port = "8001";

    // OID of MIB RFC 1213; Scalar Object = .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0
    private static final String oidValue = ".1.3.6.1.2.1.1.1.0";  // ends with 0 for scalar object

    private static final int snmpVersion = SnmpConstants.version1;

    private static final String community = "public";

    public static void main(String[] args) throws Exception {
        System.out.println("SNMP GET Demo");

        // Create TransportMapping and Listen
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        transport.listen();

        // Create Target Address object
        CommunityTarget<Address> comtarget = new CommunityTarget<>();
        comtarget.setCommunity(new OctetString(community));
        comtarget.setVersion(snmpVersion);
        comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
        comtarget.setRetries(2);
        comtarget.setTimeout(1000);

        // Create the PDU object
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new org.snmp4j.smi.OID(oidValue)));
        pdu.setType(PDU.GET);
        pdu.setRequestID(new Integer32(1));

        // Create Snmp object for sending data to Agent
        try (Snmp snmp = new Snmp(transport)) {

            System.out.println("Sending Request to Agent...");
            ResponseEvent<Address> response = snmp.get(pdu, comtarget);

            // Process Agent Response
            if (response == null) {
                System.out.println("Error: Agent Timeout... ");
                return;
            }

            System.out.println("Got Response from Agent");
            PDU responsePDU = response.getResponse();

            if (responsePDU == null) {
                System.out.println("Error: Response PDU is null");
                return;
            }

            int errorStatus = responsePDU.getErrorStatus();
            int errorIndex = responsePDU.getErrorIndex();
            String errorStatusText = responsePDU.getErrorStatusText();

            if (errorStatus == PDU.noError) {
                System.out.println("Snmp Get Response = " + responsePDU.getVariableBindings());
            } else {
                System.out.println("Error: Request Failed");
                System.out.println("Error Status = " + errorStatus);
                System.out.println("Error Index = " + errorIndex);
                System.out.println("Error Status Text = " + errorStatusText);
            }
        }
    }
}