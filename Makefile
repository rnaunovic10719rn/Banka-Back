REPO ?= ghcr.io/raf-si-2021
TAG ?= latest
VERSION ?= 0.0.1-SNAPSHOT

.PHONY: test
build-docker:
	docker build --build-arg=VERSION=$(VERSION) -t $(REPO)/banka-influx-scrapper:$(TAG) ./InfluxScrapper
	docker build --build-arg=VERSION=$(VERSION) -t $(REPO)/banka-user-service:$(TAG) ./user-service
	docker build --build-arg=VERSION=$(VERSION) -t $(REPO)/banka-mail-service:$(TAG) ./mail-service
	docker build --build-arg=VERSION=$(VERSION) -t $(REPO)/banka-berza:$(TAG) ./berza
	docker build --build-arg=VERSION=$(VERSION) -t $(REPO)/banka-racun-service:$(TAG) ./racun-service

.PHONY: push-docker
push-docker: build-docker
	docker push $(REPO)/banka-influx-scrapper:$(TAG)
	docker push $(REPO)/banka-user-service:$(TAG)
	docker push $(REPO)/banka-mail-service:$(TAG)
	docker push $(REPO)/banka-berza:$(TAG)
	docker push $(REPO)/banka-racun-service:$(TAG)

