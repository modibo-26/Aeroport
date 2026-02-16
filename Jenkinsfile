pipeline {
    agent any
    triggers {
        githubPush()
    }
    environment {
        AWS_IP = '13.62.225.210'
        DOCKER_HUB = 'moboks'
    }
    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/modibo-26/Aeroport.git'
            }
        }
        stage('Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker-compose -p aeroport build"
                    sh "docker tag aeroport-eureka ${DOCKER_HUB}/aeroport-eureka:v1"
                    sh "docker tag aeroport-gateway ${DOCKER_HUB}/aeroport-gateway:v1"
                    sh "docker tag aeroport-service-auth ${DOCKER_HUB}/aeroport-service-auth:v1"
                    sh "docker tag aeroport-service-vols ${DOCKER_HUB}/aeroport-service-vols:v1"
                    sh "docker tag aeroport-service-reservations ${DOCKER_HUB}/aeroport-service-reservations:v1"
                    sh "docker tag aeroport-service-notifications ${DOCKER_HUB}/aeroport-service-notifications:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-eureka:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-gateway:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-service-auth:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-service-vols:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-service-reservations:v1"
                    sh "docker push ${DOCKER_HUB}/aeroport-service-notifications:v1"
                }
            }
        }
        stage('Deploy') {
            steps {
                sshagent(['aws-ssh-key']) {
                    sh "scp -o StrictHostKeyChecking=no docker-compose.prod.yml prometheus.yml ubuntu@${AWS_IP}:/home/ubuntu/"
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${AWS_IP} '
                            cd /home/ubuntu &&
                            docker-compose -f docker-compose.prod.yml pull &&
                            docker-compose -f docker-compose.prod.yml down &&
                            docker-compose -f docker-compose.prod.yml up -d
                        '
                    """
                }
            }
        }
    }
    post {
        success { echo '✅ Déploiement réussi !' }
        failure { echo '❌ Échec du déploiement' }
    }
}
