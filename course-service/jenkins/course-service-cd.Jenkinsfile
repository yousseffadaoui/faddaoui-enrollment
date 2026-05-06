pipeline {
    agent any

    stages {

        stage('Deploy to Kubernetes') {
            steps {

                withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {

                    sh '''
                    export KUBECONFIG=$KUBECONFIG_FILE

                    kubectl apply -f course-service/k8s/course-deployment.yml
                    kubectl apply -f course-service/k8s/course-service.yaml

                    kubectl rollout status deployment/course-service --timeout=60s || true
                    kubectl get pods
                    kubectl get svc
                    '''
                }
            }
        }
    }
}