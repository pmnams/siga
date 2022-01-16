#!make

IMAGENAME=siga-full
REPO=brunocasas
verssion=latest

.PHONY: help build push all

help:
	    @echo "Makefile arguments:"
	    @echo ""
	    @echo "verssion - Image Version"
	    @echo ""
	    @echo "Makefile commands:"
	    @echo "build"
	    @echo "pull"
	    @echo "all"

.DEFAULT_GOAL := build

IMAGEFULLNAME=${REPO}/${IMAGENAME}:${verssion}

start-dev: export BASE_PATH = $(shell pwd)
start-dev:
	@bash infra/bin/siga-infra.sh deploy siga --desenv -a docker/swarm/siga-compose-dev.yaml
	@bash infra/bin/siga-infra.sh deploy traefik

stop-dev:
	@docker stack rm siga
	@docker stack rm traefik

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

