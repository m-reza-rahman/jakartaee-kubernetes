# FROM open-liberty:kernel-slim-java17-openj9
FROM open-liberty:full-java17-openj9

COPY --chown=1001:0 server.xml /config/

# RUN features.sh

COPY --chown=1001:0 postgresql.jar /opt/ol/wlp/usr/shared/resources
COPY --chown=1001:0 jakartaee-cafe/target/jakartaee-cafe.war /config/dropins/

# RUN configure.sh
