pipeline {
  agent any

  environment {
    DOCKER_IMAGE = 'haviineesh/hactt-demo'
  }

  stages {
    stage('Checkout Code') {
      steps {
        git credentialsId: 'github-credentials', url: 'https://github.com/Haviineesh/HACTT-FRONTEND.git'
      }
    }

    stage('Build Spring Boot App') {
      steps {
        dir('demo') {
          sh 'mvn clean package -DskipTests'
        }
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          dockerImage = docker.build("${DOCKER_IMAGE}:latest", "demo")
        }
      }
    }

    stage('Push to Docker Hub') {
      steps {
        script {
          docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
            dockerImage.push('latest')
          }
        }
      }
    }

    stage('Deploy to Minikube') {
      steps {
        withCredentials([file(credentialsId: 'kubeconfig-minikube', variable: 'KUBECONFIG')]) {
          sh 'kubectl apply -f demo/deployment.yaml'
          sh 'kubectl apply -f demo/service.yaml'
        }
      }
    }
  }
}
