FROM pmna/siga-base:latest

COPY infra/config/base/siga-skel/configs "${SIGA_DIR}/configs"
COPY infra/config/base/siga-skel/props "${SIGA_DIR}/props"

COPY target/siga.war "${SIGA_DIR}/deployments"
COPY target/sigaex.war "${SIGA_DIR}/deployments"
COPY target/sigawf.war "${SIGA_DIR}/deployments"
COPY target/sigagc.war "${SIGA_DIR}/deployments"