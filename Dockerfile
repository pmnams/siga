FROM brunocasas/siga-base:latest

COPY docker "${SIGA_DIR}"

COPY target/siga.war "${SIGA_DIR}/deployments"
COPY target/sigaex.war "${SIGA_DIR}/deployments"
COPY target/sigawf.war "${SIGA_DIR}/deployments"