// Pipeline CI — Enrollment-service (agent Windows : utiliser bat)
// Prérequis Jenkins : JDK, Docker, credentials Docker Hub, plugins AnsiColor (optionnel), JUnit,
// SonarQube Scanner + installation « SonarQube » (pour withSonarQubeEnv).
//
// Jobs : nommer le job CD exactement comme ci-dessous pour le déclenchement.
// Credential Docker Hub : créer une entrée « Username with password » et renseigner DOCKERHUB_CREDENTIALS_ID.

pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        DOCKER_IMAGE = 'yousseffadaoui/enrollment-service'
        DOCKER_TAG = 'latest'
        // ID Jenkins des identifiants Docker Hub (username/password)
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-yousseffadaoui'
        // Clé / nom du projet Sonar (adapter si besoin)
        SONAR_PROJECT_KEY = 'enrollment-service'
        SONAR_PROJECT_NAME = 'enrollment-service'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Resolve service directory') {
            steps {
                script {
                    def root = fileExists('Enrollment-service/pom.xml') ? 'Enrollment-service' : '.'
                    env.SVC_ROOT = root
                    env.MAVEN_CLI = fileExists("${root}/mvnw") ? './mvnw' : 'mvn'
                }
            }
        }

        stage('Maven — clean verify (tests + JaCoCo)') {
            steps {
                dir("${env.SVC_ROOT}") {
                    sh """
                        if [ "${env.MAVEN_CLI}" = "./mvnw" ]; then
                            chmod +x ./mvnw
                        fi
                        ${env.MAVEN_CLI} clean verify -B
                    """
                }
            }
            post {
                always {
                    dir("${env.SVC_ROOT}") {
                        junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                        // Rapport HTML/XML JaCoCo : target/site/jacoco/ (généré par Maven lors du verify)
                    }
                }
            }
        }

        stage('SonarQube analysis') {
            steps {
                dir("${env.SVC_ROOT}") {
                    // Nom de l’installation SonarQube défini dans Jenkins > Manage Jenkins > Configure System
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            if [ "${env.MAVEN_CLI}" = "./mvnw" ]; then
                                chmod +x ./mvnw
                            fi
                            ${env.MAVEN_CLI} -B sonar:sonar \\
                              -Dsonar.projectKey=$SONAR_PROJECT_KEY \\
                              -Dsonar.projectName=$SONAR_PROJECT_NAME \\
                              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
                }
            }
        }

        stage('Docker build') {
            steps {
                script {
                    def ctx = "${env.SVC_ROOT}"
                    def dockerfile = "${ctx}/Dockerfile"
                    if (ctx == '.') {
                        dockerfile = 'Dockerfile'
                    }
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -f ${dockerfile} ${ctx}
                    """
                }
            }
        }

        stage('Docker Hub push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                    usernameVariable: 'DH_USER',
                    passwordVariable: 'DH_PASS'
                )]) {
                    sh """
                        docker login -u $DH_USER -p $DH_PASS
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                    """
                }
            }
        }

        stage('Trigger CD') {
            steps {
                build job: 'enrollment-service-CD', wait: false
            }
        }
    }
}
