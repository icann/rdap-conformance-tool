package org.icann.rdapconformance.validator;

public enum NetworkProtocol {
    IPv4,
    IPv6;

    @Override
    public String toString() {
        return name();
    }
}