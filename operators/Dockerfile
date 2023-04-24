FROM websphere-liberty

COPY --chown=1001:0 jakartaee-cafe/target/postgresql.jar /opt/ibm/wlp/usr/shared/resources/
COPY --chown=1001:0 server.xml /config/
COPY --chown=1001:0 jakartaee-cafe/target/jakartaee-cafe.war /config/dropins/