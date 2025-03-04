package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class BootstrapDomainNameSpace implements RDAPDatasetModel {

    private Map<String, Set<String>> tlds = new HashMap<>();

    private List<List<List<String>>> services;

    @JsonCreator
    public BootstrapDomainNameSpace(@JsonProperty("services") List<List<List<String>>> services) {
        this.services = services;
        parseServices();
    }

    public BootstrapDomainNameSpace() {

    }

    private void parseServices() {
        for (List<List<String>> service : services) {
            List<String> tldData = service.get(0);
            List<String> urls = service.get(1);
            for (String tld : tldData) {
                Set<String> urlSet = tlds.getOrDefault(tld, new HashSet<>());
                urlSet.addAll(urls);
                tlds.put(tld, urlSet);
            }
        }
    }

    public Set<String> getUrlsForTld(String tld) {
        return tlds.get(tld);
    }

    public boolean tldExists(String tld) {
        return tlds.keySet().stream().anyMatch(key -> key.equalsIgnoreCase(tld));
    }

    public Set<String> getTlds() {
        return tlds.keySet();
    }
}
