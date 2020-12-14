FROM websphere-liberty

RUN installUtility install mpHealth-2.2 --acceptLicense
RUN installUtility install mpMetrics-2.3 --acceptLicense

COPY postgresql-42.2.4.jar /opt/ibm/wlp/usr/shared/resources/
COPY server.xml /config/
ADD jakartaee-cafe.war /config/dropins/jakartaee-cafe.war