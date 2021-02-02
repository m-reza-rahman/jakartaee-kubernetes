FROM grafana/grafana

ENV GF_AUTH_ANONYMOUS_ENABLED "true"
ENV GF_AUTH_ANONYMOUS_ORG_ROLE "Admin"

COPY prometheus-datasource.yml /etc/grafana/provisioning/datasources/
COPY liberty-dashboard.yml /etc/grafana/provisioning/dashboards/
COPY liberty-dashboard.json /var/lib/grafana/dashboards/