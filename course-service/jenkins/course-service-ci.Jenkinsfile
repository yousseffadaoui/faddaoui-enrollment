stage('Maven Build') {
    steps {
        dir('course-service') {
            sh 'chmod +x mvnw'
            sh './mvnw clean verify'
        }
    }
}