# Multi-stage build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy all source code and build
COPY pom.xml .
COPY jitpack/ jitpack/
COPY validator/ validator/
COPY tool/ tool/

# Build it!
RUN mvn package -DskipTests --quiet

# Latest, please
FROM alpine:latest

# Install runtime dependencies
RUN apk add --update --no-cache openjdk21-jre xq curl && \
    adduser -D -s /bin/sh appuser

WORKDIR /app

# Copy over just what we need
COPY --from=builder /build/tool/target/ tool/target/
COPY --from=builder /build/tool/bin/ tool/bin/
COPY --from=builder /build/pom.xml .

# Copy entrypoint script
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

# Create our needed directories
RUN mkdir -p datasets results && chown -R appuser:appuser /app

# Download all IANA datasets in a single layer
RUN curl -s -f https://www.iana.org/assignments/rdap-extensions/rdap-extensions.xml -o datasets/rdap-extensions.xml && \
    curl -s -f https://www.iana.org/assignments/registrar-ids/registrar-ids.xml -o datasets/registrar-ids.xml && \
    curl -s -f https://data.iana.org/rdap/dns.json -o datasets/dns.json && \
    curl -s -f https://www.iana.org/assignments/epp-repository-ids/epp-repository-ids.xml -o datasets/epp-repository-ids.xml && \
    curl -s -f https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml -o datasets/iana-ipv4-special-registry.xml && \
    curl -s -f https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml -o datasets/ipv4-address-space.xml && \
    curl -s -f https://www.iana.org/assignments/link-relations/link-relations.xml -o datasets/link-relations.xml && \
    curl -s -f https://www.iana.org/assignments/rdap-json-values/rdap-json-values.xml -o datasets/rdap-json-values.xml && \
    curl -s -f https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml -o datasets/ipv6-address-space.xml && \
    curl -s -f https://www.iana.org/assignments/ds-rr-types/ds-rr-types.xml -o datasets/ds-rr-types.xml && \
    curl -s -f https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xml -o datasets/dns-sec-alg-numbers.xml && \
    curl -s -f https://www.iana.org/assignments/media-types/media-types.xml -o datasets/media-types.xml && \
    curl -s -f https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xml -o datasets/iana-ipv6-special-registry.xml && \
    chown -R appuser:appuser datasets

# Switch to non-root user
USER appuser

ENTRYPOINT ["./docker-entrypoint.sh"]
