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
        stage('Detect Changes') {
            steps {
                script {
                    def changed = sh(script: "git diff --name-only HEAD~1 || echo .", returnStdout: true).trim()
                    echo "Fichiers modifiés:\n${changed}"

                    def services = ['eureka', 'gateway', 'service-auth', 'service-vols', 'service-reservations', 'service-notifications', 'service-paiement']
                    def toBuild = []

                    for (svc in services) {
                        if (changed.contains(svc + '/')) {
                            toBuild.add(svc)
                        }
                    }

                    // Si docker-compose ou Jenkinsfile modifié, tout rebuild
                    if (changed.contains('docker-compose') || changed.contains('Jenkinsfile')) {
                        toBuild = services
                    }

                    env.SERVICES_TO_BUILD = toBuild.join(',')
                    echo "Services à rebuild: ${env.SERVICES_TO_BUILD ?: 'aucun'}"
                }
            }
        }
        stage('Build & Push') {
            when {
                expression { env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    script {
                        def services = env.SERVICES_TO_BUILD.split(',')
                        for (svc in services) {
                            def imageName = "aeroport-${svc}"
                            sh "docker-compose -p aeroport build ${svc}"
                            sh "docker tag ${imageName} ${DOCKER_HUB}/${imageName}:v1"
                            sh "docker push ${DOCKER_HUB}/${imageName}:v1"
                        }
                    }
                }
            }
        }
        stage('Deploy') {
            when {
                expression { env.SERVICES_TO_BUILD?.trim() }
            }
            steps {
                sshagent(['aws-ssh-key']) {
                    sh "scp -o StrictHostKeyChecking=no docker-compose.prod.yml prometheus.yml ubuntu@${AWS_IP}:/home/ubuntu/"
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${AWS_IP} '
                            cd /home/ubuntu &&
                            docker rm -f aeroport-frontend || true &&
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