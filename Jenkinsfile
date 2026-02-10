pipeline {
    agent any
    triggers {
        githubPush()
    }
    environment {
        AWS_IP = '51.20.187.103'
    }
    stages {
        stage('Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/modibo-26/Aeroport.git'
            }
        }
        stage('Deploy') {
            steps {
                sshagent(['aws-ssh-key']) {
                    sh "scp -o StrictHostKeyChecking=no docker-compose.prod.yml ubuntu@${AWS_IP}:/home/ubuntu/"
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