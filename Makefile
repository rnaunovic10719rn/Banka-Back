REPO ?= ghcr.io/raf-si-2021
TAG ?= latest

.PHONY: test
build-docker:
	docker build -t $(REPO)/banka-eureka:$(TAG) ./eureka
	docker build -t $(REPO)/banka-zuul:$(TAG) ./zuul
	docker build -t $(REPO)/banka-influx-scrapper:$(TAG) ./InfluxScrapper
	docker build -t $(REPO)/banka-user-service:$(TAG) ./user-service
	docker build -t $(REPO)/banka-mail-service:$(TAG) ./mail-service
	docker build -t $(REPO)/banka-berza:$(TAG) ./berza

.PHONY: push-docker
push-docker: build-docker
	docker push $(REPO)/banka-eureka:$(TAG)
	docker push $(REPO)/banka-zuul:$(TAG)
	docker push $(REPO)/banka-influx-scrapper:$(TAG)
	docker push $(REPO)/banka-user-service:$(TAG)
	docker push $(REPO)/banka-mail-service:$(TAG)
	docker push $(REPO)/banka-berza:$(TAG)

