pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'you25/course-service:latest'
        CD_JOB_NAME = 'course-service-cd'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Maven Build') {
            steps {
                dir('course-service') {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean verify'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('course-service') {
                    withSonarQubeEnv('SonarQube') {
                        sh './mvnw sonar:sonar -Dsonar.projectKey=course-service -Dsonar.projectName=course-service'
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir('course-service') {
                    sh 'docker build -t $DOCKER_IMAGE .'
                }
            }
        }

        stage('Trigger CD') {
            steps {
                build job: "${CD_JOB_NAME}", wait: false
            }
        }
    }
}