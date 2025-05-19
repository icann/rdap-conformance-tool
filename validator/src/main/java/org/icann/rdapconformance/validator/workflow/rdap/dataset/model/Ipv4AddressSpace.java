package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import inet.ipaddr.IPAddressString;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class Ipv4AddressSpace extends XmlObject implements DatasetValidatorModel {

    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private final List<Ipv4AddressSpaceRecord> records = new ArrayList<>();

//    public boolean isInvalid(String ipAddress) {
//        return records.stream()
//                      .filter(r -> r.getStatus().equals("ALLOCATED") || r.getStatus().equals("LEGACY"))
//                      .noneMatch(r -> {
//                          IPAddressString net = new IPAddressString(r.getPrefix());
//                          return net.contains(new IPAddressString(ipAddress));
//                      });
//    }

    public boolean isInvalid(String ipAddress) {
        System.out.println("DEBUG-DNS: Checking IP address: " + ipAddress);
        System.out.println("DEBUG-DNS: Number of records to check: " + records.size());

        return records.stream()
                      .filter(r -> {
                          boolean isValidStatus = r.getStatus().equals("ALLOCATED") || r.getStatus().equals("LEGACY");
                          System.out.println("DEBUG-DNS: Record prefix: " + r.getPrefix() + " status: " + r.getStatus() + " isValidStatus: " + isValidStatus);
                          return isValidStatus;
                      })
                      .noneMatch(r -> {
                          IPAddressString net = new IPAddressString(r.getPrefix());
                          boolean contains = net.contains(new IPAddressString(ipAddress));
                          System.out.println("DEBUG-DNS: Checking if " + r.getPrefix() + " contains " + ipAddress + ": " + contains);
                          return contains;
                      });
    }

    @Override
    public void parse(InputStream inputStream) throws Throwable {

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Ipv4AddressSpaceRecord {

        @XmlElement(name = "prefix", namespace = "http://www.iana.org/assignments")
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public String getStatus() {
            return status;
        }

        @XmlElement(name = "status", namespace = "http://www.iana.org/assignments")
        private String status;

        public Ipv4AddressSpaceRecord(String prefix, String status) {
            this.prefix = prefix;
            this.status = status;
        }
        void afterUnmarshal(Unmarshaller u, Object parent) {
            this.prefix = String.format("%d.0.0.0/8", Integer.valueOf(this.prefix.split("/")[0]));
        }

        public Ipv4AddressSpaceRecord() {
            super();
        }
    }
}
