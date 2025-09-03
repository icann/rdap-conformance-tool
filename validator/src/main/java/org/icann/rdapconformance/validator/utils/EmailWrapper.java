package org.icann.rdapconformance.validator.utils;

public class EmailWrapper {
    private String local;
    private String domain;

    public EmailWrapper(String email) {
        int indexOfAt = email.indexOf("@");
        if (indexOfAt > 0 && email.length() - 1 > indexOfAt) {
            setLocal(email.substring(0,indexOfAt));
            setDomain(email.substring(indexOfAt + 1));
        }
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

} 