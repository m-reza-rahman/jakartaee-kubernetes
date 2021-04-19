FROM websphere-liberty

COPY --chown=1001:0 postgresql-42.2.4.jar /opt/ibm/wlp/usr/shared/resources/
COPY --chown=1001:0 server.xml /config/
COPY --chown=1001:0 jakartaee-cafe.war /config/dropins/