pipeline {
    agent any
    triggers {
        githubPush()
    }
    environment {
        DOCKER_HUB = 'moboks'
    }
    stages {
        stage('Deploy') {
            steps {
                sh 'docker-compose -f docker-compose.prod.yml pull'
                sh 'docker-compose -f docker-compose.prod.yml down || true'
                sh 'docker-compose -f docker-compose.prod.yml up -d'
            }
        }
    }
    post {
        success { echo '✅ Déploiement réussi !' }
        failure { echo '❌ Échec du déploiement' }
    }
}