Wir haben zwei Möglichkeiten, um unsere Anwendung in Kubernetes zu deployen:

1. Mit Minikube
2. Mit K3d

Welche Ressourcen brauchen wir?

1. Postgres -> Auth-Service
2. Postgres -> Library-Service
3. Auth-Service 
4. Library-Service 
5. API-Gateway 
6. Frontend

Wir wollen eine klare Trennung der Deployements-Dateien, Wir wollen dann mindestens 6 Ordners. 
Brauchen wir ein Ingress-Controller? Ich denke ja, um die Anfragen an die richtigen Services weiterzuleiten.

Ist das dann ausreichend, um die Anwendung in Kubernetes zu deployen?

Welche Ordnerstruktur wollen wir dann haben? Wir trennen auch die Services voneinander.     
Es muss quasi so aussehen:

/k8s
    /postgres
        /auth-postgres
            /statefulset.yaml -> StatefulSet? Mit volumeClaimTemplates
            /service.yaml  -> Headless Service für StatefulSet
            /configmap.yaml
            /secret.yaml
        /library-postgres
            /statefulset.yaml -> StatefulSet? Mit volumeClaimTemplates
            /service.yaml  -> Headless Service für StatefulSet
            /configmap.yaml
            /secret.yaml
    /auth-service
        /deployment.yaml -> Liveness und Readiness Probes + Resource Limits
        /service.yaml
        /configmap.yaml
        /secret.yaml
    /library-service
        /deployment.yaml -> Liveness und Readiness Probes + Resource Limits
        /service.yaml
        /configmap.yaml
        /secret.yaml
    /api-gateway
        /deployment.yaml
        /service.yaml
        /configmap.yaml
        /secret.yaml
    /frontend
        /deployment.yaml
        /service.yaml
        /configmap.yaml
        /secret.yaml
    /ingress
        /ingress.yaml
    /scripts
        /deploy.sh
        /delete.sh

    /namespace.yaml
    /README.md


    ```bash 

    # 1. Image mit Docker Compose bauen
docker compose up --build -d

# 2. Image in Minikube laden
minikube image load library_app-auth-service:latest

# 3. Deployment aktualisieren
kubectl rollout restart deployment auth-service -n library-app

# 4. internes Healch Check durchführen
wget -qO- http://localhost:8080/actuator/health

kubectl run -it --rm debug --image=curlimages/curl --restart=Never -n library-app -- \
  curl http://auth-service:8080/actuator/health

# 5. List the pods 
kubectl get ingress -n library-app && minikube addons list | grep ingress

```

Nach jedem Update muss ich die Images in Minikube/ Docker Hub laden/pushen damit die Deployement aktualisiert werden können