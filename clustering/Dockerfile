FROM websphere-liberty

COPY --chown=1001:0 server/postgresql-42.6.0.jar /opt/ibm/wlp/usr/shared/resources/
COPY --chown=1001:0 server/server.xml /config/
COPY --chown=1001:0 jakartaee-cafe.war /config/dropins/
