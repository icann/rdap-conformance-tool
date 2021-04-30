package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class BootstrapDomainNameSpace implements RDAPDatasetModel {

  private final Map<String, Set<String>> tlds = new HashMap<>();


  @Override
  public void parse(InputStream inputStream) throws Throwable {
    JSONObject jsonObject;
    try (Reader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr)) {
      jsonObject = new JSONObject(br.lines().collect(Collectors.joining(System.lineSeparator())));
    }
    JSONArray array = jsonObject.getJSONArray("services");
    for (Object dnsData : array) {
      JSONArray inArray = (JSONArray) dnsData;
      for (Object obj : inArray.getJSONArray(0)) {
        String tld = String.valueOf(obj);
        Set<String> urls = tlds.getOrDefault(tld, new HashSet<>());
        urls.addAll(inArray.getJSONArray(1).toList().stream()
            .map(String::valueOf)
            .collect(Collectors.toList()));
        tlds.put(tld, urls);
      }
    }
  }

  public Set<String> getUrlsForTld(String tld) {
    return tlds.get(tld);
  }

  public boolean tldExists(String tld) {
    return tlds.containsKey(tld);
  }

  public Set<String> getTlds() {
    return tlds.keySet();
  }
}
