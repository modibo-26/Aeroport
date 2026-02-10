pipeline {
    agent any

    triggers {
        githubPush()
    }

    environment {
        DOCKER_HUB = 'moboks'
    }

    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/modibo-26/Aeroport.git'
            }
        }

        stage('Build') {
            steps {
                sh 'docker-compose build'
            }
        }

        stage('Push to Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'

                    sh 'docker tag aeroport_eureka $DOCKER_HUB/aeroport-eureka:v1'
                    sh 'docker tag aeroport_gateway $DOCKER_HUB/aeroport-gateway:v1'
                    sh 'docker tag aeroport_service-auth $DOCKER_HUB/aeroport-service-auth:v1'
                    sh 'docker tag aeroport_service-vols $DOCKER_HUB/aeroport-service-vols:v1'
                    sh 'docker tag aeroport_service-reservations $DOCKER_HUB/aeroport-service-reservations:v1'
                    sh 'docker tag aeroport_service-notifications $DOCKER_HUB/aeroport-service-notifications:v1'

                    sh 'docker push $DOCKER_HUB/aeroport-eureka:v1'
                    sh 'docker push $DOCKER_HUB/aeroport-gateway:v1'
                    sh 'docker push $DOCKER_HUB/aeroport-service-auth:v1'
                    sh 'docker push $DOCKER_HUB/aeroport-service-vols:v1'
                    sh 'docker push $DOCKER_HUB/aeroport-service-reservations:v1'
                    sh 'docker push $DOCKER_HUB/aeroport-service-notifications:v1'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose down || true'
                sh 'docker-compose up -d'
            }
        }
    }

    post {
        success {
            echo '✅ Déploiement réussi !'
        }
        failure {
            echo '❌ Échec du déploiement'
        }
    }
}