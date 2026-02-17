#!/bin/bash

kubectl apply -f k8s/postgres/auth-postgres/secret.yaml
kubectl apply -f k8s/postgres/auth-postgres/configmap.yaml
kubectl apply -f k8s/postgres/auth-postgres/deployment.yaml
kubectl apply -f k8s/postgres/auth-postgres/service.yaml

kubectl apply -f k8s/postgres/library-postgres/secret.yaml
kubectl apply -f k8s/postgres/library-postgres/configmap.yaml
kubectl apply -f k8s/postgres/library-postgres/deployment.yaml
kubectl apply -f k8s/postgres/library-postgres/service.yaml

kubectl apply -f k8s/auth-service/configmap.yaml
kubectl apply -f k8s/auth-service/deployment.yaml
kubectl apply -f k8s/auth-service/service.yaml

kubectl apply -f k8s/library-service/configmap.yaml
kubectl apply -f k8s/library-service/deployment.yaml
kubectl apply -f k8s/library-service/service.yaml

kubectl apply -f k8s/api-gateway/configmap.yaml
kubectl apply -f k8s/api-gateway/deployment.yaml
kubectl apply -f k8s/api-gateway/service.yaml

kubectl apply -f k8s/frontend/configmap.yaml
kubectl apply -f k8s/frontend/deployment.yaml
kubectl apply -f k8s/frontend/service.yaml

kubectl apply -f k8s/namespace.yaml