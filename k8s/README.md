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
# Im Hauptverzeichnis des Projekts
cd /home/sonfack/projet_2025/Library_App

# Images mit Docker Compose bauen
docker compose build

# Images in Minikube laden
minikube image load library_app-auth-service:latest
minikube image load library_app-library-service:latest
minikube image load library_app-api-gateway:latest
minikube image load library_app-frontend:latest
```

### 4. Namespace erstellen

```bash
kubectl apply -f k8s/namespace.yaml
```

### 5. Alle Ressourcen deployen

```bash
# Deployment-Skript verwenden (empfohlen)
cd k8s/scripts
./deploy.sh

# Oder manuell:
kubectl apply -f k8s/postgres/auth-postgres/
kubectl apply -f k8s/postgres/library-postgres/
kubectl apply -f k8s/auth-service/
kubectl apply -f k8s/library-service/
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/frontend/
kubectl apply -f k8s/ingress/
```

### 6. Port-Forwarding einrichten

```bash
kubectl port-forward -n ingress-nginx service/ingress-nginx-controller 8090:80
```

### 7. Anwendung öffnen

Öffne im Browser: **http://localhost:8090**

## 🔍 Anwendung prüfen

### Alle Pods anzeigen

```bash
kubectl get pods -n library-app
```

**Erwartete Ausgabe:** Alle Pods sollten `Running` und `READY 1/1` sein.

### Services prüfen

```bash
kubectl get services -n library-app
```

### Ingress prüfen

```bash
kubectl get ingress -n library-app
```

**Erwartete Ausgabe:**
```
NAME              CLASS   HOSTS                    ADDRESS        PORTS   AGE
library-ingress   nginx   library.local,localhost  192.168.49.2   80      Xd
```

### Endpoints prüfen

```bash
kubectl get endpoints -n library-app
```

Prüfe, dass alle Services Endpoints haben (nicht leer).

### Deployments prüfen

```bash
kubectl get deployments -n library-app
```

**Erwartete Ausgabe:** Alle Deployments sollten `READY` sein.

### Gesamtübersicht

```bash
kubectl get all -n library-app
```

## 📋 Logs anschauen

### Logs eines bestimmten Pods

```bash
# Pod-Namen anzeigen
kubectl get pods -n library-app

# Logs anzeigen (ersetze <pod-name> mit dem echten Namen)
kubectl logs <pod-name> -n library-app

# Beispiele:
kubectl logs library-frontend-678bc656d9-xxk7l -n library-app
kubectl logs auth-service-74947f6d6c-ltcf2 -n library-app
kubectl logs api-gateway-68dc588d79-2k689 -n library-app
```

### Logs in Echtzeit verfolgen (follow)

```bash
kubectl logs -f <pod-name> -n library-app
```

### Logs aller Pods eines Deployments

```bash
kubectl logs -l app=library-frontend -n library-app
kubectl logs -l app=auth-service -n library-app
kubectl logs -l app=library-service -n library-app
kubectl logs -l app=api-gateway -n library-app
```

### Letzte N Zeilen anzeigen

```bash
kubectl logs <pod-name> -n library-app --tail=50
```

### Ingress-Controller Logs

```bash
kubectl logs -n ingress-nginx -l app.kubernetes.io/name=ingress-nginx --tail=50
```

## 🐛 Debugging

### Pod-Details anzeigen

```bash
kubectl describe pod <pod-name> -n library-app
```

Zeigt Events, Fehler, Resource-Limits, etc.

### In einen Pod einsteigen

```bash
kubectl exec -it <pod-name> -n library-app -- /bin/sh
# oder
kubectl exec -it <pod-name> -n library-app -- /bin/bash
```

### Dateien im Pod prüfen (Frontend)

```bash
kubectl exec -it <frontend-pod-name> -n library-app -- ls -la /usr/share/nginx/html
```

### Netzwerk-Test innerhalb des Clusters

```bash
# Test-Pod starten und curl ausführen
kubectl run -it --rm test-curl --image=curlimages/curl --restart=Never -n library-app -- \
  curl -I http://library-frontend:80

# Service-Erreichbarkeit testen
kubectl run -it --rm test-curl --image=curlimages/curl --restart=Never -n library-app -- \
  curl http://auth-service:8080/actuator/health
```

### Port-Forwarding zu einem bestimmten Service

```bash
# Frontend direkt
kubectl port-forward -n library-app service/library-frontend 8080:80

# Auth-Service
kubectl port-forward -n library-app service/auth-service 8081:8080

# API Gateway
kubectl port-forward -n library-app service/api-gateway 8082:8080
```

### Events anzeigen

```bash
# Alle Events im Namespace
kubectl get events -n library-app --sort-by='.lastTimestamp'

# Events für einen bestimmten Pod
kubectl get events -n library-app --field-selector involvedObject.name=<pod-name>
```

## 🔄 Anwendung aktualisieren

### Nach Code-Änderungen

```bash
# 1. Images neu bauen
docker compose build

# 2. Images in Minikube laden
minikube image load library_app-frontend:latest
# (oder andere Services)

# 3. Deployment neu starten
kubectl rollout restart deployment library-frontend -n library-app

# 4. Rollout-Status prüfen
kubectl rollout status deployment library-frontend -n library-app
```

### Konfiguration aktualisieren

```bash
# ConfigMap oder Secret ändern, dann:
kubectl apply -f k8s/frontend/configmap.yaml

# Deployment neu starten, damit Änderungen übernommen werden
kubectl rollout restart deployment library-frontend -n library-app
```

## 🗑️ Anwendung löschen

### Alle Ressourcen löschen

```bash
# Skript verwenden
cd k8s/scripts
./delete.sh

# Oder manuell:
kubectl delete namespace library-app
```

### Einzelne Komponenten löschen

```bash
kubectl delete -f k8s/frontend/
kubectl delete -f k8s/api-gateway/
# etc.
```

## 🌐 Zugriffsmethoden

### Methode 1: Port-Forward (empfohlen für lokale Entwicklung)

```bash
kubectl port-forward -n ingress-nginx service/ingress-nginx-controller 8090:80
```

**URL:** http://localhost:8090

### Methode 2: Minikube IP + /etc/hosts

```bash
# Minikube IP herausfinden
minikube ip

# /etc/hosts bearbeiten
sudo nano /etc/hosts

# Zeile hinzufügen (ersetze IP mit deiner Minikube IP):
192.168.49.2 library.local
```

**URL:** http://library.local

> **Hinweis:** Diese Methode funktioniert nur mit `minikube tunnel` im Hintergrund:
> ```bash
> sudo minikube tunnel
> ```

### Methode 3: NodePort (direkt)

```bash
# NodePort herausfinden
kubectl get svc -n ingress-nginx

# Zugriff über Minikube IP + NodePort
# Beispiel: http://192.168.49.2:32654
```

## 📊 Nützliche Befehle

### Minikube Dashboard

```bash
minikube dashboard
```

Öffnet ein Web-UI zur Verwaltung des Clusters.

### Resource-Nutzung anzeigen

```bash
kubectl top pods -n library-app
kubectl top nodes
```

### Alle Namespaces anzeigen

```bash
kubectl get namespaces
```

### Kontext prüfen

```bash
kubectl config current-context
```

## ⚠️ Häufige Probleme

### Problem: Pod startet nicht (ImagePullBackOff)

**Lösung:**
```bash
# Image in Minikube laden
minikube image load library_app-frontend:latest

# Pod neu starten
kubectl delete pod <pod-name> -n library-app
```

### Problem: 404 Not Found beim Zugriff

**Prüfungen:**
1. Ist Port-Forward aktiv?
2. Sind alle Pods `Running`?
3. Ingress korrekt konfiguriert?

```bash
kubectl get ingress -n library-app
kubectl describe ingress library-ingress -n library-app
```

### Problem: Service nicht erreichbar

```bash
# Endpoints prüfen
kubectl get endpoints library-frontend -n library-app

# Service-Details anzeigen
kubectl describe service library-frontend -n library-app
```

### Problem: Container startet, aber crasht sofort

```bash
# Logs anschauen
kubectl logs <pod-name> -n library-app

# Vorherige Logs anschauen (nach Crash)
kubectl logs <pod-name> -n library-app --previous
```

## 📝 Wichtige Hinweise

1. **Nach jedem Image-Update** müssen die Images in Minikube geladen werden
2. **Port 8080** könnte von Docker Compose belegt sein - daher nutzen wir Port 8090
3. Der **Ingress-Controller** braucht ca. 30-60 Sekunden zum Starten nach Minikube-Start
4. **StatefulSets** (PostgreSQL) brauchen länger zum Starten als normale Deployments

## 🔗 Weitere Ressourcen

- [Minikube Dokumentation](https://minikube.sigs.k8s.io/docs/)
- [Kubernetes Dokumentation](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)