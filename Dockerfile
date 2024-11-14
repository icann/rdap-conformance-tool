FROM alpine:latest

RUN apk -q update ; apk -q add maven git xq

WORKDIR /app

COPY . /app

RUN mvn package -DskipTests --quiet

RUN mkdir datasets results

ADD https://www.iana.org/assignments/rdap-extensions/rdap-extensions.xml datasets/rdap-extensions.xml
ADD https://www.iana.org/assignments/registrar-ids/registrar-ids.xml datasets/registrar-ids.xml
ADD https://data.iana.org/rdap/dns.json datasets/dns.json
ADD https://www.iana.org/assignments/epp-repository-ids/epp-repository-ids.xml datasets/epp-repository-ids.xml
ADD https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml datasets/iana-ipv4-special-registry.xml
ADD https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml datasets/ipv4-address-space.xml
ADD https://www.iana.org/assignments/link-relations/link-relations.xml datasets/link-relations.xml
ADD https://www.iana.org/assignments/rdap-json-values/rdap-json-values.xml datasets/rdap-json-values.xml
ADD https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml datasets/ipv6-address-space.xml
ADD https://www.iana.org/assignments/ds-rr-types/ds-rr-types.xml datasets/ds-rr-types.xml
ADD https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xml datasets/dns-sec-alg-numbers.xml
ADD https://www.iana.org/assignments/media-types/media-types.xml datasets/media-types.xml
ADD https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xml datasets/iana-ipv6-special-registry.xml

ENTRYPOINT ["./docker-entrypoint.sh"]
