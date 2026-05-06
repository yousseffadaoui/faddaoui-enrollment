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
                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                        export KUBECONFIG=$KUBECONFIG_FILE
                        kubectl apply -f Enrollment-service/k8s/enrollment-deployment.yml -f Enrollment-service/k8s/enrollment-service.yaml --validate=false
                        kubectl rollout status deployment/enrollment-service --timeout=5m
                    '''
                }
            }
        }
    }
}
