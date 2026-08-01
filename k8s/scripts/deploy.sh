#!/bin/bash

# Create namespace first
kubectl apply -f k8s/namespace.yaml

# Databases
kubectl apply -f k8s/postgres/auth-postgres/secret.yaml
kubectl apply -f k8s/postgres/auth-postgres/configmap.yaml
kubectl apply -f k8s/postgres/auth-postgres/statefulset.yaml
kubectl apply -f k8s/postgres/auth-postgres/service.yaml

kubectl apply -f k8s/postgres/library-postgres/secret.yaml
kubectl apply -f k8s/postgres/library-postgres/configmap.yaml
kubectl apply -f k8s/postgres/library-postgres/statefulset.yaml
kubectl apply -f k8s/postgres/library-postgres/service.yaml

# Microservices
kubectl apply -f k8s/auth-service/configmap.yaml
kubectl apply -f k8s/auth-service/deployment.yaml
kubectl apply -f k8s/auth-service/service.yaml

kubectl apply -f k8s/library-service/configmap.yaml
kubectl apply -f k8s/library-service/deployment.yaml
kubectl apply -f k8s/library-service/service.yaml

kubectl apply -f k8s/api-gateway/configmap.yaml
kubectl apply -f k8s/api-gateway/deployment.yaml
kubectl apply -f k8s/api-gateway/service.yaml

# Frontend
kubectl apply -f k8s/frontend/deployment.yaml
kubectl apply -f k8s/frontend/service.yaml

# Ingress
kubectl apply -f k8s/ingress/ingress.yaml