IMAGENAME=siga-full
REPO=brunocasas

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
	@docker stack deploy -c infra/siga-compose.yml -c infra/siga-compose-hom.yaml -c docker/swarm/siga-compose-dev.yaml siga
	@docker stack deploy -c infra/traefik-compose.yaml traefik

stop-dev:
	@docker stack rm siga
	@docker stack rm traefik

restart-dev:
	@docker service update siga_app --force

build:
	@docker build --rm -t ${IMAGEFULLNAME} .

push:
	@docker push ${IMAGEFULLNAME}

