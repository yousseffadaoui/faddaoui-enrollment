// Pipeline CD — Enrollment-service (agent Windows : bat + kubectl)
// Prérequis : kubectl installé sur l’agent, kubeconfig pointant vers le cluster (kubeadm sur GCP),
// droits suffisants pour apply + rollout.
//
// Manifestes : k8s/enrollment-deployment.yml et k8s/enrollment-service.yaml

pipeline {
    agent any

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Kubernetes apply') {
            steps {
                bat '''
                    kubectl apply -f k8s/enrollment-deployment.yml -f k8s/enrollment-service.yaml
                '''
            }
        }

        stage('Rollout status') {
            steps {
                bat '''
                    kubectl rollout status deployment/enrollment-service --timeout=5m
                '''
            }
        }
    }
}
