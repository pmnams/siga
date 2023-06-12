#!make

IMAGENAME=siga-full
REPO=pmna
version=latest

.PHONY: help build push all

help:
	    @echo "Makefile arguments:"
	    @echo ""
	    @echo "version - Image Version"
	    @echo ""
	    @echo "Makefile commands:"
	    @echo "build"
	    @echo "pull"
	    @echo "all"

.DEFAULT_GOAL := build

IMAGEFULLNAME=${REPO}/${IMAGENAME}:${version}

start-dev-tls: export BASE_PATH = $(shell pwd)
start-dev-tls:
	@bash infra/siga-infra.sh deploy siga --dev --tls --add docker/swarm/siga-dev.yaml
	@bash infra/siga-infra.sh deploy infra --tls --add docker/swarm/traefik-dev.yaml

start-dev: export BASE_PATH = $(shell pwd)
start-dev:
	@bash infra/siga-infra.sh deploy siga --dev --add docker/swarm/siga-dev.yaml
	@bash infra/siga-infra.sh deploy infra --add docker/swarm/traefik-dev.yaml

stop-dev:
	@docker stack rm siga-default
	@docker stack rm infra

restart-dev:
	@docker service update siga_app --force

build-all:
	@mvn clean
	@mvn package -DshipTests=true
	@docker build --rm -t ${IMAGEFULLNAME} .

build:
	@docker build --rm -t ${IMAGEFULLNAME} .

push:
	@docker push ${IMAGEFULLNAME}
